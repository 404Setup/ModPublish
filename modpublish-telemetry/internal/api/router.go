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

package api

import (
	"github.com/bytedance/sonic"
	"github.com/gofiber/fiber/v3"
	"github.com/gofiber/fiber/v3/middleware/cors"

	"modpublish-telemetry/internal/config"
	"modpublish-telemetry/internal/fetcher"
	"modpublish-telemetry/internal/store"
)

func SetupRouter(cfg *config.Config, stats *store.StatsCache, worker *store.Worker, fetcher *fetcher.MCFetcher) *fiber.App {
	app := fiber.New(fiber.Config{
		JSONEncoder: sonic.Marshal,
		JSONDecoder: sonic.Unmarshal,
		BodyLimit:   10 * 1024 * 1024,
	})

	app.Use(cors.New())

	handler := NewHandler(worker, fetcher, stats)
	proxyHandler := NewProxyHandler(cfg.ProxyWhitelist, cfg.RateLimitLimit, cfg.RateLimitPeriod)

	api := app.Group("/api")

	telemetryGroup := api.Group("/telemetry")
	telemetryGroup.Use(UAMiddleware())
	telemetryGroup.Use(SignatureMiddleware())
	telemetryGroup.Post("/", handler.HandleTelemetry)

	api.Get("/stats", handler.HandleStats)
	api.Get("/mc_versions", handler.HandleMCVersions)
	api.All("/proxy", proxyHandler.HandleProxy)

	return app
}
