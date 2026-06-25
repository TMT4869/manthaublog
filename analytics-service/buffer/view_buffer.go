package buffer

import (
	"context"
	"fmt"

	"github.com/redis/go-redis/v9"
)

var ctx = context.Background()

type ViewBuffer struct {
	rdb *redis.Client
}

func NewViewBuffer(rdb *redis.Client) *ViewBuffer {
	return &ViewBuffer{rdb: rdb}
}

func (b *ViewBuffer) Track(postID, date string) error {
	key := fmt.Sprintf("analytics:views:%s:%s", postID, date)
	return b.rdb.Incr(ctx, key).Err()
}

func (b *ViewBuffer) FlushAll() (map[string]int64, error) {
	keys, err := b.rdb.Keys(ctx, "analytics:views:*").Result()
	if err != nil || len(keys) == 0 {
		return nil, err
	}

	pipe := b.rdb.Pipeline()
	cmds := make([]*redis.StringCmd, len(keys))
	for i, key := range keys {
		cmds[i] = pipe.GetDel(ctx, key)
	}
	if _, err := pipe.Exec(ctx); err != nil && err != redis.Nil {
		return nil, err
	}

	result := make(map[string]int64, len(keys))
	for i, cmd := range cmds {
		val, _ := cmd.Int64()
		if val > 0 {
			result[keys[i]] = val
		}
	}
	return result, nil
}
