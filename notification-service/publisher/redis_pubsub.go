package publisher

import (
	"context"

	"github.com/redis/go-redis/v9"
)

type Publisher interface {
	Publish(ctx context.Context, userID, payload string) error
}

type redisPublisher struct {
	rdb *redis.Client
}

func New(rdb *redis.Client) Publisher {
	return &redisPublisher{rdb: rdb}
}

func (p *redisPublisher) Publish(ctx context.Context, userID, payload string) error {
	return p.rdb.Publish(ctx, "notifications:"+userID, payload).Err()
}