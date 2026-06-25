package main

import (
	"log"

	"api-gateway/config"
	"api-gateway/router"

	"github.com/redis/go-redis/v9"
)

func main() {
	cfg := config.Load()

	rdb := redis.NewClient(&redis.Options{
		Addr: cfg.RedisAddr,
	})

	r := router.Setup(rdb, cfg)

	if err := r.Run(":" + cfg.Port); err != nil {
		log.Fatal(err)
	}
}
