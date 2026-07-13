window.ModPublish = window.ModPublish || {};
window.ModPublish.pages = window.ModPublish.pages || {};

window.ModPublish.pages.intellij = {
    render: function () {
        return `
            <div class="project-page-container animate-fade-in">
                <div class="project-banner-header">
                    <img src="image/banner_intellij.svg" alt="ModPublish IntelliJ Banner">
                </div>
                
                <div class="project-details-grid">
                    <aside class="project-meta-sidebar">
                        <div class="project-meta-card card">
                            <div class="meta-header">
                                <img src="image/icon_intellij.svg" alt="IntelliJ Icon" class="meta-icon">
                                <div>
                                    <h2>IntelliJ Edition</h2>
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
                                    <span class="meta-label">JetBrains Marketplace</span>
                                    <a href="https://plugins.jetbrains.com/plugin/28320-minecraft-mod-publish-utils" target="_blank" class="meta-value">
                                        Minecraft Mod Publish Utils
                                    </a>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">Latest Version</span>
                                    <span class="meta-value" id="plugin-latest-version">Loading...</span>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">Downloads</span>
                                    <span class="meta-value" id="plugin-downloads">Loading...</span>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">Last Updated</span>
                                    <span class="meta-value" id="plugin-last-updated">Loading...</span>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">License</span>
                                    <span class="meta-value font-mono">LGPL-3.0</span>
                                </div>
                            </div>
                            <div class="meta-actions">
                                <a href="https://plugins.jetbrains.com/plugin/28320-minecraft-mod-publish-utils" target="_blank" class="btn btn-primary btn-full intellij-btn">
                                    Install Plugin
                                </a>
                                <a href="https://github.com/404Setup/ModPublish" target="_blank" class="btn btn-secondary btn-full">
                                    View Source
                                </a>
                            </div>
                        </div>
                    </aside>
                    
                    <main class="project-main-content">
                        <section class="content-section">
                            <h1>Minecraft Mod Publish Utils for IntelliJ IDEA</h1>
                            <p class="lead">
                                The flagship version of ModPublish, built specifically for JetBrains IDEs to help mod developers automate multi-platform release distributions.
                            </p>
                            <p>
                                Quickly publish your Minecraft mods to multiple mod hosting sites directly from the comfort of IntelliJ IDEA. By automating version parsing and payload generation, it streamlines what used to be a tedious, error-prone manual process.
                            </p>
                        </section>

                        <section class="content-section">
                            <h2>Key Features</h2>
                            <ul class="feature-bullet-list">
                                <li>
                                    <strong>Fully GUI-based Operation:</strong> No complex config scripts or gradle settings needed. Fill out the dialog details and publish in one click.
                                </li>
                                <li>
                                    <strong>Automatic Metadata Detection:</strong> Reads and parses mod descriptors (such as <code>fabric.mod.json</code>, <code>mods.toml</code>, <code>neoforge.mods.toml</code>, <code>litemod.json</code>, and <code>mcmod.info</code>) directly inside the editor.
                                </li>
                                <li>
                                    <strong>History Retention:</strong> Saves the last configured platform dependencies and changelog text, eliminating repetitive typing for incremental updates.
                                </li>
                                <li>
                                    <strong>Simultaneous Publishing:</strong> Pushes compiled packages and documentation to Modrinth, CurseForge, GitHub, and GitLab in parallel.
                                </li>
                                <li>
                                    <strong>Built-in Description Syncing:</strong> Syncs or updates mod descriptions and info pages directly from local Markdown files.
                                </li>
                            </ul>
                        </section>

                        <section class="content-section">
                            <h2>Quick Setup</h2>
                            <ol class="setup-list">
                                <li>
                                    <strong>Install the Extension:</strong> Head to <em>Settings/Preferences > Plugins</em> in IntelliJ, search for <code>Minecraft Mod Publish Utils</code>, and click install.
                                </li>
                                <li>
                                    <strong>Register API Tokens:</strong> Go to <em>Tools > ModPublish Settings</em> to enter and securely save API tokens for your publishing targets.
                                </li>
                                <li>
                                    <strong>Define ModIDs:</strong> Input your project ID configurations in the Settings page or directly in the popup publishing wizard.
                                </li>
                                <li>
                                    <strong>Publish:</strong> Right-click on your project's built JAR file, select "Publish Mod...", confirm details, and hit send!
                                </li>
                            </ol>
                            <div class="setup-docs-link">
                                <a href="#docs" class="btn btn-outline">Read IntelliJ Config Guide</a>
                            </div>
                        </section>
                    </main>
                </div>
            </div>
        `;
    },

    init: function () {
        this.fetchMarketplaceStats();
    },

    getCookie: function (name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    setCookie: function (name, value, days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = "; expires=" + date.toUTCString();
        document.cookie = name + "=" + (value || "") + expires + "; path=/; SameSite=Lax";
    },

    fetchMarketplaceStats: function () {
        const versionEl = document.getElementById('plugin-latest-version');
        const downloadsEl = document.getElementById('plugin-downloads');
        const updatedEl = document.getElementById('plugin-last-updated');

        const cachedStr = this.getCookie('modpublish_intellij_stats');
        if (cachedStr) {
            try {
                const cachedStats = JSON.parse(cachedStr);
                if (versionEl) versionEl.innerText = cachedStats.version;
                if (downloadsEl) downloadsEl.innerText = cachedStats.downloads;
                if (updatedEl) updatedEl.innerText = cachedStats.lastUpdated;
                return;
            } catch (e) {
                console.error("Failed to parse cached cookie data:", e);
            }
        }

        const apiBaseUrl = window.ModPublish?.config?.apiBaseUrl || 'http://localhost:3000';

        this.abortController = new AbortController();
        const timeoutId = setTimeout(() => {
            if (this.abortController) this.abortController.abort();
        }, 5000);

        fetch(`${apiBaseUrl}/api/proxy?url=${encodeURIComponent('https://plugins.jetbrains.com/plugins/list?pluginId=one.pkg.modpublish')}`, {
            signal: this.abortController.signal
        })
            .then(res => {
                clearTimeout(timeoutId);
                if (!res.ok) throw new Error('API server returned error status');
                return res.text();
            })
            .then(xmlText => {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(xmlText, 'text/xml');
                const firstPlugin = xmlDoc.querySelector('idea-plugin');

                if (firstPlugin) {
                    const version = firstPlugin.querySelector('version')?.textContent || 'Unknown';
                    const downloads = firstPlugin.getAttribute('downloads') || 'Unknown';
                    const updatedDateAttr = firstPlugin.getAttribute('updatedDate');

                    const versionStr = 'v' + version;
                    const downloadsStr = parseInt(downloads).toLocaleString();
                    let updatedStr = 'Unknown';

                    if (updatedDateAttr) {
                        const ts = parseInt(updatedDateAttr);
                        updatedStr = new Date(ts).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric'
                        });
                    }

                    const statsObject = {
                        version: versionStr,
                        downloads: downloadsStr,
                        lastUpdated: updatedStr
                    };

                    this.setCookie('modpublish_intellij_stats', JSON.stringify(statsObject), 3);

                    if (versionEl) versionEl.innerText = versionStr;
                    if (downloadsEl) downloadsEl.innerText = downloadsStr;
                    if (updatedEl) updatedEl.innerText = updatedStr;
                } else {
                    throw new Error('Plugin element not found in response XML');
                }
            })
            .catch(err => {
                clearTimeout(timeoutId);
                console.warn('Failed to query JetBrains list API. Falling back to static details:', err);

                const versionStr = 'Unknown';
                const downloadsStr = 'Unknown';
                const updatedStr = 'Unknown';

                if (versionEl) versionEl.innerText = versionStr;
                if (downloadsEl) downloadsEl.innerText = downloadsStr;
                if (updatedEl) updatedEl.innerText = updatedStr;
            });
    },

    abortController: null,

    destroy: function () {
        if (this.abortController) {
            this.abortController.abort();
            this.abortController = null;
        }
    }
};
