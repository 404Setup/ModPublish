# ModPublish VS Code Extension

This is the VS Code port of the ModPublish IntelliJ plugin.

Quickly publish your Minecraft mods to multiple mod hosting sites using Visual Studio Code.


## Features

- **Explorer Context Menu Integration**: Right-click on any `.jar` or `.litemod` in the Explorer and select "Publish
  Mod..." to launch.
- **Auto Metadata Extraction**: Auto-reads `fabric.mod.json`, `mods.toml`, `neoforge.mods.toml`, `riftmod.json`,
  `litemod.json`, `mcmod.info` and manifest files directly from the JAR.
- **Vanilla JS Webview**: Custom, lightweight form UI styled dynamically with VS Code theme variables, adapting
  automatically to light/dark themes.
- **Multi-platform publishing**: One-click upload to Modrinth, CurseForge, GitHub, and GitLab.
- **Git Integration**: Dynamically pre-fills branch and repo details.

## Install

We have already published it on the Visual Studio Code Marketplace. You can access it from the following URL, or you can
search for "Minecraft Mod Publish Utils for vsc" in the extension marketplace of your vscode, trae, or any other IDE
based on Visual Studio Code to install it. (Usually, you don't need to type the full name.)

Please note that this is a completely free extension, with no cloud services operated by the developer, and the code has
not been obfuscated in any way. If you find someone publishing ModPublish with suspicious code behavior, full of ads or
paid pop-ups, please report to me immediately.

Download: [Marketplace](https://marketplace.visualstudio.com/items?itemName=404Setup.modpublish-vsc)

## How to Configure

1. Open the command palette (`Ctrl+Shift+P` / `Cmd+Shift+P`) and search for **`ModPublish: Set Platform API Token...`**
   to securely register your credentials.
2. In VS Code settings or your workspace's `.vscode/settings.json`, configure non-sensitive Project IDs and
   repositories:
    - `modpublish.modrinth.modid`
    - `modpublish.curseforge.modid`
    - `modpublish.github.repo`
    - `modpublish.gitlab.repo`

## Build & Run

To build the extension:

1. Install dependencies:
   ```bash
   npm install && npm install -g @vscode/vsce
   ```
2. Build/Bundle:
   ```bash
   vsce package
   ```
3. Open the folder in VS Code, press `F5`, and choose "VS Code Extension Development Host" to debug and test.

## Support ModPublish

If you like my work, or if it has brought you great convenience, please consider supporting my work
through [Patreon](https://www.patreon.com/tranic).

If you don't want to do that, you can also support me by clicking the Star on the Repo.
The more Stars, the happier I am, and the more motivated I am to continue working.

Thanks to everyone who uses and supports ModPublish.

## License
2025 - 2026 404Setup. All rights reserved.

The source code is licensed under the LGPL-3.0 license.