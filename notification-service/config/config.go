package config

import "os"

type Config struct {
	Port           string
	DatabaseURL    string
	RedisAddr      string
	RedisPassword  string
	RabbitMQURL    string
	AuthServiceURL string
	SMTPHost       string
	SMTPPort       string
	SMTPUser       string
	SMTPPassword   string
}

func Load() *Config {
	return &Config{
		Port:           getEnv("SERVER_PORT", "8086"),
		DatabaseURL:    getEnv("DATABASE_URL", "postgres://postgres:secret@postgres:5432/notification_db"),
		RedisAddr:      getEnv("REDIS_HOST", "redis") + ":" + getEnv("REDIS_PORT", "6379"),
		RedisPassword:  getEnv("REDIS_PASSWORD", ""),
		RabbitMQURL:    buildRabbitMQURL(),
		AuthServiceURL: getEnv("AUTH_SERVICE_URL", "http://auth-service:8081"),
		SMTPHost:       getEnv("SMTP_HOST", "smtp.gmail.com"),
		SMTPPort:       getEnv("SMTP_PORT", "587"),
		SMTPUser:       getEnv("SMTP_USER", ""),
		SMTPPassword:   getEnv("SMTP_PASSWORD", ""),
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
