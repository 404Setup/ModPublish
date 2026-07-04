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
	"sync/atomic"

	"github.com/llxisdsh/pb"
)

type StatsCache struct {
	TotalRequests atomic.Int64
	Targets       [4]atomic.Int64
	Loaders       [7]atomic.Int64
	MCVersions    *pb.MapOf[string, *atomic.Int64]
}

func NewStatsCache() *StatsCache {
	return &StatsCache{
		MCVersions: pb.NewMapOf[string, *atomic.Int64](),
	}
}

func (s *StatsCache) LoadFromDB(db *DBStore) error {
	var total int64
	db.Get(&total, "SELECT count FROM stats WHERE category = 'total' AND key = 'total'")
	s.TotalRequests.Store(total)

	rows, err := db.Queryx("SELECT category, key, count FROM stats")
	if err == nil {
		defer rows.Close()
		for rows.Next() {
			var category, key string
			var count int64
			if err := rows.Scan(&category, &key, &count); err == nil {
				switch category {
				case "target":
					if len(key) == 1 && key[0] >= '0' && key[0] <= '3' {
						s.Targets[key[0]-'0'].Store(count)
					}
				case "loader":
					if len(key) == 1 && key[0] >= '0' && key[0] <= '6' {
						s.Loaders[key[0]-'0'].Store(count)
					}
				case "mc_version":
					val := &atomic.Int64{}
					val.Store(count)
					s.MCVersions.Store(key, val)
				}
			}
		}
	}

	return nil
}

func incrementPbMap(m *pb.MapOf[string, *atomic.Int64], key string, count int64) {
	v, ok := m.Load(key)
	if !ok {
		newVal := &atomic.Int64{}
		v, _ = m.LoadOrStore(key, newVal)
	}
	v.Add(count)
}

func (s *StatsCache) Increment(requests int64, targets, loaders, mcVersions []string) {
	s.TotalRequests.Add(requests)
	for _, t := range targets {
		if len(t) == 1 && t[0] >= '0' && t[0] <= '3' {
			s.Targets[t[0]-'0'].Add(1)
		}
	}
	for _, l := range loaders {
		if len(l) == 1 && l[0] >= '0' && l[0] <= '6' {
			s.Loaders[l[0]-'0'].Add(1)
		}
	}
	for _, mc := range mcVersions {
		incrementPbMap(s.MCVersions, mc, 1)
	}
}
