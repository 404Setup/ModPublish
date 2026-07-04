package main

import (
	"log"

	"modpublish-telemetry/internal/api"
	"modpublish-telemetry/internal/config"
	"modpublish-telemetry/internal/fetcher"
	"modpublish-telemetry/internal/store"
)

func main() {
	cfg := config.LoadConfig()

	db := store.InitDB()
	defer db.Close()

	stats := store.NewStatsCache()
	if err := stats.LoadFromDB(db); err != nil {
		log.Printf("Warning: failed to load initial stats from DB: %v\n", err)
	}

	worker := store.NewWorker(db, stats)
	go worker.Start()

	mcFetcher := fetcher.NewMCFetcher()
	go mcFetcher.Start()

	app := api.SetupRouter(stats, worker, mcFetcher)

	log.Printf("Server listening on port %s", cfg.Port)
	log.Fatal(app.Listen(":" + cfg.Port))
}
