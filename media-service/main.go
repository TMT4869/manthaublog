package main

import (
	"media-service/config"
	"media-service/consumer"
	"media-service/handler"
	"media-service/processor"
	"media-service/publisher"
	"media-service/router"
	"media-service/storage"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.uber.org/zap"
)

func main() {
	logger, _ := zap.NewProduction()
	defer func() { _ = logger.Sync() }()

	cfg := config.Load()

	store, err := storage.NewMinio(
		cfg.MinioEndpoint,
		cfg.MinioAccessKey,
		cfg.MinioSecretKey,
		cfg.MinioBucket,
		cfg.MinioUseSSL,
	)
	if err != nil {
		logger.Fatal("minio connect failed", zap.Error(err))
	}

	conn, err := amqp.Dial(cfg.RabbitMQURL)
	if err != nil {
		logger.Fatal("RabbitMQ connect failed", zap.Error(err))
	}
	defer conn.Close()

	pubCh, err := conn.Channel()
	if err != nil {
		logger.Fatal("RabbitMQ publisher channel failed", zap.Error(err))
	}
	defer pubCh.Close()

	consCh, err := conn.Channel()
	if err != nil {
		logger.Fatal("RabbitMQ consumer channel failed", zap.Error(err))
	}
	defer consCh.Close()

	proc := processor.New(store)
	pub := publisher.New(pubCh)
	cons := consumer.New(consCh, proc, logger)
	h := handler.New(store, pub, logger)

	go cons.ConsumeMediaProcess()

	r := router.Setup(h, cfg.StoragePath)

	logger.Info("media service listening", zap.String("port", cfg.Port))
	if err := r.Run(":" + cfg.Port); err != nil {
		logger.Fatal("server failed", zap.Error(err))
	}
}
