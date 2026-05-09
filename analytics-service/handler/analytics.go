package handler

import (
	"analytics-service/db"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type Handler struct {
	db     *db.Analytics
	logger *zap.Logger
}

func New(store *db.Analytics, logger *zap.Logger) *Handler {
	return &Handler{db: store, logger: logger}
}

func (h *Handler) internalErr(c *gin.Context, op string, err error) {
	h.logger.Error(op, zap.Error(err))
	c.JSON(http.StatusInternalServerError, gin.H{"error": "internal error"})
}

// POST /api/analytics/read-time
func (h *Handler) TrackReadTime(c *gin.Context) {
	var req struct {
		PostID          string `json:"post_id" binding:"required"`
		ReadTimeSeconds int    `json:"read_time_seconds" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	if err := h.db.AppendReadTime(c.Request.Context(), req.PostID, req.ReadTimeSeconds); err != nil {
		h.internalErr(c, "append read time", err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"ok": true})
}

// GET /api/analytics/posts/:id/stats
func (h *Handler) GetStats(c *gin.Context) {
	stats, err := h.db.GetStats(c.Request.Context(), c.Param("id"))
	if err != nil {
		h.internalErr(c, "get stats", err)
		return
	}
	c.JSON(http.StatusOK, stats)
}

// GET /api/analytics/posts/:id/daily
func (h *Handler) GetDailyStats(c *gin.Context) {
	days, _ := strconv.Atoi(c.DefaultQuery("days", "30"))
	if days <= 0 || days > 365 {
		days = 30
	}
	stats, err := h.db.GetDailyStats(c.Request.Context(), c.Param("id"), days)
	if err != nil {
		h.internalErr(c, "get daily stats", err)
		return
	}
	if stats == nil {
		stats = []db.DailyStat{}
	}
	c.JSON(http.StatusOK, stats)
}
