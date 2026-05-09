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
	"github.com/jackc/pgx/v5/pgxpool"
	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

func main() {
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	cfg := config.Load()

	pool, err := pgxpool.New(context.Background(), cfg.DatabaseURL)
	if err != nil {
		logger.Fatal("postgres connect failed", zap.Error(err))
	}
	defer pool.Close()

	store := db.New(pool)
	if err := store.Migrate(context.Background()); err != nil {
		logger.Fatal("migrate failed", zap.Error(err))
	}

	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr,
		Password: cfg.RedisPassword,
	})

	conn, err := amqp.Dial(cfg.RabbitMQURL)
	if err != nil {
		logger.Fatal("rabbitmq connect failed", zap.Error(err))
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		logger.Fatal("rabbitmq channel failed", zap.Error(err))
	}
	defer ch.Close()

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
