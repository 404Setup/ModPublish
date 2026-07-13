window.ModPublish = window.ModPublish || {};

window.ModPublish.initCodeCopyButtons = function () {
    const preElements = document.querySelectorAll('pre');
    preElements.forEach(pre => {
        if (pre.querySelector('.copy-code-btn')) return;
        const code = pre.querySelector('code');
        if (!code) return;

        pre.style.position = 'relative';

        const btn = document.createElement('button');
        btn.className = 'copy-code-btn';
        btn.type = 'button';
        btn.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
            <span>Copy</span>
        `;

        btn.addEventListener('click', () => {
            const text = code.innerText;
            navigator.clipboard.writeText(text).then(() => {
                btn.classList.add('copied');
                btn.innerHTML = `
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                    <span>Copied!</span>
                `;
                setTimeout(() => {
                    btn.classList.remove('copied');
                    btn.innerHTML = `
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
                        <span>Copy</span>
                    `;
                }, 2000);
            }).catch(err => {
                console.error('Failed to copy code: ', err);
            });
        });
        pre.appendChild(btn);
    });
};

window.ModPublish.closeLightbox = function () {
    const lightbox = document.getElementById('docs-image-lightbox');
    if (lightbox && lightbox.classList.contains('open')) {
        lightbox.classList.add('closing');
        lightbox.classList.remove('open');
        setTimeout(() => {
            lightbox.classList.remove('closing');
            lightbox.style.display = 'none';
        }, 300);
    }
};

window.ModPublish.openLightbox = function (src, alt) {
    let lightbox = document.getElementById('docs-image-lightbox');
    if (!lightbox) {
        lightbox = document.createElement('div');
        lightbox.id = 'docs-image-lightbox';
        lightbox.className = 'image-lightbox';
        lightbox.innerHTML = `
            <button class="image-lightbox-close" aria-label="Close image">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
            </button>
            <img class="image-lightbox-content" src="" alt="">
        `;
        document.body.appendChild(lightbox);

        lightbox.querySelector('.image-lightbox-close').addEventListener('click', (e) => {
            e.stopPropagation();
            window.ModPublish.closeLightbox();
        });

        lightbox.addEventListener('click', (e) => {
            if (e.target === lightbox) {
                window.ModPublish.closeLightbox();
            }
        });
    }

    const img = lightbox.querySelector('.image-lightbox-content');
    img.src = src;
    img.alt = alt || 'Zoomed Image';

    lightbox.style.display = 'flex';
    // Trigger reflow
    lightbox.offsetHeight;
    lightbox.classList.add('open');
};

window.ModPublish.initImageZoom = function () {
    const contentPane = document.getElementById('docs-content-pane');
    if (!contentPane) return;

    if (contentPane.dataset.zoomInitialized) return;
    contentPane.dataset.zoomInitialized = 'true';

    contentPane.addEventListener('click', (e) => {
        const target = e.target;
        if (target.tagName === 'IMG') {
            e.preventDefault();
            window.ModPublish.openLightbox(target.src, target.alt);
        }
    });
};

window.ModPublish.getCookie = function (name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
};

window.ModPublish.setCookie = function (name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    const expires = "; expires=" + date.toUTCString();
    document.cookie = name + "=" + (value || "") + expires + "; path=/; SameSite=Lax";
};

window.ModPublish.fetchWithFallback = async function (url, options = {}) {
    const apiBaseUrl = window.ModPublish.config?.apiBaseUrl || 'http://localhost:3000';
    const proxyUrl = `${apiBaseUrl}/api/proxy?url=${encodeURIComponent(url)}`;

    const fetchWithTimeout = async (targetUrl) => {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 5000);

        if (options.signal) {
            if (options.signal.aborted) {
                clearTimeout(timeoutId);
                throw new DOMException('Aborted', 'AbortError');
            }
            options.signal.addEventListener('abort', () => {
                clearTimeout(timeoutId);
                controller.abort();
            });
        }

        try {
            const response = await fetch(targetUrl, {
                ...options,
                signal: controller.signal
            });
            clearTimeout(timeoutId);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response;
        } catch (err) {
            clearTimeout(timeoutId);
            throw err;
        }
    };

    try {
        return await fetchWithTimeout(proxyUrl);
    } catch (proxyError) {
        if (proxyError.name === 'AbortError' && (!options.signal || options.signal.aborted)) {
            throw proxyError;
        }
        console.warn(`CORS proxy failed for ${url}, falling back to direct request:`, proxyError);
        return await fetchWithTimeout(url);
    }
};

window.ModPublish.loadCommitHash = function () {
    const cachedHash = window.ModPublish.getCookie('modpublish_commit_hash');
    const footerRight = document.getElementById('footer-commit-info');
    if (!footerRight) return;

    if (cachedHash) {
        const parts = cachedHash.split(':');
        if (parts.length === 2) {
            const sha = parts[0];
            const shortSha = parts[1];
            footerRight.innerHTML = `version <a href="https://github.com/404Setup/ModPublish/commit/${sha}" target="_blank" class="commit-link">${shortSha}</a>`;
            footerRight.style.display = 'block';
            return;
        }
    }

    window.ModPublish.fetchWithFallback('https://api.github.com/repos/404Setup/ModPublish/commits')
        .then(res => res.json())
        .then(async (commits) => {
            if (!Array.isArray(commits) || commits.length === 0) return;

            const checkRunsPromises = commits.slice(0, 5).map(commit => {
                const sha = commit.sha;
                const checkRunsUrl = `https://api.github.com/repos/404Setup/ModPublish/commits/${sha}/check-runs`;
                return window.ModPublish.fetchWithFallback(checkRunsUrl)
                    .then(res => res.json())
                    .then(data => ({ sha, commit, data }))
                    .catch(err => {
                        console.warn(`Failed to fetch check runs for commit ${sha}:`, err);
                        return null;
                    });
            });

            const results = await Promise.all(checkRunsPromises);

            const match = results.find(res => {
                if (!res || !res.data || !res.data.check_runs) return false;
                return res.data.check_runs.some(cr =>
                    cr.status === 'completed' &&
                    cr.conclusion === 'success' &&
                    cr.app &&
                    cr.app.slug !== 'github-actions' &&
                    cr.app.name !== 'GitHub Actions'
                );
            });

            if (match) {
                const sha = match.sha;
                const shortSha = sha.substring(0, 7);
                window.ModPublish.setCookie('modpublish_commit_hash', `${sha}:${shortSha}`, 1);
                footerRight.innerHTML = `version <a href="https://github.com/404Setup/ModPublish/commit/${sha}" target="_blank" class="commit-link">${shortSha}</a>`;
                footerRight.style.display = 'block';
            }
        })
        .catch(err => {
            console.error('Failed to load commit hash:', err);
        });
};

document.addEventListener('DOMContentLoaded', () => {
    if (window.ModPublish.router && typeof window.ModPublish.router.init === 'function') {
        window.ModPublish.router.init();
    } else {
        console.error("Router not loaded successfully!");
    }

    try {
        window.ModPublish.loadCommitHash();
    } catch (e) {
        console.error("Failed to trigger loadCommitHash:", e);
    }

    document.addEventListener('dragstart', (e) => {
        const target = e.target;
        if (target.tagName === 'IMG' || target.tagName === 'A' || target.closest('a')) {
            e.preventDefault();
        }
    });

    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            window.ModPublish.closeLightbox();
        }
    });
});
