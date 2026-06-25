package publisher

import (
	"context"
	"encoding/json"

	amqp "github.com/rabbitmq/amqp091-go"
)

type ViewCountUpdatedEvent struct {
	PostID    string `json:"post_id"`
	ViewCount int64  `json:"view_count"`
}

type Publisher struct {
	ch *amqp.Channel
}

func New(ch *amqp.Channel) *Publisher {
	if err := ch.ExchangeDeclare("analytics.events", "topic", true, false, false, false, nil); err != nil {
		panic("analytics.events exchange declare failed: " + err.Error())
	}
	return &Publisher{ch: ch}
}

func (p *Publisher) PublishViewCountUpdated(ctx context.Context, event ViewCountUpdatedEvent) error {
	body, err := json.Marshal(event)
	if err != nil {
		return err
	}
	return p.ch.PublishWithContext(ctx, "analytics.events", "view.count.updated", false, false, amqp.Publishing{
		ContentType:  "application/json",
		DeliveryMode: amqp.Persistent,
		Body:         body,
	})
}
