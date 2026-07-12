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
	"bytes"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"

	"github.com/gofiber/fiber/v3"
	"github.com/llxisdsh/pb"
)

const maxBodySize = 5 * 1024 * 1024

type CachedResponse struct {
	StatusCode int
	Body       []byte
	Headers    map[string][]string
	ExpiresAt  time.Time
}

type ProxyCache struct {
	items *pb.MapOf[string, CachedResponse]
}

func NewProxyCache() *ProxyCache {
	pc := &ProxyCache{
		items: pb.NewMapOf[string, CachedResponse](),
	}
	go pc.cleanupLoop()
	return pc
}

func (pc *ProxyCache) cleanupLoop() {
	ticker := time.NewTicker(10 * time.Minute)
	for range ticker.C {
		now := time.Now()
		pc.items.Range(func(k string, v CachedResponse) bool {
			if now.After(v.ExpiresAt) {
				pc.items.Delete(k)
			}
			return true
		})
	}
}

func (pc *ProxyCache) Get(key string) (CachedResponse, bool) {
	item, found := pc.items.Load(key)
	if !found {
		return CachedResponse{}, false
	}
	if time.Now().After(item.ExpiresAt) {
		return CachedResponse{}, false
	}
	return item, true
}

func (pc *ProxyCache) Set(key string, resp CachedResponse) {
	pc.items.Store(key, resp)
}

type ipLimiter struct {
	sync.Mutex
	requests map[string][]time.Time
}

func newIPLimiter(period time.Duration) *ipLimiter {
	l := &ipLimiter{
		requests: make(map[string][]time.Time),
	}
	go l.cleanupLoop(period)
	return l
}

func (l *ipLimiter) cleanupLoop(period time.Duration) {
	ticker := time.NewTicker(period * 2)
	for range ticker.C {
		l.Lock()
		now := time.Now()
		cutoff := now.Add(-period)
		for ip, times := range l.requests {
			var validTimes []time.Time
			for _, t := range times {
				if t.After(cutoff) {
					validTimes = append(validTimes, t)
				}
			}
			if len(validTimes) == 0 {
				delete(l.requests, ip)
			} else {
				l.requests[ip] = validTimes
			}
		}
		l.Unlock()
	}
}

func (l *ipLimiter) Allow(ip string, limit int, period time.Duration) bool {
	l.Lock()
	defer l.Unlock()

	now := time.Now()
	cutoff := now.Add(-period)

	times := l.requests[ip]
	var validTimes []time.Time
	for _, t := range times {
		if t.After(cutoff) {
			validTimes = append(validTimes, t)
		}
	}

	if len(validTimes) >= limit {
		l.requests[ip] = validTimes
		return false
	}

	l.requests[ip] = append(validTimes, now)
	return true
}

type ProxyHandler struct {
	whitelist       []string
	proxyCache      *ProxyCache
	httpClient      *http.Client
	limiter         *ipLimiter
	rateLimitLimit  int
	rateLimitPeriod time.Duration
}

func NewProxyHandler(whitelist []string, limit int, periodStr string) *ProxyHandler {
	period, err := time.ParseDuration(periodStr)
	if err != nil {
		period = 5 * time.Minute
	}
	if limit <= 0 {
		limit = 30
	}

	ph := &ProxyHandler{
		whitelist:       whitelist,
		proxyCache:      NewProxyCache(),
		limiter:         newIPLimiter(period),
		rateLimitLimit:  limit,
		rateLimitPeriod: period,
	}

	ph.httpClient = &http.Client{
		Timeout: 15 * time.Second,
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			if len(via) >= 10 {
				return errors.New("stopped after 10 redirects")
			}
			if !ph.isAllowed(req.URL.String(), req.URL) {
				return fmt.Errorf("redirect target url %s not allowed by whitelist", req.URL.String())
			}
			if isLoopbackOrLocal(req.URL.Host) {
				return errors.New("redirect to loopback/local address not allowed")
			}
			return nil
		},
	}

	return ph
}

func (ph *ProxyHandler) isAllowed(targetURLStr string, targetURL *url.URL) bool {
	targetHost := targetURL.Host
	if h, _, err := net.SplitHostPort(targetHost); err == nil {
		targetHost = h
	}
	targetHost = strings.TrimSpace(strings.ToLower(targetHost))

	for _, entry := range ph.whitelist {
		entryLower := strings.TrimSpace(strings.ToLower(entry))
		if strings.Contains(entryLower, "://") {
			if strings.HasPrefix(strings.ToLower(targetURLStr), entryLower) {
				return true
			}
		} else {
			entryHost := entryLower
			if h, _, err := net.SplitHostPort(entryHost); err == nil {
				entryHost = h
			}
			if targetHost == entryHost {
				return true
			}
		}
	}
	return false
}

func isLoopbackOrLocal(host string) bool {
	h, _, err := net.SplitHostPort(host)
	if err != nil {
		h = host
	}
	h = strings.TrimSpace(strings.ToLower(h))
	if h == "localhost" || h == "127.0.0.1" || h == "::1" {
		return true
	}

	ips, err := net.LookupIP(h)
	if err != nil {
		return true
	}

	for _, ip := range ips {
		if ip.IsLoopback() || ip.IsPrivate() || ip.IsLinkLocalUnicast() || ip.IsUnspecified() {
			return true
		}
	}
	return false
}

func (ph *ProxyHandler) HandleProxy(c fiber.Ctx) error {
	clientIP := c.Get("X-Forwarded-For")
	if clientIP == "" {
		clientIP = c.Get("X-Real-IP")
	}
	if clientIP == "" {
		clientIP = c.IP()
	}
	if idx := strings.Index(clientIP, ","); idx != -1 {
		clientIP = strings.TrimSpace(clientIP[:idx])
	}

	if !ph.limiter.Allow(clientIP, ph.rateLimitLimit, ph.rateLimitPeriod) {
		return c.Status(fiber.StatusTooManyRequests).SendString("Rate limit exceeded")
	}

	targetURLStr := c.Query("url")
	if targetURLStr == "" {
		return c.Status(fiber.StatusBadRequest).SendString("Missing url query parameter")
	}

	targetURL, err := url.Parse(targetURLStr)
	if err != nil || targetURL.Scheme == "" || targetURL.Host == "" {
		return c.Status(fiber.StatusBadRequest).SendString("Invalid target url")
	}

	scheme := strings.ToLower(targetURL.Scheme)
	if scheme != "http" && scheme != "https" {
		return c.Status(fiber.StatusBadRequest).SendString("Only http and https schemes are allowed")
	}

	if !ph.isAllowed(targetURLStr, targetURL) {
		return c.Status(fiber.StatusForbidden).SendString("URL not allowed by whitelist")
	}

	if isLoopbackOrLocal(targetURL.Host) {
		return c.Status(fiber.StatusForbidden).SendString("Loopback/local URL forwarding is not allowed")
	}

	method := c.Method()
	cacheKey := method + ":" + targetURLStr

	if method == fiber.MethodGet {
		if cached, found := ph.proxyCache.Get(cacheKey); found {
			for k, values := range cached.Headers {
				for _, val := range values {
					c.Response().Header.Add(k, val)
				}
			}
			c.Set("X-Proxy-Cache", "HIT")
			return c.Status(cached.StatusCode).Send(cached.Body)
		}
	}

	if c.Request().Header.ContentLength() > maxBodySize || len(c.Body()) > maxBodySize {
		return c.Status(fiber.StatusRequestEntityTooLarge).SendString("Request body too large (max 5MB)")
	}

	req, err := http.NewRequest(method, targetURLStr, bytes.NewReader(c.Body()))
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).SendString(fmt.Sprintf("Failed to create proxy request: %v", err))
	}

	for key, value := range c.Request().Header.All() {
		k := string(key)
		kl := strings.ToLower(k)
		if kl == "host" || kl == "connection" || kl == "keep-alive" ||
			kl == "proxy-authenticate" || kl == "proxy-authorization" ||
			kl == "te" || kl == "trailers" || kl == "transfer-encoding" ||
			kl == "upgrade" {
			continue
		}
		req.Header.Add(k, string(value))
	}

	resp, err := ph.httpClient.Do(req)
	if err != nil {
		return c.Status(fiber.StatusBadGateway).SendString(fmt.Sprintf("Proxy request failed: %v", err))
	}
	defer resp.Body.Close()

	if resp.ContentLength > maxBodySize {
		return c.Status(fiber.StatusBadGateway).SendString("Target response body too large (max 5MB)")
	}

	limitReader := io.LimitReader(resp.Body, maxBodySize+1)
	bodyBytes, err := io.ReadAll(limitReader)
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).SendString(fmt.Sprintf("Failed to read proxy response: %v", err))
	}
	if len(bodyBytes) > maxBodySize {
		return c.Status(fiber.StatusBadGateway).SendString("Target response body too large (max 5MB)")
	}

	for k, values := range resp.Header {
		kl := strings.ToLower(k)
		if kl == "connection" || kl == "keep-alive" || kl == "proxy-authenticate" ||
			kl == "proxy-authorization" || kl == "te" || kl == "trailers" ||
			kl == "transfer-encoding" || kl == "upgrade" ||
			strings.HasPrefix(kl, "access-control-") {
			continue
		}
		for _, val := range values {
			c.Response().Header.Add(k, val)
		}
	}

	if resp.StatusCode == http.StatusOK && method == fiber.MethodGet {
		cached := CachedResponse{
			StatusCode: resp.StatusCode,
			Body:       bodyBytes,
			Headers:    make(map[string][]string),
			ExpiresAt:  time.Now().Add(6 * time.Hour),
		}
		for k, values := range resp.Header {
			kl := strings.ToLower(k)
			if kl == "connection" || kl == "keep-alive" || kl == "proxy-authenticate" ||
				kl == "proxy-authorization" || kl == "te" || kl == "trailers" ||
				kl == "transfer-encoding" || kl == "upgrade" ||
				strings.HasPrefix(kl, "access-control-") {
				continue
			}
			cached.Headers[k] = values
		}
		ph.proxyCache.Set(cacheKey, cached)
	}

	c.Set("X-Proxy-Cache", "MISS")
	return c.Status(resp.StatusCode).Send(bodyBytes)
}
