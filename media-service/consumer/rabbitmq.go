package consumer

import (
	"encoding/json"

	"media-service/processor"
	"media-service/publisher"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.uber.org/zap"
)

type Consumer struct {
	ch        *amqp.Channel
	processor *processor.Processor
	logger    *zap.Logger
}

func New(ch *amqp.Channel, proc *processor.Processor, logger *zap.Logger) *Consumer {
	_, err := ch.QueueDeclare(publisher.QueueMediaProcess, true, false, false, false, nil)
	if err != nil {
		logger.Fatal("media.process queue declare failed", zap.Error(err))
	}
	return &Consumer{ch: ch, processor: proc, logger: logger}
}

func (c *Consumer) ConsumeMediaProcess() {
	msgs, _ := c.ch.Consume(publisher.QueueMediaProcess, "", false, false, false, false, nil)
	for msg := range msgs {
		var event publisher.MediaProcessEvent
		if err := json.Unmarshal(msg.Body, &event); err != nil {
			c.logger.Error("unmarshal media.process", zap.Error(err))
			msg.Nack(false, false)
			continue
		}

		if err := c.processor.Process(event.UserID, event.Filename); err != nil {
			c.logger.Error("process image failed",
				zap.String("userID", event.UserID),
				zap.String("filename", event.Filename),
				zap.Error(err),
			)
			msg.Nack(false, true) // requeue
			continue
		}
		msg.Ack(false)
	}
}
