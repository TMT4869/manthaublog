package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
)

func RequireRole(allowed ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := c.Get("role")
		if !exists {
			c.AbortWithStatusJSON(401, gin.H{"error": "missing role"})
			return
		}
		for _, r := range allowed {
			roleValue, ok := role.(string)
			if ok && strings.EqualFold(roleValue, r) {
				c.Next()
				return
			}
		}
		c.AbortWithStatusJSON(403, gin.H{"error": "forbidden"})
	}
}
