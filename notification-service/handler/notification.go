package handler

import (
	"net/http"
	"strconv"

	"notification-service/db"
	"notification-service/hub"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type Handler struct {
	store  db.Storer
	hub    *hub.Hub
	logger *zap.Logger
}

func New(store db.Storer, h *hub.Hub, logger *zap.Logger) *Handler {
	return &Handler{store: store, hub: h, logger: logger}
}

func (h *Handler) internalErr(c *gin.Context, op string, err error) {
	h.logger.Error(op, zap.Error(err))
	c.JSON(http.StatusInternalServerError, gin.H{"error": "internal error"})
}

// GET /api/notifications
func (h *Handler) List(c *gin.Context) {
	userID := c.GetString(ctxUserID)
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	offset, _ := strconv.Atoi(c.DefaultQuery("offset", "0"))

	notifications, err := h.store.List(c.Request.Context(), userID, limit, offset)
	if err != nil {
		h.internalErr(c, "list notifications", err)
		return
	}
	if notifications == nil {
		notifications = []db.Notification{}
	}
	c.JSON(http.StatusOK, notifications)
}

// PUT /api/notifications/:id/read
func (h *Handler) MarkRead(c *gin.Context) {
	if err := h.store.MarkRead(c.Request.Context(), c.Param("id"), c.GetString(ctxUserID)); err != nil {
		h.internalErr(c, "mark read", err)
	} else {
		c.Status(http.StatusNoContent)
	}
}

// PUT /api/notifications/read-all
func (h *Handler) MarkAllRead(c *gin.Context) {
	if err := h.store.MarkAllRead(c.Request.Context(), c.GetString(ctxUserID)); err != nil {
		h.internalErr(c, "mark all read", err)
	} else {
		c.Status(http.StatusNoContent)
	}
}
