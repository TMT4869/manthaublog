package config

import (
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	Port        string
	StoragePath string // used by LocalStorage fallback + static file route
	RabbitMQURL string

	MinioEndpoint  string
	MinioAccessKey string
	MinioSecretKey string
	MinioBucket    string
	MinioUseSSL    bool
}

func Load() *Config {
	_ = godotenv.Load()
	return &Config{
		Port:        getEnv("SERVER_PORT", "8088"),
		StoragePath: getEnv("STORAGE_PATH", "/app/storage"),
		RabbitMQURL: buildRabbitMQURL(),

		MinioEndpoint:  getEnv("MINIO_ENDPOINT", "minio:9000"),
		MinioAccessKey: getEnv("MINIO_ACCESS_KEY", "minioadmin"),
		MinioSecretKey: getEnv("MINIO_SECRET_KEY", "minioadmin"),
		MinioBucket:    getEnv("MINIO_BUCKET", "media"),
		MinioUseSSL:    getEnv("MINIO_USE_SSL", "false") == "true",
	}
}

func buildRabbitMQURL() string {
	user := getEnv("RABBITMQ_USERNAME", "admin")
	pass := getEnv("RABBITMQ_PASSWORD", "secret")
	host := getEnv("RABBITMQ_HOST", "rabbitmq")
	port := getEnv("RABBITMQ_PORT", "5672")
	return "amqp://" + user + ":" + pass + "@" + host + ":" + port + "/"
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
