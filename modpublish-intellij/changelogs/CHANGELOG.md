## v1.12.0

### Fix
- Compatible with Modrinth's new Environment changes

### Refactor
- Removed okhttp, fully switched to ktor
- When publishing from CurseForge, Markdown input will be automatically translated to HTML (testing)
- The appearance of the dependency manager has been redesigned

### Performance
- Optimize regex compilation in ModVersion.kt
- Optimize collection filtering in DependencyManagerPanel

### Chore
- More i18n
- Bump minecraft version list

## v1.11.3

### Refactor

- SKToml has been replaced with a completely refactored SJToml. SJToml is also lightweight, but more powerful than SKToml, and has more optimizations applied.

### Performance

- Remove some stream list/map operations
- optimize loaders array iteration in CurseForgeAPI
- Optimize UI performance by avoiding string-based enum lookup in PublishModDialog
- Optimize redundant string allocation in VersionProcessor map loop
- optimize file parsing in PublishModAction to prevent UI freezing
- optimize intermediate collections in PublishModDialog

### Fix

- I tried a different method to fix the problem that v1.11.2 was attempting to fix
- Version checking fails when a comma follows a space in maven style version constraints (Github/41)
- In the version constraint `[26.1,26.2)`, version 26.2 is unexpectedly and automatically selected (Github/42)
- In version constraints, `> 1.20.1 < 1.21.1` cannot be detected. (Github/43)
- In the version constraint `[26.1, 1.21.1, 26.2)`, the version detector incorrectly selects 26.1, 1.21.1, and 26.2 (Github/44)

### Chore

- Bump minecraft version list

## v1.11.2

### Fix

- CurseForge's changelog is missing line breaks. After implementing this fix, users on Linux platforms and those using LF line delimiters may encounter unexpected issues. Since I do not have a Linux or macOS graphical device, this fix has only been tested on Windows. If you encounter any problems, please open any issues section to let me know.

## v1.11.1

### Fix

- Fixed an issue where component caching caused the Minecraft version list to not update correctly after using the "Update Version List" and "Clear Version List Cache" features.

## v1.11.0

### Feature

- Curseforge StudioAPI is now optional. Other methods will be used to completely eliminate it in the future.

### Refactor

- Redesigned the dependency addition tool
- Removed the cumbersome encryption method; we now use IDEA's API to manage keys

### Performance

- optimize ModJsonParser string building array parsing
- optimize collection mapping in PublishModDialog
- optimize asset parsing in GitlabAPI
- optimize regex group extraction performance
- optimize UI rendering with incremental dependency list updates

### Chore

- Bump minecraft version list
- Bump kotlin version
- Bump okhttp version
- Update CurseForge token link

## v1.10.2

### Chore

- Bump minecraft version list

## v1.10.1

### Chore

- Bump minecraft version list
- Bump kotlin version

## v1.10.0

### Feature

- Reimplement Gitlab
- Support sync README to Modrinth
- Support LiteLoader and JavaAgent
- Support automatic selection of Support Targets

### Fix

- Fixed the parsing failure caused by multiple ways of writing the minecraft version range in fabric.mod.json
- Bundling OkHttp to fix the issue where ModPublish could not be used in IntelliJ IDEA 26

### Refactor

- VersionProcessor to use data classes for better performance
- simplify verbose null and blank/empty checks

### Performance

- Cache local Minecraft versions
- optimize version processor with streaming JSON
- Default to compact JSON serialization

### Change

- Changelog input box is now scrollable
- Import SKToml as a separate dependency
- Java 21 is now required

### Chore

- Bump minecraft version list
- Bump kotlin version

## v0.0.9

### Fix

- Modrinth publish failed

## v0.0.8

### Optimize

- Improved KToml parser

### Chore

- Bump minecraft version list

### Fix

- LocalModInfo not being updated correctly

## v0.0.7

### Refactor

- Refactoring part of the code using Kotlin

### Optimize

- Improved KToml parser

### Change

- Java 17 is now required
- Use Kotlin coroutines instead of JVM virtual threads

### Remove

- remove lombok

### Chore

- Bump minecraft version list

### Fix

- Fix file selector and ReleaseChannel to the left side of the page to resolve the issue where they sometimes moved to
  the center.
- Under certain conditions, the PublishUI success dialog cannot pop up

---

## v0.0.6

### Feature

- More configurable network options
- Support proxy simple auth
- Add pluginIcon
- Curseforge supports multiple file uploads
- When the previous publication target fails, the later publishing tasks will not be canceled
- When publishing fails, all error messages are displayed
- Supports hot updates of the Minecraft version list

### Performance

- PublishTask is now parallel
- PublishTask no longer freezes PublishUI
- Improved Toml parser

### Optimize

- Enhanced number input field validation
- More icons

### Translate

- Improved translations

### Change

- Java 21 is now required

### Remove

- remove modrinth test server

### Chore

- Bump minecraft version list

### Fix

- Incorrect reading of ProxyType
- In some cases, the GUI could not start due to VersionRangeParser initialization failure.

---

## v0.0.5

### Feature

- More uses for version parser: Automatically select version ranges
- Upload multiple files simultaneously
- Improved Toml parser
- When API errors occur, should return API ID
- Support using proxy server. Need to configure in settings.
- Support auto detecting current Git branch

### Performance

- Don't process Minecraft version list synchronously when creating UI
- Cache isn't used effectively

### Change

- Network request timeout increased from 15 to 20 seconds

### Translate

- Added German translation

### Remove

- Cancel plans to provide GitLab compatibility

### Chore

- Use Lombok to clean code
- Add a prompt for CurseForge
- Bump minecraft version list

### Fix

- Version parts should not be null
- The first value is not selected by default after refreshing Minecraft version list
- An incorrect judgment in the dependency manager
- When API returns error message, should not call getI18n method to create PublishResult
- Settings options disappeared due to ID conflict

---

## v0.0.4

### Feature

- Improved Toml Parser
- Added variables for name template: low-version, max-version
- When publishing to Github, it will now automatically reuse existing release tags

### UI

- Allow scrolling in the Publish page
- Replaced Changelog input component with EditorTextField
- PublishUI layout optimization

### Change

- When publishing to Github/Gitlab, loader and support target selection is no longer mandatory

### Translate

- Improved translations
- Added French translation
- Added Russian translation
- Added Spanish translation

### Chore

- Bump minecraft version list

### Fix

- Title repeatedly requests i18n
- Fixed incorrect Korean reference

---

## v0.0.3

### Feature

- Detect token decryption status on release page
- Improved hardware ID algorithm to prevent frequent token invalidation
- Added support for Github
- Github/GitLab branches are now optional, ModPublish will search for them in the repo
- Quick access to token application pages from settings

### Fix

- Do not display tooltip for disabled release targets on release page

### Translate

- Improved translations

### Change

- ModType name should be capitalized

---

## v0.0.2

### Feature

- Auto generate title based on mod name/version/loader or user-customized format
- Support for selecting release channels

### Optimize

- Project configuration UI optimization
- Project structure optimization

### Fix

- Unable to read TOML
- Some other bugs (my commit records were lost, so I don't remember)

### Remove

- No longer using JToml

---

### v0.0.1

- Release