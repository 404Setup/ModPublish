## v0.0.7

### Refactor
- Refactoring part of the code using Kotlin

### Chore
- Bump minecraft version list

### Fix
- Fix file selector and ReleaseChannel to the left side of the page to resolve the issue where they sometimes moved to the center.

---

## v0.0.6

### Feat
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

### Feat
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

### Feat
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

### Feat
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

### Feat
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