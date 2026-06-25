package publisher

import (
	"encoding/json"

	amqp "github.com/rabbitmq/amqp091-go"
)

const QueueMediaProcess = "media.process"

type MediaProcessEvent struct {
	UserID   string `json:"userId"`
	Filename string `json:"filename"`
}

type Publisher struct {
	ch *amqp.Channel
}

func New(ch *amqp.Channel) *Publisher {
	_, err := ch.QueueDeclare(QueueMediaProcess, true, false, false, false, nil)
	if err != nil {
		panic("media.process queue declare failed: " + err.Error())
	}
	return &Publisher{ch: ch}
}

func (p *Publisher) PublishMediaProcess(event MediaProcessEvent) error {
	body, err := json.Marshal(event)
	if err != nil {
		return err
	}
	return p.ch.Publish("", QueueMediaProcess, false, false, amqp.Publishing{
		ContentType: "application/json",
		Body:        body,
	})
}
