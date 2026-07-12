# Telemetry Server Overview

An anonymous, lightweight backend aggregator to record publishing statistics and proxy CORS requests.

## Purpose

The Telemetry server receives publish payloads containing the mod loader, Minecraft version, and targeted platforms. This data contains **no personal information, file hashes, or developer identifiers**. It helps analyze modding community trends, indicating which loaders (Fabric, Forge, NeoForge, Quilt) are active and which Minecraft versions are targeted.

It also provides a secure, white-listed, loopback-prevented **CORS Proxy** service to bypass CORS restrictions for public pages (e.g. JetBrains Marketplace API) safely, replacing third-party services.

## Running the Server

On first start, the server dynamically generates a `config.json` configuration file in the working directory. You can edit this file to configure the port, whitelisted proxy hosts/URLs, and client IP rate limiting:

```json
{
  "port": "3000",
  "proxy_whitelist": [
    "plugins.jetbrains.com",
    "marketplace.visualstudio.com"
  ],
  "rate_limit_limit": 30,
  "rate_limit_period": "5m"
}
```

Run it by building the executable:

```bash
cd modpublish-telemetry
go run ./cmd/server
```
