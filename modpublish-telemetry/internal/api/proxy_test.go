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
	"io"
	"net/http"
	"testing"

	"github.com/gofiber/fiber/v3"
)

type mockRoundTripper func(req *http.Request) (*http.Response, error)

func (m mockRoundTripper) RoundTrip(req *http.Request) (*http.Response, error) {
	return m(req)
}

func TestIsLoopbackOrLocal(t *testing.T) {
	if !isLoopbackOrLocal("localhost") {
		t.Error("expected localhost to be detected as loopback/local")
	}
	if !isLoopbackOrLocal("127.0.0.1") {
		t.Error("expected 127.0.0.1 to be detected as loopback/local")
	}
	if !isLoopbackOrLocal("::1") {
		t.Error("expected ::1 to be detected as loopback/local")
	}
	if !isLoopbackOrLocal("192.168.1.100") {
		t.Error("expected private IP 192.168.1.100 to be detected as loopback/local")
	}
	if isLoopbackOrLocal("plugins.jetbrains.com") {
		t.Error("expected plugins.jetbrains.com to not be detected as loopback/local")
	}
}

func TestHandleProxy_Validation(t *testing.T) {
	app := fiber.New()
	ph := NewProxyHandler([]string{"plugins.jetbrains.com"}, 10, "5m")
	app.All("/api/proxy", ph.HandleProxy)

	req, _ := http.NewRequest("GET", "/api/proxy", nil)
	resp, err := app.Test(req)
	if err != nil {
		t.Fatalf("Test failed: %v", err)
	}
	if resp.StatusCode != http.StatusBadRequest {
		t.Errorf("Expected status 400 for missing url, got %d", resp.StatusCode)
	}

	req, _ = http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fgoogle.com", nil)
	resp, err = app.Test(req)
	if err != nil {
		t.Fatalf("Test failed: %v", err)
	}
	if resp.StatusCode != http.StatusForbidden {
		t.Errorf("Expected status 403 for non-whitelisted host, got %d", resp.StatusCode)
	}

	req, _ = http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2F127.0.0.1%2Fsecret", nil)
	resp, err = app.Test(req)
	if err != nil {
		t.Fatalf("Test failed: %v", err)
	}
	if resp.StatusCode != http.StatusForbidden {
		t.Errorf("Expected status 403 for loopback host, got %d", resp.StatusCode)
	}
}

func TestHandleProxy_ForwardingAndCache(t *testing.T) {
	app := fiber.New()
	ph := NewProxyHandler([]string{"plugins.jetbrains.com"}, 10, "5m")

	callCount := 0
	ph.httpClient.Transport = mockRoundTripper(func(req *http.Request) (*http.Response, error) {
		callCount++
		if req.URL.String() != "https://plugins.jetbrains.com/plugins/list?pluginId=one.pkg.modpublish" {
			t.Errorf("Unexpected request URL: %s", req.URL.String())
		}
		if req.Header.Get("X-Custom-Header") != "value1" {
			t.Errorf("Expected forwarded header X-Custom-Header, got %s", req.Header.Get("X-Custom-Header"))
		}

		respHeader := make(http.Header)
		respHeader.Set("Content-Type", "text/xml")
		respHeader.Set("X-Resp-Header", "value2")
		respHeader.Set("Access-Control-Allow-Origin", "*")

		return &http.Response{
			StatusCode: 200,
			Proto:      "HTTP/1.1",
			ProtoMajor: 1,
			ProtoMinor: 1,
			Header:     respHeader,
			Body:       io.NopCloser(bytes.NewReader([]byte("<xml>test</xml>"))),
		}, nil
	})

	app.All("/api/proxy", ph.HandleProxy)

	req1, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist%3FpluginId%3Done.pkg.modpublish", nil)
	req1.Header.Set("X-Custom-Header", "value1")
	resp1, err := app.Test(req1)
	if err != nil {
		t.Fatalf("First request failed: %v", err)
	}
	if resp1.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp1.StatusCode)
	}
	body1, _ := io.ReadAll(resp1.Body)
	if string(body1) != "<xml>test</xml>" {
		t.Errorf("Expected '<xml>test</xml>', got '%s'", string(body1))
	}
	if resp1.Header.Get("X-Resp-Header") != "value2" {
		t.Errorf("Expected response header X-Resp-Header: value2, got %s", resp1.Header.Get("X-Resp-Header"))
	}
	if resp1.Header.Get("X-Proxy-Cache") != "MISS" {
		t.Errorf("Expected cache MISS header, got %s", resp1.Header.Get("X-Proxy-Cache"))
	}

	req2, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist%3FpluginId%3Done.pkg.modpublish", nil)
	req2.Header.Set("X-Custom-Header", "value1")
	resp2, err := app.Test(req2)
	if err != nil {
		t.Fatalf("Second request failed: %v", err)
	}
	if resp2.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp2.StatusCode)
	}
	body2, _ := io.ReadAll(resp2.Body)
	if string(body2) != "<xml>test</xml>" {
		t.Errorf("Expected '<xml>test</xml>', got '%s'", string(body2))
	}
	if resp2.Header.Get("X-Proxy-Cache") != "HIT" {
		t.Errorf("Expected cache HIT header, got %s", resp2.Header.Get("X-Proxy-Cache"))
	}

	if callCount != 1 {
		t.Errorf("Expected backend server to be called exactly 1 time, but was called %d times", callCount)
	}
}

func TestHandleProxy_RateLimit(t *testing.T) {
	app := fiber.New()
	ph := NewProxyHandler([]string{"plugins.jetbrains.com"}, 2, "5m")
	app.All("/api/proxy", ph.HandleProxy)

	ph.httpClient.Transport = mockRoundTripper(func(req *http.Request) (*http.Response, error) {
		return &http.Response{
			StatusCode: 200,
			Body:       io.NopCloser(bytes.NewReader([]byte("ok"))),
			Header:     make(http.Header),
		}, nil
	})

	req1, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist", nil)
	req1.Header.Set("X-Forwarded-For", "1.2.3.4")
	resp1, _ := app.Test(req1)
	if resp1.StatusCode != http.StatusOK {
		t.Errorf("Request 1 expected 200, got %d", resp1.StatusCode)
	}

	req2, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist", nil)
	req2.Header.Set("X-Forwarded-For", "1.2.3.4")
	resp2, _ := app.Test(req2)
	if resp2.StatusCode != http.StatusOK {
		t.Errorf("Request 2 expected 200, got %d", resp2.StatusCode)
	}

	req3, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist", nil)
	req3.Header.Set("X-Forwarded-For", "1.2.3.4")
	resp3, _ := app.Test(req3)
	if resp3.StatusCode != http.StatusTooManyRequests {
		t.Errorf("Request 3 expected 429, got %d", resp3.StatusCode)
	}

	req4, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist", nil)
	req4.Header.Set("X-Forwarded-For", "5.6.7.8")
	resp4, _ := app.Test(req4)
	if resp4.StatusCode != http.StatusOK {
		t.Errorf("Request 4 expected 200, got %d", resp4.StatusCode)
	}
}

func TestHandleProxy_FullURLWhitelist(t *testing.T) {
	app := fiber.New()
	ph := NewProxyHandler([]string{
		"https://plugins.jetbrains.com/plugins/list?pluginId=one.pkg.modpublish",
		"marketplace.visualstudio.com",
	}, 10, "5m")
	app.All("/api/proxy", ph.HandleProxy)

	ph.httpClient.Transport = mockRoundTripper(func(req *http.Request) (*http.Response, error) {
		return &http.Response{
			StatusCode: 200,
			Body:       io.NopCloser(bytes.NewReader([]byte("ok"))),
			Header:     make(http.Header),
		}, nil
	})

	req1, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist%3FpluginId%3Done.pkg.modpublish", nil)
	resp1, _ := app.Test(req1)
	if resp1.StatusCode != http.StatusOK {
		t.Errorf("Expected 200 for matching whitelisted URL prefix, got %d", resp1.StatusCode)
	}

	req2, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fother%2Fpath", nil)
	resp2, _ := app.Test(req2)
	if resp2.StatusCode != http.StatusForbidden {
		t.Errorf("Expected 403 for non-matching URL prefix, got %d", resp2.StatusCode)
	}

	req3, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fmarketplace.visualstudio.com%2F_apis%2Fpublic%2Fgallery", nil)
	resp3, _ := app.Test(req3)
	if resp3.StatusCode != http.StatusOK {
		t.Errorf("Expected 200 for whitelisted host-only path, got %d", resp3.StatusCode)
	}
}

func TestHandleProxy_BodySizeLimits(t *testing.T) {
	app := fiber.New(fiber.Config{
		BodyLimit: 10 * 1024 * 1024,
	})
	ph := NewProxyHandler([]string{"plugins.jetbrains.com"}, 10, "5m")
	app.All("/api/proxy", ph.HandleProxy)

	// Case 1: Client Request Body over 5MB
	largeBody := make([]byte, 5*1024*1024+1)
	req1, _ := http.NewRequest("POST", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist", bytes.NewReader(largeBody))
	resp1, err := app.Test(req1)
	if err != nil {
		t.Fatalf("Case 1 test failed: %v", err)
	}
	if resp1.StatusCode != http.StatusRequestEntityTooLarge {
		t.Errorf("Expected 413 for client request body > 5MB, got %d", resp1.StatusCode)
	}

	// Case 2: Server Response Body over 5MB
	ph.httpClient.Transport = mockRoundTripper(func(req *http.Request) (*http.Response, error) {
		largeServerResp := make([]byte, 5*1024*1024+1)
		return &http.Response{
			StatusCode: 200,
			Body:       io.NopCloser(bytes.NewReader(largeServerResp)),
			Header:     make(http.Header),
		}, nil
	})

	req2, _ := http.NewRequest("GET", "/api/proxy?url=https%3A%2F%2Fplugins.jetbrains.com%2Fplugins%2Flist", nil)
	resp2, err := app.Test(req2)
	if err != nil {
		t.Fatalf("Case 2 test failed: %v", err)
	}
	if resp2.StatusCode != http.StatusBadGateway {
		t.Errorf("Expected 502 for target server response body > 5MB, got %d", resp2.StatusCode)
	}
}
