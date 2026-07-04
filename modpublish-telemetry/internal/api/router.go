package api

import (
	"github.com/bytedance/sonic"
	"github.com/gofiber/fiber/v3"
	"github.com/gofiber/fiber/v3/middleware/static"

	"modpublish-telemetry/internal/fetcher"
	"modpublish-telemetry/internal/store"
)

func SetupRouter(stats *store.StatsCache, worker *store.Worker, fetcher *fetcher.MCFetcher) *fiber.App {
	app := fiber.New(fiber.Config{
		JSONEncoder: sonic.Marshal,
		JSONDecoder: sonic.Unmarshal,
	})

	handler := NewHandler(worker, fetcher, stats)

	api := app.Group("/api")

	telemetryGroup := api.Group("/telemetry")
	telemetryGroup.Use(UAMiddleware())
	telemetryGroup.Use(SignatureMiddleware())
	telemetryGroup.Post("/", handler.HandleTelemetry)

	api.Get("/stats", handler.HandleStats)
	api.Get("/mc_versions", handler.HandleMCVersions)

	app.Get("/*", static.New("./public"))

	return app
}
