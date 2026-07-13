window.ModPublish = window.ModPublish || {};
window.ModPublish.pages = window.ModPublish.pages || {};

window.ModPublish.pages.docs = {
    activeSection: 'overview',

    render: function() {
        return `
            <div class="docs-container animate-fade-in">
                <div class="mobile-docs-header">
                    <button class="btn btn-secondary mobile-toggle-btn" id="docs-menu-toggle">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" style="display:inline-block; vertical-align:middle; margin-right:4px;">
                            <line x1="3" y1="12" x2="21" y2="12"></line>
                            <line x1="3" y1="6" x2="21" y2="6"></line>
                            <line x1="3" y1="18" x2="21" y2="18"></line>
                        </svg>
                        Docs Navigation
                    </button>
                    <span class="mobile-docs-title" id="mobile-docs-title">Overview</span>
                </div>

                <aside class="docs-sidebar" id="docs-sidebar-menu">
                    <nav class="docs-nav">
                        <div class="docs-nav-group">
                            <span class="docs-nav-title">Getting Started</span>
                            <ul>
                                <li><a href="#docs" class="docs-nav-link" data-section="overview">Overview</a></li>
                                <li><a href="#docs" class="docs-nav-link" data-section="config-guide">Configuration Guide</a></li>
                            </ul>
                        </div>
                        <div class="docs-nav-group">
                            <span class="docs-nav-title">IntelliJ IDEA Edition</span>
                            <ul>
                                <li><a href="#docs" class="docs-nav-link" data-section="intellij-intro">Introduction</a></li>
                                <li><a href="#docs" class="docs-nav-link" data-section="intellij-build">Build from Source</a></li>
                            </ul>
                        </div>
                        <div class="docs-nav-group">
                            <span class="docs-nav-title">VS Code Edition</span>
                            <ul>
                                <li><a href="#docs" class="docs-nav-link" data-section="vscode-intro">Introduction</a></li>
                                <li><a href="#docs" class="docs-nav-link" data-section="vscode-build">Build & Package</a></li>
                            </ul>
                        </div>
                        <div class="docs-nav-group">
                            <span class="docs-nav-title">Telemetry Backend</span>
                            <ul>
                                <li><a href="#docs" class="docs-nav-link" data-section="telemetry-info">Overview & Specs</a></li>
                                <li><a href="#docs" class="docs-nav-link" data-section="telemetry-api">API Reference</a></li>
                            </ul>
                        </div>
                    </nav>
                </aside>
                <main class="docs-content" id="docs-content-pane">
                </main>
            </div>
        `;
    },

    init: function() {
        const toggleBtn = document.getElementById('docs-menu-toggle');
        const sidebarMenu = document.getElementById('docs-sidebar-menu');

        if (toggleBtn && sidebarMenu) {
            toggleBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                sidebarMenu.classList.toggle('open');
            });

            document.addEventListener('click', (e) => {
                if (sidebarMenu.classList.contains('open') && !sidebarMenu.contains(e.target) && e.target !== toggleBtn) {
                    sidebarMenu.classList.remove('open');
                }
            });
        }

        const links = document.querySelectorAll('.docs-nav-link');
        links.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = link.getAttribute('data-section');
                this.showSection(section);

                if (sidebarMenu) {
                    sidebarMenu.classList.remove('open');
                }
            });
        });

        this.showSection(this.activeSection);
    },

    destroy: function() {
    },

    showSection: function(sectionId) {
        this.activeSection = sectionId;

        const links = document.querySelectorAll('.docs-nav-link');
        links.forEach(link => {
            if (link.getAttribute('data-section') === sectionId) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        const mobileTitle = document.getElementById('mobile-docs-title');
        const activeLink = document.querySelector(`.docs-nav-link[data-section="${sectionId}"]`);
        if (mobileTitle && activeLink) {
            mobileTitle.innerText = activeLink.innerText;
        }

        const contentPane = document.getElementById('docs-content-pane');
        if (contentPane) {
            contentPane.innerHTML = `
                <div class="docs-article animate-fade-in" style="display: flex; justify-content: center; align-items: center; min-height: 200px; color: var(--text-secondary);">
                    <div style="text-align: center;">
                        <div class="spinner" style="border: 2px solid rgba(255,255,255,0.1); border-top: 2px solid #818cf8; border-radius: 50%; width: 24px; height: 24px; animation: spin 0.8s linear infinite; margin: 0 auto 10px;"></div>
                        Loading Documentation...
                    </div>
                </div>
            `;
            contentPane.scrollTop = 0;

            if (!document.getElementById('docs-spinner-style')) {
                const style = document.createElement('style');
                style.id = 'docs-spinner-style';
                style.innerHTML = `@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`;
                document.head.appendChild(style);
            }

            fetch(`docs/${sectionId}.md`)
                .then(res => {
                    if (!res.ok) throw new Error(`HTTP ${res.status}: Failed to load "${sectionId}.md"`);
                    return res.text();
                })
                .then(markdown => {
                    if (this.activeSection !== sectionId) return;

                    const html = typeof marked !== 'undefined' ? marked.parse(markdown) : markdown;
                    contentPane.innerHTML = `<div class="docs-article animate-fade-in">${html}</div>`;
                    
                    if (window.ModPublish && typeof window.ModPublish.initCodeCopyButtons === 'function') {
                        window.ModPublish.initCodeCopyButtons();
                    }
                })
                .catch(err => {
                    console.error(err);
                    if (this.activeSection !== sectionId) return;
                    contentPane.innerHTML = `
                        <div class="docs-article animate-fade-in">
                            <h1>Error Loading Document</h1>
                            <p>Unable to retrieve the documentation file. Please check your network connection or ensure the file exists.</p>
                            <div class="alert" style="background: rgba(239, 68, 68, 0.05); border: 1px solid rgba(239, 68, 68, 0.15); color: #f87171; margin-top: 16px;">
                                <strong>Error Detail:</strong> ${err.message}
                            </div>
                        </div>
                    `;
                });
        }
    }
};

