/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package config

import (
	"errors"
	"io/fs"
	"log"
	"os"
	"strings"

	"github.com/bytedance/sonic"
)

type Config struct {
	Port            string   `json:"port"`
	ProxyWhitelist  []string `json:"proxy_whitelist"`
	RateLimitLimit  int      `json:"rate_limit_limit"`
	RateLimitPeriod string   `json:"rate_limit_period"`
}

const configFileName = "config.json"

func LoadConfig() *Config {
	_, err := os.Stat(configFileName)
	if errors.Is(err, fs.ErrNotExist) {
		defaultConfig := &Config{
			Port:            "3000",
			ProxyWhitelist:  []string{"plugins.jetbrains.com", "marketplace.visualstudio.com"},
			RateLimitLimit:  30,
			RateLimitPeriod: "5m",
		}

		data, err := sonic.ConfigFastest.MarshalIndent(defaultConfig, "", "  ")
		if err != nil {
			log.Printf("Warning: failed to marshal default config: %v. Using hardcoded defaults.\n", err)
			return defaultConfig
		}

		if err := os.WriteFile(configFileName, data, 0644); err != nil {
			log.Printf("Warning: failed to write default config file: %v\n", err)
		} else {
			log.Printf("Generated default configuration file: %s\n", configFileName)
		}

		return defaultConfig
	}

	data, err := os.ReadFile(configFileName)
	if err != nil {
		log.Printf("Warning: failed to read config file %s: %v. Using hardcoded defaults.\n", configFileName, err)
		return &Config{
			Port:           "3000",
			ProxyWhitelist: []string{"plugins.jetbrains.com", "marketplace.visualstudio.com"},
		}
	}

	cfg := &Config{}
	if err := sonic.ConfigFastest.Unmarshal(data, cfg); err != nil {
		log.Printf("Warning: failed to parse config file %s: %v. Using hardcoded defaults.\n", configFileName, err)
		return &Config{
			Port:            "3000",
			ProxyWhitelist:  []string{"plugins.jetbrains.com", "marketplace.visualstudio.com"},
			RateLimitLimit:  30,
			RateLimitPeriod: "5m",
		}
	}

	if cfg.Port == "" {
		cfg.Port = "3000"
	}

	if cfg.RateLimitLimit <= 0 {
		cfg.RateLimitLimit = 30
	}

	if cfg.RateLimitPeriod == "" {
		cfg.RateLimitPeriod = "5m"
	}

	for i, w := range cfg.ProxyWhitelist {
		cfg.ProxyWhitelist[i] = strings.TrimSpace(strings.ToLower(w))
	}

	return cfg
}
