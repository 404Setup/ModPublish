<img src="./src/main/resources/META-INF/pluginIcon.svg" alt="Logo" align="right" width="150">

ModPublish
====

**This plugin is completely free, please download it from Jetbrains Marketplace or the official GitHub page!
Do not download this plugin from any untrusted third party pages!**

----

Quickly publish your Minecraft mods to multiple mod hosting sites using Jetbrains IDEA.

It may not be as convenient as some Gradle plugins, but I want to do it.

## Feature

- Fully GUI-based operation
- Automatically detect Mod information, no need to repeatedly manually select/input
- Quick saving of last operation records (dependencies, changelog)
- One-click publishing of mods to Modrinth, CurseForge, GitHub, and GitLab
- Update Mod Description Page

### Unsupported Features

> These features will never be supported unless someone submits a pull request,
> or I suddenly want to implement them

- Publishing to custom Git servers
- Publishing resource packs/data packs

### TODO

> Priority order from top to bottom, smaller numbers indicate higher priority

1. Update Forge/NeoForge update.json during publishing
2. Publishing plugins
3. Improve dependency manager
4. Allow syncing README to ~~Modrinth and~~ CurseForge (no API available)
5. Publishing to Hangar/SpigotMC

## Installation

ModPublish can be found in the Jetbrains plugin marketplace. You can install it by searching for "Minecraft Mod Publish
Utils" in IDEA's `Plugin` module,
or download it through [this link](https://plugins.jetbrains.com/plugin/28320-minecraft-mod-publish-utils).

## Usage
Before getting started, you need to apply for API Tokens from your publishing targets and configure ModIDs for your
project. If you don't do this, the corresponding publishing targets will be automatically disabled.

[Wiki](https://github.com/404Setup/ModPublish/wiki)

## Build

Building ModPublish requires the following tools

- JDK 17
- Gradle 9
- Python3

Build steps:

1. Run `./version_processor.py` to download latest Minecraft versions from network
2. Copy generated json files to `./src/main/resources/META-INF` directory (`minecraft.version.json`)
3. Run `./gradlew buildPlugin`
4. Final output is located in `./build/distributions` directory

## Contribution

> This is not mandatory, just a suggestion

- Do not introduce third party dependencies unless necessary
- Do not write duplicate code
- Ensure code has good readability
- Tests are optional, but functionality must be manually verified before submission
- Comments are not mandatory. But if there are any, please use English.
- Variable/method/parameter names should not be too long

### For Translations

- Do not use machine translation services like Google, DeepL, etc.
- If using AI translation, ensure it does not cause major ambiguity
- Do not use discriminatory language
- Do not use dialects
- Do not use unnatural or confusing language

## Credits

#### Dependencies

- [Gson](https://github.com/google/gson) - License [Apache 2.0](https://github.com/google/gson/blob/main/LICENSE)
- [OKHttp](https://github.com/square/okhttp) -
  License [Apache 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt)
- [TProxy](https://github.com/404Setup/t-proxy) -
  License [Apache 2.0](https://github.com/404Setup/t-proxy/blob/master/LICENSE)
- [TinyUtils](https://github.com/404Setup/tiny-utils) -
  License [Apache 2.0](https://github.com/404Setup/tiny-utils/blob/master/LICENSE)
- [Flexmark](https://github.com/vsch/flexmark-java) -
  License [BSD 2-Clause "Simplified" License](https://github.com/vsch/flexmark-java/blob/master/LICENSE.txt)

#### Resource Files

All resource files have been adjusted to display at 24px.

- [Modrinth Logo](https://github.com/modrinth/code/blob/main/packages/assets/branding/logo.svg) -
  License [GPL-3.0](https://github.com/modrinth/code/blob/main/packages/assets/LICENSE), with proper coloring of the
  logo
- [CurseForge Logo](https://gist.github.com/thecodewarrior/110057b210551c4ecf2c9be6d58ff824) -
  License [CC BY 4.0](https://gist.github.com/thecodewarrior/110057b210551c4ecf2c9be6d58ff824?permalink_comment_id=3683512#gistcomment-3683512)
- [Github Icon](https://github.com/logos)
- [Gitlab Logo (vscode-icons)](https://github.com/vscode-icons/vscode-icons) - [License](https://github.com/vscode-icons/vscode-icons#license)
- [FluentUI System Icons](https://github.com/microsoft/fluentui-system-icons) -
  License [MIT](https://github.com/microsoft/fluentui-system-icons/blob/main/LICENSE)

## Support ModPublish

If you like my work, or if it has brought you great convenience, please consider supporting my work
through [Patreon](https://www.patreon.com/tranic).

If you don't want to do that, you can also support me by clicking the Star on the Repo.
The more Stars, the happier I am, and the more motivated I am to continue working.

Thanks to everyone who uses and supports ModPublish.

## License

The source code is licensed under the LGPL-3.0 license.