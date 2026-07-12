# Configuration Guide

Setting up credentials and project IDs is a shared requirement across both the IntelliJ and VS Code extensions. While the settings locations differ, the credentials themselves are universal.

## 1. Setting up API Tokens

To authorize file uploads, you must generate developer API tokens for each hosting target. These tokens act as passwords and should be kept private:

* **Modrinth Token:** Obtain a token from your Modrinth Account Settings.
* **CurseForge Token:** Create an API key in the CurseForge Developer Console.
* **GitHub Token:** Personal Access Token with `repo` write scope.

<div class="image-gallery">
    <figure>
        <img src="image/github_token.png" alt="Creating a GitHub Personal Access Token">
        <figcaption>Creating a GitHub Personal Access Token with repo scope.</figcaption>
    </figure>
    <figure>
        <img src="image/modrinth_token_page.png" alt="Obtaining Modrinth API Token">
        <figcaption>Obtaining your API Token from the Modrinth developer settings.</figcaption>
    </figure>
</div>

## 2. Configuring Project IDs

Each mod page has a unique platform identifier. Copy these identifiers and supply them to your extension so it maps files to the correct page:

<div class="image-gallery">
    <figure>
        <img src="image/get_curseforge_modid.png" alt="CurseForge Project ID location">
        <figcaption>Copy the project ID from your CurseForge mod dashboard.</figcaption>
    </figure>
    <figure>
        <img src="image/get_modrinth_modid.png" alt="Modrinth Mod ID location">
        <figcaption>Copy the project ID from your Modrinth mod dashboard settings.</figcaption>
    </figure>
</div>

## 3. Where to Input Settings

### In IntelliJ IDEA

* **API Tokens:** Go to **Tools > ModPublish Settings** and input tokens. They are saved securely in the IDE's internal safe storage (Password Safe).
* **Project IDs:** Fill them directly in the popup publishing wizard window, or configuration settings.

### In VS Code

* **API Tokens:** Open the VS Code Command Palette (`Ctrl+Shift+P` / `Cmd+Shift+P`) and search for **`ModPublish: Set Platform API Token...`**. These are saved in the OS keychain via the VS Code Secrets API.
* **Project IDs:** Add them to your workspace's `.vscode/settings.json`:
  ```json
  {
    "modpublish.modrinth.modid": "A1b2C3d4",
    "modpublish.curseforge.modid": "123456",
    "modpublish.github.repo": "username/repo-name",
    "modpublish.gitlab.repo": "username/repo-name"
  }
  ```
