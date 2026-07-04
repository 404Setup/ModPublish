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
	"fmt"
	"strings"
	"sync"
	"sync/atomic"

	"github.com/bytedance/sonic"
	"github.com/gofiber/fiber/v3"
	"github.com/llxisdsh/pb"

	"modpublish-telemetry/internal/fetcher"
	"modpublish-telemetry/internal/model"
	"modpublish-telemetry/internal/store"
)

type Handler struct {
	worker  *store.Worker
	fetcher *fetcher.MCFetcher
	stats   *store.StatsCache
}

func NewHandler(worker *store.Worker, fetcher *fetcher.MCFetcher, stats *store.StatsCache) *Handler {
	return &Handler{
		worker:  worker,
		fetcher: fetcher,
		stats:   stats,
	}
}

var reqPool = sync.Pool{
	New: func() any {
		return &model.TelemetryRequest{
			MinecraftVersion: make([]string, 0, 8),
			PublishTarget:    make([]string, 0, 4),
			Loader:           make([]string, 0, 8),
		}
	},
}

var targetKeys = []string{"0", "1", "2", "3"}
var loaderKeys = []string{"0", "1", "2", "3", "4", "5", "6"}

func (h *Handler) HandleTelemetry(c fiber.Ctx) error {
	_, ok := c.Locals("resolved_ua").(string)
	if !ok {
		return c.Status(fiber.StatusInternalServerError).SendString("UA resolving failed")
	}

	rawBody := c.Body()

	req := reqPool.Get().(*model.TelemetryRequest)
	req.MinecraftVersion = req.MinecraftVersion[:0]
	req.PublishTarget = req.PublishTarget[:0]
	req.Loader = req.Loader[:0]
	defer reqPool.Put(req)

	if err := sonic.ConfigFastest.Unmarshal(rawBody, req); err != nil {
		return c.Status(fiber.StatusBadRequest).SendString("Invalid JSON body")
	}

	for _, target := range req.PublishTarget {
		if len(target) != 1 || target[0] < '0' || target[0] > '3' {
			return c.Status(fiber.StatusBadRequest).SendString(fmt.Sprintf("Invalid publishTarget: %s", target))
		}
	}

	for _, loader := range req.Loader {
		if len(loader) != 1 || loader[0] < '0' || loader[0] > '6' {
			return c.Status(fiber.StatusBadRequest).SendString(fmt.Sprintf("Invalid loader: %s", loader))
		}
	}

	for _, mc := range req.MinecraftVersion {
		if !h.fetcher.IsValidMCVersion(mc) {
			return c.Status(fiber.StatusBadRequest).SendString(fmt.Sprintf("Invalid minecraftVersion: %s", mc))
		}
	}

	h.stats.Increment(1, req.PublishTarget, req.Loader, req.MinecraftVersion)

	return c.SendStatus(fiber.StatusOK)
}

func (h *Handler) HandleStats(c fiber.Ctx) error {
	targetQuery := c.Query("target")
	loaderQuery := c.Query("loader")
	mcQuery := c.Query("mc_version")

	targetsMap := make(map[string]int64)
	if targetQuery == "" {
		for i := 0; i < 4; i++ {
			targetsMap[targetKeys[i]] = h.stats.Targets[i].Load()
		}
	} else {
		for _, q := range strings.Split(targetQuery, ",") {
			if len(q) == 1 && q[0] >= '0' && q[0] <= '3' {
				targetsMap[q] = h.stats.Targets[q[0]-'0'].Load()
			}
		}
	}

	loadersMap := make(map[string]int64)
	if loaderQuery == "" {
		for i := 0; i < 7; i++ {
			loadersMap[loaderKeys[i]] = h.stats.Loaders[i].Load()
		}
	} else {
		for _, q := range strings.Split(loaderQuery, ",") {
			if len(q) == 1 && q[0] >= '0' && q[0] <= '6' {
				loadersMap[q] = h.stats.Loaders[q[0]-'0'].Load()
			}
		}
	}

	response := fiber.Map{
		"total_requests":     h.stats.TotalRequests.Load(),
		"publish_targets":    targetsMap,
		"loaders":            loadersMap,
		"minecraft_versions": pbMapToMap(h.stats.MCVersions, mcQuery),
	}

	return c.JSON(response)
}

func pbMapToMap(m *pb.MapOf[string, *atomic.Int64], queryParam string) map[string]int64 {
	result := make(map[string]int64)
	if queryParam == "" {
		m.Range(func(k string, v *atomic.Int64) bool {
			result[k] = v.Load()
			return true
		})
		return result
	}

	items := strings.Split(queryParam, ",")
	for _, item := range items {
		if val, exists := m.Load(item); exists {
			result[item] = val.Load()
		}
	}
	return result
}

func (h *Handler) HandleMCVersions(c fiber.Ctx) error {
	return c.JSON(h.fetcher.GetValidVersions())
}
