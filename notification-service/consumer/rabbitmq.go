package consumer

import (
	"context"
	"encoding/json"
	"fmt"
	"net/smtp"
	"sync"

	"notification-service/client"
	"notification-service/config"
	"notification-service/db"
	"notification-service/publisher"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.uber.org/zap"
)

const (
	batchSize      = 500
	workerPool     = 8
	maxRetries     = 3
	queueEmailSend = "email.send"
)

type Consumer struct {
	ch         *amqp.Channel
	store      db.Storer
	pub        publisher.Publisher
	userClient *client.UserClient
	cfg        *config.Config
	logger     *zap.Logger
}

func New(
	ch *amqp.Channel,
	store db.Storer,
	pub publisher.Publisher,
	userClient *client.UserClient,
	cfg *config.Config,
	logger *zap.Logger,
) *Consumer {
	return &Consumer{ch: ch, store: store, pub: pub, userClient: userClient, cfg: cfg, logger: logger}
}

// ---- event payloads ----

type PostApprovedEvent struct {
	PostID   string `json:"postId"`
	AuthorID string `json:"authorId"`
	Title    string `json:"title"`
	Link     string `json:"link"`
}

type PostSubmittedEvent struct {
	PostID   string `json:"postId"`
	AuthorID string `json:"authorId"`
	Title    string `json:"title"`
	Link     string `json:"link"`
}

type PostRejectedEvent struct {
	PostID          string `json:"postId"`
	AuthorID        string `json:"authorId"`
	Title           string `json:"title"`
	RejectionReason string `json:"rejectionReason"`
}

type UserFollowedEvent struct {
	FollowerID   string `json:"followerId"`
	FollowedID   string `json:"followedId"`
	FollowerName string `json:"followerName"`
}

type CommentCreatedEvent struct {
	PostID                string `json:"postId"`
	PostAuthorID          string `json:"postAuthorId"`
	CommenterID           string `json:"commenterId"`
	CommenterName         string `json:"commenterName"`
	PostTitle             string `json:"postTitle"`
	PostLink              string `json:"postLink"`
	ParentCommentAuthorID string `json:"parentCommentAuthorId,omitempty"`
}

type EmailSendEvent struct {
	To         string `json:"to"`
	Subject    string `json:"subject"`
	Body       string `json:"body"`
	RetryCount int    `json:"retryCount"`
}

type NewsletterEvent struct {
	Subscribers []string `json:"subscribers"`
	Subject     string   `json:"subject"`
	Body        string   `json:"body"`
}

// ---- helpers ----

func (c *Consumer) declareQueue(name string) {
	_, err := c.ch.QueueDeclare(name, true, false, false, false, nil)
	if err != nil {
		c.logger.Fatal("queue declare failed", zap.String("queue", name), zap.Error(err))
	}
}

// fanOut publishes notifications to Redis using a fixed worker pool.
func (c *Consumer) fanOut(ctx context.Context, notifications []db.Notification) {
	jobs := make(chan db.Notification, len(notifications))
	var wg sync.WaitGroup
	for i := 0; i < workerPool; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for n := range jobs {
				if err := c.pub.Publish(ctx, n.UserID, n.ToJSON()); err != nil {
					c.logger.Warn("redis publish failed", zap.String("userID", n.UserID), zap.Error(err))
				}
			}
		}()
	}
	for _, n := range notifications {
		jobs <- n
	}
	close(jobs)
	wg.Wait()
}

func (c *Consumer) sendEmail(to, subject, body string) error {
	auth := smtp.PlainAuth("", c.cfg.SMTPUser, c.cfg.SMTPPassword, c.cfg.SMTPHost)
	msg := fmt.Sprintf("To: %s\r\nSubject: %s\r\n\r\n%s", to, subject, body)
	return smtp.SendMail(c.cfg.SMTPHost+":"+c.cfg.SMTPPort, auth, c.cfg.SMTPUser, []string{to}, []byte(msg))
}

// saveAndPublish saves a notification to DB then publishes it to Redis.
func (c *Consumer) saveAndPublish(ctx context.Context, n db.Notification) error {
	saved, err := c.store.Save(ctx, n)
	if err != nil {
		return err
	}
	_ = c.pub.Publish(ctx, saved.UserID, saved.ToJSON())
	return nil
}

// ---- consumers ----

// ConsumePostApproved notifies the author and fan-outs to all followers (paged batch).
func (c *Consumer) ConsumePostApproved() {
	c.declareQueue("post.approved")
	msgs, _ := c.ch.Consume("post.approved", "", false, false, false, false, nil)
	ctx := context.Background()

	for msg := range msgs {
		var event PostApprovedEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal post.approved", zap.Error(err))
			msg.Nack(false, false)
			continue
		}
		c.handlePostApproved(ctx, event)
		msg.Ack(false)
	}
}

func (c *Consumer) handlePostApproved(ctx context.Context, event PostApprovedEvent) {
	if err := c.saveAndPublish(ctx, db.Notification{
		UserID: event.AuthorID,
		Type:   "post_approved",
		Title:  fmt.Sprintf("Your post \"%s\" has been approved", event.Title),
		Link:   event.Link,
	}); err != nil {
		c.logger.Error("save author notification", zap.Error(err))
	}
	c.fanOutFollowers(ctx, event)
}

// fanOutFollowers pages through followers and batch-notifies each page.
func (c *Consumer) fanOutFollowers(ctx context.Context, event PostApprovedEvent) {
	for page := 0; ; page++ {
		followers, hasNext := c.userClient.GetFollowersPaged(event.AuthorID, page, batchSize)
		if len(followers) == 0 {
			return
		}

		notifications := make([]db.Notification, 0, len(followers))
		for _, fid := range followers {
			notifications = append(notifications, db.Notification{
				UserID: fid,
				Type:   "new_post",
				Title:  fmt.Sprintf("New post: \"%s\"", event.Title),
				Link:   event.Link,
			})
		}

		if err := c.store.BatchInsert(ctx, notifications); err != nil {
			c.logger.Error("batch insert followers", zap.Error(err))
		}
		c.fanOut(ctx, notifications)

		if !hasNext {
			return
		}
	}
}

// ConsumePostSubmitted fan-outs to all admins (small set, no paging needed).
func (c *Consumer) ConsumePostSubmitted() {
	c.declareQueue("post.submitted")
	msgs, _ := c.ch.Consume("post.submitted", "", false, false, false, false, nil)
	ctx := context.Background()

	for msg := range msgs {
		var event PostSubmittedEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal post.submitted", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		for _, adminID := range c.userClient.GetAdmins() {
			saved, err := c.store.Save(ctx, db.Notification{
				UserID: adminID,
				Type:   "post_submitted",
				Title:  fmt.Sprintf("New post submitted: \"%s\"", event.Title),
				Link:   event.Link,
			})
			if err != nil {
				c.logger.Error("save admin notification", zap.Error(err))
				continue
			}
			_ = c.pub.Publish(ctx, adminID, saved.ToJSON())
		}
		msg.Ack(false)
	}
}

// ConsumePostRejected notifies the author with the rejection reason.
func (c *Consumer) ConsumePostRejected() {
	c.declareQueue("post.rejected")
	msgs, _ := c.ch.Consume("post.rejected", "", false, false, false, false, nil)
	ctx := context.Background()

	for msg := range msgs {
		var event PostRejectedEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal post.rejected", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		saved, err := c.store.Save(ctx, db.Notification{
			UserID: event.AuthorID,
			Type:   "post_rejected",
			Title:  fmt.Sprintf("Your post \"%s\" was rejected", event.Title),
			Body:   event.RejectionReason,
		})
		if err != nil {
			c.logger.Error("save post rejected notification", zap.Error(err))
			msg.Nack(false, true)
			continue
		}
		_ = c.pub.Publish(ctx, saved.UserID, saved.ToJSON())
		msg.Ack(false)
	}
}

// ConsumeUserFollowed notifies the followed user.
func (c *Consumer) ConsumeUserFollowed() {
	c.declareQueue("user.followed")
	msgs, _ := c.ch.Consume("user.followed", "", false, false, false, false, nil)
	ctx := context.Background()

	for msg := range msgs {
		var event UserFollowedEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal user.followed", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		saved, err := c.store.Save(ctx, db.Notification{
			UserID: event.FollowedID,
			Type:   "new_follower",
			Title:  fmt.Sprintf("%s started following you", event.FollowerName),
		})
		if err != nil {
			c.logger.Error("save follower notification", zap.Error(err))
			msg.Nack(false, true)
			continue
		}
		_ = c.pub.Publish(ctx, saved.UserID, saved.ToJSON())
		msg.Ack(false)
	}
}

// ConsumeCommentCreated notifies the post author and the parent comment author (if a reply).
func (c *Consumer) ConsumeCommentCreated() {
	c.declareQueue("comment.created")
	msgs, _ := c.ch.Consume("comment.created", "", false, false, false, false, nil)
	ctx := context.Background()

	for msg := range msgs {
		var event CommentCreatedEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal comment.created", zap.Error(err))
			msg.Nack(false, false)
			continue
		}
		c.handleCommentCreated(ctx, event)
		msg.Ack(false)
	}
}

func (c *Consumer) handleCommentCreated(ctx context.Context, event CommentCreatedEvent) {
	if event.PostAuthorID != event.CommenterID {
		if err := c.saveAndPublish(ctx, db.Notification{
			UserID: event.PostAuthorID,
			Type:   "new_comment",
			Title:  fmt.Sprintf("%s commented on your post \"%s\"", event.CommenterName, event.PostTitle),
			Link:   event.PostLink,
		}); err != nil {
			c.logger.Error("save comment notification for post author", zap.Error(err))
		}
	}

	if event.ParentCommentAuthorID != "" && event.ParentCommentAuthorID != event.CommenterID {
		if err := c.saveAndPublish(ctx, db.Notification{
			UserID: event.ParentCommentAuthorID,
			Type:   "new_comment",
			Title:  fmt.Sprintf("%s replied to your comment on \"%s\"", event.CommenterName, event.PostTitle),
			Link:   event.PostLink,
		}); err != nil {
			c.logger.Error("save reply notification", zap.Error(err))
		}
	}
}

// ConsumeEmailSend sends transactional emails with retry up to maxRetries before going to DLQ.
func (c *Consumer) ConsumeEmailSend() {
	c.declareQueue(queueEmailSend)
	msgs, _ := c.ch.Consume(queueEmailSend, "", false, false, false, false, nil)

	for msg := range msgs {
		var event EmailSendEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal email.send", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		if err := c.sendEmail(event.To, event.Subject, event.Body); err != nil {
			c.logger.Error("send email failed", zap.String("to", event.To), zap.Error(err))
			if event.RetryCount >= maxRetries {
				// Exhausted retries — nack to dead-letter queue
				msg.Nack(false, false)
			} else {
				// Re-enqueue with incremented retry count
				event.RetryCount++
				retryBody, _ := json.Marshal(event)
				_ = c.ch.Publish("", queueEmailSend, false, false, amqp.Publishing{
					ContentType: "application/json",
					Body:        retryBody,
				})
				msg.Ack(false)
			}
			continue
		}
		msg.Ack(false)
	}
}

// ConsumeNewsletter sends bulk newsletter emails to all subscribers.
func (c *Consumer) ConsumeNewsletter() {
	c.declareQueue("newsletter.send")
	msgs, _ := c.ch.Consume("newsletter.send", "", false, false, false, false, nil)

	for msg := range msgs {
		var event NewsletterEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal newsletter.send", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		for _, to := range event.Subscribers {
			if err := c.sendEmail(to, event.Subject, event.Body); err != nil {
				c.logger.Error("newsletter send failed", zap.String("to", to), zap.Error(err))
			}
		}
		msg.Ack(false)
	}
}
