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

document.addEventListener('DOMContentLoaded', () => {
    if (window.ModPublish.router && typeof window.ModPublish.router.init === 'function') {
        window.ModPublish.router.init();
    } else {
        console.error("Router not loaded successfully!");
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
