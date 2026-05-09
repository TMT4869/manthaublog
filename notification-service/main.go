package main

import (
	"context"
	"notification-service/client"
	"notification-service/config"
	"notification-service/consumer"
	"notification-service/db"
	"notification-service/handler"
	"notification-service/hub"
	"notification-service/publisher"

	"github.com/gin-gonic/gin"
	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/redis/go-redis/v9"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
	"go.uber.org/zap"
)

func main() {
	logger, _ := zap.NewProduction()
	defer func(logger *zap.Logger) {
		err := logger.Sync()
		if err != nil {
			logger.Error("logger sync failed", zap.Error(err))
		}
	}(logger)

	cfg := config.Load()

	mongoClient, err := mongo.Connect(options.Client().ApplyURI(cfg.DatabaseURL))
	if err != nil {
		logger.Fatal("mongodb connect failed", zap.Error(err))
	}
	defer func() {
		if err := mongoClient.Disconnect(context.Background()); err != nil {
			logger.Error("mongodb disconnect failed", zap.Error(err))
		}
	}()

	col := mongoClient.Database("notification_db").Collection("notifications")
	store := db.New(col)
	if err := store.Migrate(context.Background()); err != nil {
		logger.Fatal("migrate failed", zap.Error(err))
	}

	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr,
		Password: cfg.RedisPassword,
	})

	conn, err := amqp.Dial(cfg.RabbitMQURL)
	if err != nil {
		logger.Fatal("RabbitMQ connect failed", zap.Error(err))
	}
	defer func(conn *amqp.Connection) {
		err := conn.Close()
		if err != nil {
			logger.Error("RabbitMQ close failed", zap.Error(err))
		}
	}(conn)

	ch, err := conn.Channel()
	if err != nil {
		logger.Fatal("RabbitMQ channel failed", zap.Error(err))
	}
	defer func(ch *amqp.Channel) {
		err := ch.Close()
		if err != nil {
			logger.Error("RabbitMQ channel close failed", zap.Error(err))
		}
	}(ch)

	pub := publisher.New(rdb)
	userClient := client.NewUserClient(cfg.AuthServiceURL)
	h := hub.New(rdb, logger)
	c := consumer.New(ch, store, pub, userClient, cfg, logger)

	go c.ConsumePostSubmitted()
	go c.ConsumePostApproved()
	go c.ConsumePostRejected()
	go c.ConsumeUserFollowed()
	go c.ConsumeCommentCreated()
	go c.ConsumeEmailSend()
	go c.ConsumeNewsletter()

	hdl := handler.New(store, h, logger)

	r := gin.New()
	r.Use(gin.Recovery())

	auth := handler.RequireUserID()

	api := r.Group("/api/notifications", auth)
	api.GET("", hdl.List)
	api.PUT("/read-all", hdl.MarkAllRead)
	api.PUT("/:id/read", hdl.MarkRead)

	r.GET("/ws", auth, hdl.WebSocket)

	logger.Info("notification service listening", zap.String("port", cfg.Port))
	if err := r.Run(":" + cfg.Port); err != nil {
		logger.Fatal("server failed", zap.Error(err))
	}
}
