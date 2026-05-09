package config

import "os"

type Config struct {
	Port                   string
	JWTSecret              string
	RedisAddr              string
	AuthServiceURL         string
	UserServiceURL         string
	PostServiceURL         string
	CommentServiceURL      string
	SearchServiceURL       string
	NotificationServiceURL string
	AnalyticsServiceURL    string
	MediaServiceURL        string
}

func Load() *Config {
	return &Config{
		Port:                   getEnv("PORT", "8080"),
		JWTSecret:              getEnv("JWT_SECRET", "secret"),
		RedisAddr:              getEnv("REDIS_ADDR", "localhost:6379"),
		AuthServiceURL:         getEnv("AUTH_SERVICE_URL", "http://localhost:8081"),
		UserServiceURL:         getEnv("USER_SERVICE_URL", "http://localhost:8082"),
		PostServiceURL:         getEnv("POST_SERVICE_URL", "http://localhost:8083"),
		CommentServiceURL:      getEnv("COMMENT_SERVICE_URL", "http://localhost:8084"),
		SearchServiceURL:       getEnv("SEARCH_SERVICE_URL", "http://localhost:8085"),
		NotificationServiceURL: getEnv("NOTIFICATION_SERVICE_URL", "http://localhost:8086"),
		AnalyticsServiceURL:    getEnv("ANALYTICS_SERVICE_URL", "http://localhost:8087"),
		MediaServiceURL:        getEnv("MEDIA_SERVICE_URL", "http://localhost:8088"),
	}
}

func getEnv(key, defaultVal string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return defaultVal
}
