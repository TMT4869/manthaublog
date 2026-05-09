package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

const ctxUserID = "userID"
const missingUserIDMsg = "missing X-User-ID"

// RequireUserID validates the X-User-ID header (set by the API gateway after JWT verification)
// and stores the user ID in the gin context for downstream handlers.
func RequireUserID() gin.HandlerFunc {
	return func(c *gin.Context) {
		userID := c.GetHeader("X-User-ID")
		if userID == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": missingUserIDMsg})
			return
		}
		c.Set(ctxUserID, userID)
		c.Next()
	}
}
