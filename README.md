# ModPublish

**This plugin is completely free, please download it from Jetbrains Marketplace or the official GitHub page!
Do not download this plugin from any untrusted third party pages!**

----

Quickly publish your Minecraft mods to multiple mod hosting sites using Jetbrains IDEA.

It may not be as convenient as some Gradle plugins, but I want to do it.

## Feature

- Fully GUI-based operation
- Automatically detect Mod information, no need to repeatedly manually select/input
- Quick saving of last operation records (dependencies, changelog)
- One-click publishing of mods to Modrinth, CurseForge and GitHub

### Unsupported Features

> These features will never be supported unless someone submits a pull request, 
> or I suddenly want to implement them

- Publishing to custom Git servers
- Publishing to Hangar/SpigotMC
- Publishing plugins/resource packs/data packs

### TODO

> Priority order from top to bottom, smaller numbers indicate higher priority

1. Improve dependency manager
2. Upload multiple files simultaneously
3. Code optimization
4. UI optimization
5. Allow hot updating of Minecraft version list
6. Publish to GitLab
7. Allow syncing README to Modrinth and CurseForge
8. Allow customizing mod upload targets

## Installation

ModPublish can be found in the Jetbrains plugin marketplace. You can install it by searching for "Minecraft Mod Publish
Utils" in IDEA's `Plugin` module,
or download it through [this link](https://plugins.jetbrains.com/plugin/28320-minecraft-mod-publish-utils).

## Usage

Before getting started, you need to apply for API Tokens from your publishing targets and configure ModIDs for your
project. If you don't do this, the corresponding publishing targets will be automatically disabled.

### Applying for API Tokens

#### For Modrinth

Apply for an API key from [Pats](https://modrinth.com/settings/pats) and set permissions as shown in the images

![Modrinth Pats](image/modrinth_token_page.png)

![Modrinth Pats](image/modrinth_token_page_2.png)
After clicking `Create PAT`, click to copy the API Token starting with `mrp_`, save it and fill it into the ModPublish
settings.

#### Curseforge

For Curseforge, you need to apply for two Tokens: **API Token** and **Studio Token**.

Apply for **Studio Token** from [here](https://console.curseforge.com/?#/api-keys)

![Curseforge Studio Token](image/curseforge_studio_token.png)

If the API Token is not displayed after registration, please leave and return to this page after a few minutes. It will
be used for dependency validation.

Apply for **API Token** from [here](https://legacy.curseforge.com/account/api-tokens), they are similar to UUIDs. It
will be used for file uploads and version creation.

#### Github

Please apply from [Fine-grained personal access tokens](https://github.com/settings/personal-access-tokens) and
configure permissions as shown in the image.

![Github PAT](image/github_token.png)

Token should start with `github_pat_`.

### Fill in API Tokens and ModID

If you want to use the same API Token across multiple projects, you should fill them in at the following location in
IDEA: `Settings | Tools | ModPublish: Global Settings`

You also need to add ModIDs in `Project | Tools | Configure ModPublish for Project`. The `API Token` you fill in here
will override the global `API Token`.

### About API Token Security

I cannot completely guarantee its security. What you need to do is not share the following information with others:

- The `workspace.xml` file in the project's `.idea` directory
- IDEA's global configuration files
- Any of your API Tokens

Additionally, the API Token will generate an encryption key based on your environment and use this encryption key to
process the API Token, encrypting and decrypting it when needed.
If hardware information, system information and JVM change, the old API Token will immediately become invalid and
unreadable. Therefore, please open settings to back up or generate new API Tokens before making any changes to prevent
operation interruption.

Do not run software from unknown sources, and do not actively calculate encryption keys and share them with others!
If you believe your API Tokens have been leaked, please revoke them immediately and regenerate new ones.

If they support fine-grained permission configuration, please do so and do not add extra permissions.

### Version Name Formatting

You may want a more formatted version name (not version number), such as `MyMod 1.0.0 Fabric 1.21.8` instead of something like
`my-mod-1.0.0+fabric.jar`.

Modrinth usually generates titles automatically, but CurseForge does not, and you don't want to manually copy it every
time.

ModPublish provides this feature since version 0.0.2. You need to configure the final formatted name template in
`Project | Tools | Configure ModPublish for Project`.
Currently supported variables:

- `{version}` - Mod version
- `{name}` - Mod name
- `{loader}` - First detected ModLoader that is compatible with the Mod
- `{low-version}` - Lowest Minecraft version that is compatible with the Mod; Not replaced if detection fails
- `{max-version}` - Highest Minecraft version that is compatible with the Mod; Not replaced if detection fails

If not configured, ModPublish will still use the default name generation rules

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

- Avoid excessive use of syntactic sugar
- Do not introduce third party dependencies unless necessary
- Do not write duplicate code
- Ensure code has good readability
- Only use Java
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
- [OKHttp](https://github.com/square/okhttp) - License [Apache 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt)

#### Resource Files
All resource files have been adjusted to display at 24px.

- [Modrinth Logo](https://github.com/modrinth/code/blob/main/packages/assets/branding/logo.svg) - License [GPL-3.0](https://github.com/modrinth/code/blob/main/packages/assets/LICENSE), with proper coloring of the logo
- [CurseForge Logo](https://gist.github.com/thecodewarrior/110057b210551c4ecf2c9be6d58ff824) - License [CC BY 4.0](https://gist.github.com/thecodewarrior/110057b210551c4ecf2c9be6d58ff824?permalink_comment_id=3683512#gistcomment-3683512)
- [Gitlab Logo (vscode-icons)](https://github.com/vscode-icons/vscode-icons) - [License](https://github.com/vscode-icons/vscode-icons#license)
- [Github Icon](https://github.com/logos)

## Support ModPublish

If you like my work, or if it has brought you great convenience, please consider supporting my work
through [Patreon](https://www.patreon.com/tranic).

If you don't want to do that, you can also support me by clicking the Star on the Repo. 
The more Stars, the happier I am, and the more motivated I am to continue working.

Thanks to everyone who uses and supports ModPublish.

## License
The source code is licensed under the LGPL-3.0 license.