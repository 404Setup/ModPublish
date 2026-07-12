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

	app := api.SetupRouter(cfg, stats, worker, mcFetcher)

	log.Printf("Server listening on port %s", cfg.Port)
	log.Fatal(app.Listen(":" + cfg.Port))
}
