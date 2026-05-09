package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

const ctxUserID = "user_id"

func RequireUserID() gin.HandlerFunc {
	return func(c *gin.Context) {
		userID := c.GetHeader("X-User-Id")
		if userID == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
			return
		}
		c.Set(ctxUserID, userID)
		c.Next()
	}
}
