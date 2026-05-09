package handler

import "github.com/gin-gonic/gin"

func RequireUserID() gin.HandlerFunc {
	return func(c *gin.Context) {
		if c.GetHeader("X-User-ID") == "" {
			c.AbortWithStatusJSON(401, gin.H{"error": "missing X-User-ID header"})
			return
		}
		c.Next()
	}
}
