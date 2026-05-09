package router

import (
	"media-service/handler"

	"github.com/gin-gonic/gin"
)

func Setup(h *handler.Handler, storagePath string) *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())

	auth := handler.RequireUserID()

	api := r.Group("/api/media", auth)
	api.POST("/upload", h.Upload)
	api.DELETE("/:filename", h.Delete)

	// Serve uploaded files: /media/{userID}/{filename}
	r.Static("/media", storagePath)

	return r
}
