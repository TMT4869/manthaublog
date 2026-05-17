package router

import (
	"api-gateway/config"
	"api-gateway/middleware"
	"api-gateway/proxy"

	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

func Setup(rdb *redis.Client, cfg *config.Config) *gin.Engine {
	r := gin.New()
	r.Use(middleware.Logger())
	r.Use(middleware.CORS())

	// Public routes — no JWT required
	r.Any("/auth/*path", proxy.NewReverseProxy(cfg.AuthServiceURL))
	r.GET("/api/categories", proxy.NewReverseProxy(cfg.PostServiceURL))
	r.GET("/api/categories/*path", proxy.NewReverseProxy(cfg.PostServiceURL))

	// Protected routes
	protected := r.Group("/api")
	protected.Use(middleware.JWTMiddleware(cfg.JWTSecret))
	protected.Use(middleware.RateLimitMiddleware(rdb))
	{
		protected.Any("/users/*path",         proxy.NewReverseProxy(cfg.UserServiceURL))
		protected.Any("/posts/*path",         proxy.NewReverseProxy(cfg.PostServiceURL))
		protected.Any("/comments/*path",      proxy.NewReverseProxy(cfg.CommentServiceURL))
		protected.Any("/search/*path",        proxy.NewReverseProxy(cfg.SearchServiceURL))
		protected.Any("/notifications/*path", proxy.NewReverseProxy(cfg.NotificationServiceURL))
		protected.Any("/analytics/*path",     proxy.NewReverseProxy(cfg.AnalyticsServiceURL))
		protected.Any("/media/*path",         proxy.NewReverseProxy(cfg.MediaServiceURL))
	}

	// Admin-only routes — require role admin/super_admin
	admin := r.Group("/api/admin")
	admin.Use(middleware.JWTMiddleware(cfg.JWTSecret))
	admin.Use(middleware.RequireRole("admin", "super_admin"))
	admin.Use(middleware.RateLimitMiddleware(rdb))
	{
		admin.Any("/posts/*path", proxy.NewReverseProxy(cfg.PostServiceURL))
		admin.Any("/categories", proxy.NewReverseProxy(cfg.PostServiceURL))
		admin.Any("/categories/*path", proxy.NewReverseProxy(cfg.PostServiceURL))
	}

	return r
}
