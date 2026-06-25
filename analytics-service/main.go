package main

import (
	"analytics-service/buffer"
	"analytics-service/config"
	"analytics-service/consumer"
	"analytics-service/db"
	"analytics-service/handler"
	"analytics-service/publisher"
	"analytics-service/scheduler"
	"context"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/redis/go-redis/v9"
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

	if err := db.RunMigrations(cfg.DatabaseURL); err != nil {
		logger.Fatal("migrate failed", zap.Error(err))
	}

	pool, err := pgxpool.New(context.Background(), cfg.DatabaseURL)
	if err != nil {
		logger.Fatal("postgres connect failed", zap.Error(err))
	}
	defer pool.Close()

	store := db.New(pool)

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
			logger.Error("RabbitMQ connection close failed", zap.Error(err))
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

	viewBuf := buffer.NewViewBuffer(rdb)
	detailBuf := buffer.NewDetailBuffer()

	pub := publisher.New(ch)
	c := consumer.New(ch, viewBuf, detailBuf, logger)
	go c.ConsumePostViewTracked()

	s := scheduler.New(viewBuf, detailBuf, store, pub, logger)
	s.Start()

	hdl := handler.New(store, logger)

	r := gin.New()
	r.Use(gin.Recovery())

	r.POST("/api/analytics/read-time", hdl.TrackReadTime)

	auth := r.Group("/api/analytics", handler.RequireUserID())
	auth.GET("/posts/:id/stats", hdl.GetStats)
	auth.GET("/posts/:id/daily", hdl.GetDailyStats)

	logger.Info("analytics service listening", zap.String("port", cfg.Port))
	if err := r.Run(":" + cfg.Port); err != nil {
		logger.Fatal("server failed", zap.Error(err))
	}
}
