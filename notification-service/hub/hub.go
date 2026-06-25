package hub

import (
	"context"
	"sync"

	"github.com/gorilla/websocket"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

type Hub struct {
	clients map[string]*websocket.Conn
	cancels map[string]context.CancelFunc
	mu      sync.RWMutex
	rdb     *redis.Client
	logger  *zap.Logger
}

func New(rdb *redis.Client, logger *zap.Logger) *Hub {
	return &Hub{
		clients: make(map[string]*websocket.Conn),
		cancels: make(map[string]context.CancelFunc),
		rdb:     rdb,
		logger:  logger,
	}
}

func (h *Hub) Register(userID string, conn *websocket.Conn) {
	ctx, cancel := context.WithCancel(context.Background())

	h.mu.Lock()
	// Cancel any previous subscription for this user (reconnect case)
	if old, ok := h.cancels[userID]; ok {
		old()
	}
	h.clients[userID] = conn
	h.cancels[userID] = cancel
	h.mu.Unlock()

	go h.subscribeRedis(ctx, userID, conn)
}

func (h *Hub) Unregister(userID string) {
	h.mu.Lock()
	if cancel, ok := h.cancels[userID]; ok {
		cancel()
		delete(h.cancels, userID)
	}
	delete(h.clients, userID)
	h.mu.Unlock()
}

func (h *Hub) subscribeRedis(ctx context.Context, userID string, conn *websocket.Conn) {
	sub := h.rdb.Subscribe(ctx, "notifications:"+userID)
	defer sub.Close()

	for msg := range sub.Channel() {
		if err := conn.WriteMessage(websocket.TextMessage, []byte(msg.Payload)); err != nil {
			h.logger.Warn("ws write error", zap.String("userID", userID), zap.Error(err))
			h.Unregister(userID)
			return
		}
	}
}
