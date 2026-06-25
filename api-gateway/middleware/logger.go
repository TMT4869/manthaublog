package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"go.uber.org/zap"
)

func Logger() gin.HandlerFunc {
	logger, _ := zap.NewProduction()
	return func(c *gin.Context) {
		traceID := uuid.New().String()
		c.Set("trace_id", traceID)
		c.Request.Header.Set("X-Trace-ID", traceID)

		start := time.Now()
		c.Next()

		logger.Info("request",
			zap.String("trace_id", traceID),
			zap.String("method", c.Request.Method),
			zap.String("path", c.Request.URL.Path),
			zap.Int("status", c.Writer.Status()),
			zap.Duration("latency", time.Since(start)),
			zap.String("ip", c.ClientIP()),
		)
	}
}
