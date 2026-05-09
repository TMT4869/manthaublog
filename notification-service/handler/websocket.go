package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"go.uber.org/zap"
	"net/http"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool { return true },
}

// GET /ws — upgrade to WebSocket; RequireUserID middleware must run first.
func (h *Handler) WebSocket(c *gin.Context) {
	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		h.logger.Error("ws upgrade failed", zap.Error(err))
		return
	}
	defer conn.Close()

	userID := c.GetString(ctxUserID)
	h.hub.Register(userID, conn)

	for {
		if _, _, err := conn.ReadMessage(); err != nil {
			h.hub.Unregister(userID)
			return
		}
	}
}
