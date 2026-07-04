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
