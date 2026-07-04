package config

import (
	"os"
)

type Config struct {
	Port string
}

func LoadConfig() *Config {
	port := os.Getenv("PORT")
	if port == "" {
		port = "3000"
	}

	return &Config{
		Port: port,
	}
}
