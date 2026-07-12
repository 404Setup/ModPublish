window.ModPublish = window.ModPublish || {};

window.ModPublish.router = {
    currentPage: null,
    routes: {
        'home': window.ModPublish.pages.home,
        'intellij': window.ModPublish.pages.intellij,
        'vscode': window.ModPublish.pages.vscode,
        'telemetry': window.ModPublish.pages.telemetry,
        'docs': window.ModPublish.pages.docs
    },

    init: function () {
        window.addEventListener('hashchange', () => this.handleRouting());
        this.handleRouting();
    },

    handleRouting: function () {
        const hash = window.location.hash.substring(1) || 'home';

        let page = this.routes[hash];
        if (!page) {
            console.warn(`Route #${hash} not found, redirecting to home.`);
            window.location.hash = '#home';
            return;
        }

        const brandLogo = document.getElementById('brand-logo');
        const brandText = document.getElementById('brand-text');
        const brandConfig = {
            'home': {icon: 'image/icon_generic.svg', text: 'ModPublish'},
            'intellij': {
                icon: 'image/icon_intellij.svg',
                text: 'ModPublish <span style="font-weight:400; font-size:0.9em; opacity:0.6; margin-left:4px;">IntelliJ</span>'
            },
            'vscode': {
                icon: 'image/icon_vscode.svg',
                text: 'ModPublish <span style="font-weight:400; font-size:0.9em; opacity:0.6; margin-left:4px;">VS Code</span>'
            },
            'telemetry': {
                icon: 'image/icon_telemetry.svg',
                text: 'ModPublish <span style="font-weight:400; font-size:0.9em; opacity:0.6; margin-left:4px;">Telemetry</span>'
            },
            'docs': {
                icon: 'image/icon_generic.svg',
                text: 'ModPublish <span style="font-weight:400; font-size:0.9em; opacity:0.6; margin-left:4px;">Docs</span>'
            }
        };

        if (brandLogo && brandText && brandConfig[hash]) {
            brandLogo.src = brandConfig[hash].icon;
            brandText.innerHTML = brandConfig[hash].text;
        }

        if (this.currentPage && typeof this.currentPage.destroy === 'function') {
            try {
                this.currentPage.destroy();
            } catch (e) {
                console.error("Error during page destruction:", e);
            }
        }

        this.currentPage = page;
        const appContainer = document.getElementById('app');
        if (appContainer) {
            appContainer.innerHTML = page.render();

            if (typeof page.init === 'function') {
                try {
                    page.init();
                } catch (e) {
                    console.error("Error during page initialization:", e);
                }
            }

            this.updateNavbarActiveLink(hash);
        }
    },

    updateNavbarActiveLink: function (hash) {
        const navLinks = document.querySelectorAll('.nav-menu a');
        navLinks.forEach(link => {
            const href = link.getAttribute('href');
            if (href === `#${hash}`) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }
};
