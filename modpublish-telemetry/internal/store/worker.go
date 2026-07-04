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

package store

import (
	"log"
	"sync/atomic"
	"time"
)

type Worker struct {
	db    *DBStore
	stats *StatsCache
}

func NewWorker(db *DBStore, stats *StatsCache) *Worker {
	return &Worker{
		db:    db,
		stats: stats,
	}
}

func (w *Worker) Start() {
	ticker := time.NewTicker(5 * time.Second)
	for {
		<-ticker.C
		w.flush()
	}
}

func (w *Worker) flush() {
	tx, err := w.db.Beginx()
	if err != nil {
		log.Println("Failed to begin transaction:", err)
		return
	}
	defer tx.Rollback()

	stmt, err := tx.Preparex("INSERT INTO stats (category, key, count) VALUES (?, ?, ?) ON CONFLICT(category, key) DO UPDATE SET count = excluded.count")
	if err != nil {
		log.Println("Failed to prepare stmt:", err)
		return
	}
	defer stmt.Close()

	stmt.Exec("total", "total", w.stats.TotalRequests.Load())

	for i := 0; i < 4; i++ {
		val := w.stats.Targets[i].Load()
		if val > 0 {
			stmt.Exec("target", string(rune('0'+i)), val)
		}
	}

	for i := 0; i < 7; i++ {
		val := w.stats.Loaders[i].Load()
		if val > 0 {
			stmt.Exec("loader", string(rune('0'+i)), val)
		}
	}

	w.stats.MCVersions.Range(func(key string, val *atomic.Int64) bool {
		stmt.Exec("mc_version", key, val.Load())
		return true
	})

	if err := tx.Commit(); err != nil {
		log.Println("Failed to commit transaction:", err)
	}
}
