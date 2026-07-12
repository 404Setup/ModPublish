# Telemetry API Reference

The backend serves stateless API routes under `/api`. Requests are fast and resource-efficient.

## 1. Submit Telemetry

* **Endpoint:** `POST /api/telemetry`

Used by IDE extensions to submit a completed publishing action. Requires special signature validation headers to verify request integrity.

* **Headers:**
  * `User-Agent`: Must contain `modpublish` (or `modpublish-vsc`)
  * `X-Timestamp`: Epoch timestamp in seconds
  * `X-Signed`: SHA-256 hash of (Timestamp + Body)

* **Request Body (JSON):**
  ```json
  {
    "target": 0,       // 0: Modrinth, 1: CurseForge, 2: GitHub, 3: GitLab
    "loader": 0,       // 0: Fabric, 1: Quilt, 2: Forge, 3: NeoForge, etc.
    "mc_version": "1.20.4"
  }
  ```

## 2. Get Statistics

* **Endpoint:** `GET /api/stats`

Retrieves aggregated publish counts, loaders distribution, Minecraft version counts, and targets distribution. Accepts optional query parameters to filter results.

* **Parameters (Optional):**
  * `target`: Comma-separated list of target IDs (e.g. `0,1`)
  * `loader`: Comma-separated list of loader IDs (e.g. `0,2`)
  * `mc_version`: Comma-separated list of Minecraft versions

## 3. Get MC Versions List

* **Endpoint:** `GET /api/mc_versions`

Returns a simple array string of all Minecraft versions recorded in the database, ordered from newest to oldest.

## 4. CORS Proxy (Bypass CORS)

* **Endpoint:** `ALL /api/proxy`

Proxies requests to whitelisted hosts while bypassing browser CORS constraints. Responses are cached to reduce upstream load.

* **Query Parameters:**
  * `url`: The encoded target URL (e.g. `https%3A%2F%2Fplugins.jetbrains.com%2F...`)

* **Security Features:**
  * **Flexible Whitelisting:** Hostnames match exactly (e.g. `marketplace.visualstudio.com`). Entries starting with `http://` or `https://` perform full URL prefix matching, allowing locking down requests to specific endpoints.
  * **Loopback Blocking:** Requests to `localhost`, `127.0.0.1`, private subnets (e.g., `192.168.0.0/16`), or link-local IPs are blocked.
  * **Redirect Safety:** Redirect hops are followed up to 10 times but validated against the whitelist and loopback checks at each hop.
  * **Body Size Limits (Max 5MB):** To prevent memory exhaustion attacks, both the incoming client request body and the outgoing target server response body are limited to a maximum size of **5MB**. Exceeding this size returns `413 Request Entity Too Large` or `502 Bad Gateway` respectively.

* **Rate Limiting:** Single-IP rate limiting is enforced to prevent abuse. By default, a client IP is allowed up to **30 requests every 5 minutes**, which is configurable via the configuration file. Exceeding this limit returns a `429 Too Many Requests` status.

* **Caching:** Successful (`200 OK`) `GET` responses are cached for **6 hours**. Cached responses include a response header `X-Proxy-Cache: HIT`.
