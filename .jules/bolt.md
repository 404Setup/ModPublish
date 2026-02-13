## 2025-05-28 - [Implicit Map Overhead]
**Learning:** The codebase heavily relied on `MutableMap<String, Any>` for JSON data processing (e.g. `VersionProcessor.kt`), which causes significant memory overhead and type unsafety compared to data classes.
**Action:** Prefer defining lightweight data classes for JSON models, even for internal/private usage, to improve both performance and maintainability.
