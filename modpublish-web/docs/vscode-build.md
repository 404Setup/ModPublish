# VS Code Extension Build Guide

To package or debug the extension locally, you need Node.js and the VSCE packaging tool.

## Steps

1. Navigate to the `modpublish-vscode` subproject directory.
2. Install standard node dependencies:
   ```bash
   npm install
   ```
3. Install the VS Code Extension Manager globally:
   ```bash
   npm install -g @vscode/vsce
   ```
4. Package the extension into a local `.vsix` file:
   ```bash
   vsce package
   ```
5. Install the generated `.vsix` file into your VS Code manually.
6. To debug, open the directory in VS Code, go to the Run & Debug pane, and press `F5` to run a Development Host instance.
