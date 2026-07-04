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
	"crypto/sha256"
	"encoding/hex"
	"hash"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/gofiber/fiber/v3"
)

var hashPool = sync.Pool{
	New: func() any {
		return sha256.New()
	},
}

func SignatureMiddleware() fiber.Handler {
	return func(c fiber.Ctx) error {
		timestampStr := c.Get("X-Timestamp")
		timestamp, err := strconv.ParseInt(timestampStr, 10, 64)
		if err != nil {
			return c.Status(fiber.StatusBadRequest).SendString("Invalid or missing X-Timestamp")
		}

		now := time.Now().Unix()
		if now-timestamp > 15 || timestamp-now > 15 {
			return c.Status(fiber.StatusForbidden).SendString("Request expired")
		}

		clientSignature := c.Get("X-Signed")
		if clientSignature == "" {
			return c.Status(fiber.StatusBadRequest).SendString("Missing X-Signed header")
		}

		rawBody := c.Body()

		h := hashPool.Get().(hash.Hash)
		h.Reset()
		h.Write([]byte(timestampStr))
		h.Write(rawBody)
		sum := h.Sum(nil)
		expectedSignature := hex.EncodeToString(sum)
		hashPool.Put(h)

		if clientSignature != expectedSignature {
			return c.Status(fiber.StatusForbidden).SendString("Invalid signature")
		}

		return c.Next()
	}
}

func UAMiddleware() fiber.Handler {
	return func(c fiber.Ctx) error {
		ua := c.Get("User-Agent")
		if strings.Contains(ua, "modpublish-vsc") {
			c.Locals("resolved_ua", "modpublish vscode")
		} else if strings.Contains(ua, "modpublish") {
			c.Locals("resolved_ua", "modpublish intellij")
		} else {
			return c.Status(fiber.StatusForbidden).SendString("Invalid Request")
		}
		return c.Next()
	}
}
