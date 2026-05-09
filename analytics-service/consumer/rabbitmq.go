package consumer

import (
	"analytics-service/buffer"
	"encoding/json"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.uber.org/zap"
)

type PostViewEvent struct {
	PostID   string  `json:"post_id"`
	ViewerID *string `json:"viewer_id"`
	IPHash   *string `json:"ip_hash"`
	Referrer *string `json:"referrer"`
}

type Consumer struct {
	ch        *amqp.Channel
	viewBuf   *buffer.ViewBuffer
	detailBuf *buffer.DetailBuffer
	logger    *zap.Logger
}

func New(ch *amqp.Channel, viewBuf *buffer.ViewBuffer, detailBuf *buffer.DetailBuffer, logger *zap.Logger) *Consumer {
	return &Consumer{ch: ch, viewBuf: viewBuf, detailBuf: detailBuf, logger: logger}
}

func (c *Consumer) ConsumePostViewTracked() {
	if err := c.ch.ExchangeDeclare("post.events", "topic", true, false, false, false, nil); err != nil {
		c.logger.Fatal("exchange declare failed", zap.Error(err))
	}

	q, err := c.ch.QueueDeclare("analytics.post-view-tracked", true, false, false, false, nil)
	if err != nil {
		c.logger.Fatal("queue declare failed", zap.Error(err))
	}

	if err := c.ch.QueueBind(q.Name, "post.view.tracked", "post.events", false, nil); err != nil {
		c.logger.Fatal("queue bind failed", zap.Error(err))
	}

	msgs, err := c.ch.Consume(q.Name, "", false, false, false, false, nil)
	if err != nil {
		c.logger.Fatal("consume failed", zap.Error(err))
	}

	for msg := range msgs {
		var event PostViewEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Warn("unmarshal event failed", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		date := time.Now().Format("2006-01-02")
		if err := c.viewBuf.Track(event.PostID, date); err != nil {
			c.logger.Error("track view failed", zap.Error(err))
			msg.Nack(false, true)
			continue
		}

		c.detailBuf.Append(buffer.ViewDetail{
			PostID:   event.PostID,
			ViewerID: event.ViewerID,
			IPHash:   event.IPHash,
			Referrer: event.Referrer,
			ViewedAt: time.Now(),
		})

		msg.Ack(false)
	}
}
