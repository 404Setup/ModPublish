window.ModPublish = window.ModPublish || {};
window.ModPublish.pages = window.ModPublish.pages || {};

window.ModPublish.pages.home = {
    render: function() {
        return `
            <div class="homepage-container">
                <section class="hero animate-fade-in">
                    <div class="hero-grid-pattern"></div>
                    <div class="hero-content">
                        <div class="hero-brand">
                            <img src="image/icon_generic.svg" alt="ModPublish" class="hero-brand-icon">
                            <span class="hero-brand-name">Mod<span class="gradient-text">Publish</span></span>
                        </div>
                        <h1 class="hero-heading">Simpler Mod Publishing for Minecraft Developers</h1>
                        <p class="hero-subtitle">
                            An open-source suite of extension tools designed to quickly publish Fabric, Forge, NeoForge, and Quilt mods to Modrinth, CurseForge, GitHub, and GitLab simultaneously.
                        </p>
                        <div class="hero-actions">
                            <a href="#docs" class="btn btn-primary">Read the Docs</a>
                            <a href="https://github.com/404Setup/ModPublish" target="_blank" rel="noopener" class="btn btn-secondary">
                                <svg class="icon-svg" viewBox="0 0 16 16" width="16" height="16">
                                    <path fill="currentColor" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path>
                                </svg>
                                Star on GitHub
                            </a>
                        </div>
                    </div>
                </section>

                <section class="subprojects-section animate-fade-in" style="animation-delay: 0.1s;">
                    <h2 class="section-title">Select Edition</h2>
                    <div class="subprojects-grid">
                        <div class="project-card intellij-card">
                            <div class="card-body">
                                <div class="card-header">
                                    <img src="image/icon_intellij.svg" alt="IntelliJ Icon" class="project-icon">
                                    <div>
                                        <h3>IntelliJ IDEA Edition</h3>
                                        <span class="badge">Kotlin</span>
                                    </div>
                                </div>
                                <p class="card-description">
                                    The full-featured JetBrains IDE plugin. Auto-detects mod metadata, reads translation settings, caches changelogs, and uploads to multiple targets.
                                </p>
                                <div class="card-footer-action">
                                    <a href="#intellij" class="explore-link">
                                        Explore IntelliJ plugin &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="project-card vscode-card">
                            <div class="card-body">
                                <div class="card-header">
                                    <img src="image/icon_vscode.svg" alt="VS Code Icon" class="project-icon">
                                    <div>
                                        <h3>VS Code Edition</h3>
                                        <span class="badge">TypeScript</span>
                                    </div>
                                </div>
                                <p class="card-description">
                                    A lightweight extension for VS Code. Integrates into the workspace explorer tree, parses manifests, and uses native code-editor color themes.
                                </p>
                                <div class="card-footer-action">
                                    <a href="#vscode" class="explore-link">
                                        Explore VS Code extension &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>
                        <div class="project-card telemetry-card">
                            <div class="card-body">
                                <div class="card-header">
                                    <img src="image/icon_telemetry.svg" alt="Telemetry Icon" class="project-icon">
                                    <div>
                                        <h3>Telemetry Service</h3>
                                        <span class="badge">Go + SQLite</span>
                                    </div>
                                </div>
                                <p class="card-description">
                                    An anonymous, lightweight backend aggregator to compile publishing statistics. View global mod loader and MC version distribution charts.
                                </p>
                                <div class="card-footer-action">
                                    <a href="#telemetry" class="explore-link">
                                        Open Telemetry dashboard &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                    </div>
                </section>
            </div>
        `;
    }
};
