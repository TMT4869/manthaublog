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
