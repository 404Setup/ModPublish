package store

import (
	"log"

	"github.com/jmoiron/sqlx"
	_ "modernc.org/sqlite"
)

type DBStore struct {
	*sqlx.DB
}

func InitDB() *DBStore {
	db, err := sqlx.Connect("sqlite", "telemetry.db?_pragma=journal_mode(WAL)&_pragma=synchronous(NORMAL)")
	if err != nil {
		log.Fatalln(err)
	}

	schema := `
	CREATE TABLE IF NOT EXISTS stats (
		category TEXT,
		key TEXT,
		count INTEGER,
		PRIMARY KEY(category, key)
	) WITHOUT ROWID;
	`
	db.MustExec(schema)
	return &DBStore{DB: db}
}
