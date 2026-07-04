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
