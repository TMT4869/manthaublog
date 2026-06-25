package handler

import (
	"io"
	"net/http"
	"path/filepath"
	"strings"

	"media-service/publisher"
	"media-service/storage"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"go.uber.org/zap"
)

var allowedMIME = map[string]string{
	"image/jpeg": ".jpg",
	"image/png":  ".png",
	"image/webp": ".webp",
	"image/gif":  ".gif",
}

type Handler struct {
	storage   storage.Storage
	publisher *publisher.Publisher
	logger    *zap.Logger
}

func New(s storage.Storage, pub *publisher.Publisher, logger *zap.Logger) *Handler {
	return &Handler{storage: s, publisher: pub, logger: logger}
}

func (h *Handler) Upload(c *gin.Context) {
	userID := c.GetHeader("X-User-ID")

	file, header, err := c.Request.FormFile("file")
	if err != nil {
		c.JSON(400, gin.H{"error": "missing file"})
		return
	}
	defer file.Close()

	if header.Size > 10<<20 { // 10 MB
		c.JSON(400, gin.H{"error": "file too large"})
		return
	}

	// Detect MIME from content, not extension
	buf := make([]byte, 512)
	n, _ := file.Read(buf)
	mime := strings.TrimSpace(strings.Split(http.DetectContentType(buf[:n]), ";")[0])
	ext, ok := allowedMIME[mime]
	if !ok {
		c.JSON(400, gin.H{"error": "unsupported file type"})
		return
	}

	if _, err := file.Seek(0, io.SeekStart); err != nil {
		c.JSON(500, gin.H{"error": "upload failed"})
		return
	}

	filename := uuid.New().String() + ext
	if err := h.storage.Save(file, userID, filename, header.Size); err != nil {
		h.logger.Error("save file failed", zap.Error(err))
		c.JSON(500, gin.H{"error": "upload failed"})
		return
	}

	if err := h.publisher.PublishMediaProcess(publisher.MediaProcessEvent{
		UserID:   userID,
		Filename: filename,
	}); err != nil {
		h.logger.Warn("publish media.process failed", zap.Error(err))
	}

	c.JSON(200, gin.H{
		"url":      h.storage.PublicURL(userID, filename, "original"),
		"filename": filename,
	})
}

func (h *Handler) Delete(c *gin.Context) {
	userID := c.GetHeader("X-User-ID")
	filename := c.Param("filename")

	base := strings.TrimSuffix(filename, filepath.Ext(filename))

	h.storage.Delete(userID, filename)
	for _, v := range []string{"thumbnail", "medium", "large"} {
		if err := h.storage.Delete(userID, v+"_"+base+".jpg"); err != nil {
			h.logger.Warn("delete variant failed",
				zap.String("variant", v),
				zap.String("filename", filename),
				zap.Error(err),
			)
		}
	}

	c.JSON(200, gin.H{"deleted": filename})
}
