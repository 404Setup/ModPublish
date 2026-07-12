window.ModPublish = window.ModPublish || {};

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
});
