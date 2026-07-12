window.ModPublish = window.ModPublish || {};
window.ModPublish.pages = window.ModPublish.pages || {};

window.ModPublish.pages.vscode = {
    render: function() {
        return `
            <div class="project-page-container animate-fade-in">
                <div class="project-banner-header">
                    <img src="image/banner_vscode.svg" alt="ModPublish VS Code Banner">
                </div>
                
                <div class="project-details-grid">
                    <aside class="project-meta-sidebar">
                        <div class="project-meta-card card">
                            <div class="meta-header">
                                <img src="image/icon_vscode.svg" alt="VS Code Icon" class="meta-icon">
                                <div>
                                    <h2>VS Code Edition</h2>
                                    <span class="badge">Active</span>
                                </div>
                            </div>
                            <hr class="divider">
                            <div class="meta-links">
                                <div class="meta-link-item">
                                    <span class="meta-label">GitHub Repository</span>
                                    <a href="https://github.com/404Setup/ModPublish" target="_blank" class="meta-value">
                                        404Setup/ModPublish
                                    </a>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">VS Code Marketplace</span>
                                    <a href="https://marketplace.visualstudio.com/items?itemName=404Setup.modpublish-vsc" target="_blank" class="meta-value">
                                        ModPublish for VSC
                                    </a>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">Latest Version</span>
                                    <span class="meta-value" id="extension-latest-version">Loading...</span>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">Last Updated</span>
                                    <span class="meta-value" id="extension-last-updated">Loading...</span>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">License</span>
                                    <span class="meta-value font-mono">LGPL-3.0</span>
                                </div>
                            </div>
                            <div class="meta-actions">
                                <a href="https://marketplace.visualstudio.com/items?itemName=404Setup.modpublish-vsc" target="_blank" class="btn btn-primary btn-full vscode-btn">
                                    Install Extension
                                </a>
                                <a href="https://github.com/404Setup/ModPublish" target="_blank" class="btn btn-secondary btn-full">
                                    View Source
                                </a>
                            </div>
                        </div>
                    </aside>
                    
                    <main class="project-main-content">
                        <section class="content-section">
                            <h1>Minecraft Mod Publish Utils for VS Code</h1>
                            <p class="lead">
                                A lightweight Visual Studio Code port of the ModPublish tool suite, ideal for developers who prefer text-editor environments.
                            </p>
                            <p>
                                The VS Code edition integrates directly into your workspace sidebar tree, allowing you to trigger a publishing dialog by right-clicking on compiled artifact files (<code>.jar</code> or <code>.vsix</code>). It inherits code editor color schemes and provides quick access via standard commands.
                            </p>
                        </section>

                        <section class="content-section">
                            <h2>Key Features</h2>
                            <ul class="feature-bullet-list">
                                <li>
                                    <strong>Explorer Context Menu:</strong> Right-click on any jar file directly in the file tree, select "Publish Mod..." to launch the upload dialog.
                                </li>
                                <li>
                                    <strong>Vanilla HTML Webview:</strong> Built completely with native JS and CSS variables, automatically picking up VS Code's editor themes (dark, light, or high-contrast styles).
                                </li>
                                <li>
                                    <strong>Fast Metadata Reading:</strong> Scans jar archives locally to parse dependency info, loaders, and versions within milliseconds.
                                </li>
                                <li>
                                    <strong>System Keychain Storage:</strong> Securely saves developer API keys in the operating system's vault via VS Code's secure secrets storage.
                                </li>
                            </ul>
                        </section>

                        <section class="content-section">
                            <h2>Quick Setup</h2>
                            <ol class="setup-list">
                                <li>
                                    <strong>Install Extension:</strong> Open VS Code extensions search, type <code>Minecraft Mod Publish Utils for vsc</code>, and click Install.
                                </li>
                                <li>
                                    <strong>Add Project Settings:</strong> Create a <code>.vscode/settings.json</code> configuration to declare project IDs:
                                    <pre><code class="language-json">{
  "modpublish.modrinth.modid": "A1b2C3d4",
  "modpublish.curseforge.modid": "123456"
}</code></pre>
                                </li>
                                <li>
                                    <strong>Input API Tokens:</strong> Open Command Palette (<code>Ctrl+Shift+P</code> / <code>Cmd+Shift+P</code>), type <code>ModPublish: Set Platform API Token...</code>, and set credentials.
                                </li>
                                <li>
                                    <strong>Publish:</strong> Right-click your compiled mod jar in the sidebar, select "Publish Mod...", type version notes, and upload.
                                </li>
                            </ol>
                            <div class="setup-docs-link">
                                <a href="#docs" class="btn btn-outline">Read VS Code Config Guide</a>
                            </div>
                        </section>
                    </main>
                </div>
            </div>
        `;
    },

    init: function() {
        this.fetchMarketplaceStats();
    },

    getCookie: function(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for(let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    setCookie: function(name, value, days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = "; expires=" + date.toUTCString();
        document.cookie = name + "=" + (value || "") + expires + "; path=/; SameSite=Lax";
    },

    fetchMarketplaceStats: function() {
        const versionEl = document.getElementById('extension-latest-version');
        const updatedEl = document.getElementById('extension-last-updated');

        const cachedStr = this.getCookie('modpublish_vscode_stats');
        if (cachedStr) {
            try {
                const cachedStats = JSON.parse(cachedStr);
                if (versionEl) versionEl.innerText = cachedStats.version;
                if (updatedEl) updatedEl.innerText = cachedStats.lastUpdated;
                return;
            } catch (e) {
                console.error("Failed to parse cached cookie data:", e);
            }
        }

        const requestBody = {
            assetTypes: null,
            filters: [
                {
                    criteria: [{ filterType: 7, value: '404Setup.modpublish-vsc' }],
                    direction: 2,
                    pageSize: 1,
                    pageNumber: 1,
                    sortBy: 0,
                    sortOrder: 0,
                    pagingToken: null
                }
            ],
            flags: 2151
        };

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 5000);

        fetch('https://marketplace.visualstudio.com/_apis/public/gallery/extensionquery', {
            method: 'POST',
            signal: controller.signal,
            headers: {
                'Accept': 'application/json;api-version=7.2-preview.1;excludeUrls=true',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        })
        .then(res => {
            clearTimeout(timeoutId);
            if (!res.ok) throw new Error('API server returned error status');
            return res.json();
        })
        .then(data => {
            const extension = data.results?.[0]?.extensions?.[0];
            if (extension) {
                const version = extension.versions?.[0]?.version || 'Unknown';
                const lastUpdatedStr = extension.lastUpdated || 'Unknown';
                
                const versionStr = 'v' + version;
                const updatedStr = new Date(lastUpdatedStr).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric'
                });

                const statsObject = {
                    version: versionStr,
                    lastUpdated: updatedStr
                };

                this.setCookie('modpublish_vscode_stats', JSON.stringify(statsObject), 3);

                if (versionEl) versionEl.innerText = versionStr;
                if (updatedEl) updatedEl.innerText = updatedStr;
            } else {
                throw new Error('Extension data not found in response JSON');
            }
        })
        .catch(err => {
            clearTimeout(timeoutId);
            console.warn('Failed to query VS Code Marketplace API. Falling back to static details:', err);
            
            const versionStr = 'Unknown';
            const updatedStr = 'Unknown';

            if (versionEl) versionEl.innerText = versionStr;
            if (updatedEl) updatedEl.innerText = updatedStr;
        });
    },

    destroy: function() {
    }
};
