window.ModPublish = window.ModPublish || {};
window.ModPublish.pages = window.ModPublish.pages || {};

window.ModPublish.pages.telemetry = {
    activeTab: 'dashboard',
    charts: {
        targets: null,
        loaders: null,
        mc: null
    },
    choices: {
        target: null,
        loader: null,
        mc: null
    },
    autoRefreshInterval: null,
    apiBaseUrl: '',

    render: function () {
        return `
            <div class="project-page-container animate-fade-in">
                <div class="project-banner-header">
                    <img src="image/banner_telemetry.svg" alt="ModPublish Telemetry Banner">
                </div>
                
                <div class="project-details-grid">
                    <aside class="project-meta-sidebar">
                        <div class="project-meta-card card">
                            <div class="meta-header">
                                <img src="image/icon_telemetry.svg" alt="Telemetry Icon" class="meta-icon">
                                <div>
                                    <h2>Telemetry</h2>
                                    <span class="badge badge-warning">Server</span>
                                </div>
                            </div>
                            <hr class="divider">
                            <div class="meta-links">
                                <div class="meta-link-item">
                                    <span class="meta-label">GitHub Repository</span>
                                    <a href="https://github.com/404Setup/ModPublish/tree/master/modpublish-telemetry" target="_blank" class="meta-value font-mono text-break" style="font-size:0.8rem;">
                                        404Setup/ModPublish
                                    </a>
                                </div>
                                <div class="meta-link-item">
                                    <span class="meta-label">License</span>
                                    <span class="meta-value font-mono">LGPL-3.0</span>
                                </div>
                            </div>
                            <div class="meta-actions">
                                <a href="https://github.com/404Setup/ModPublish/tree/master/modpublish-telemetry" target="_blank" class="btn btn-secondary btn-full">
                                    GitHub Project
                                </a>
                            </div>
                        </div>
                    </aside>
                    
                    <main class="project-main-content">
                        <section class="content-section">
                            <h1>ModPublish Telemetry System</h1>
                            <p class="lead">
                                A high-performance, lightweight Go backend designed to collect and aggregate anonymous publishing statistics.
                            </p>
                        </section>
                        <div class="docs-tabs-nav">
                            <button class="tab-btn active" id="tab-btn-dashboard">Analytics Dashboard</button>
                            <button class="tab-btn" id="tab-btn-about">System & Privacy</button>
                        </div>
                        <div id="tab-content-dashboard" class="tab-pane active">
                            <div class="api-status-banner card" id="api-status" style="display: none; margin-bottom: 20px;">
                                <div class="banner-content">
                                    <span class="pulse-indicator offline"></span>
                                    <p>Cannot connect to the telemetry API server. Please verify that the backend is running at <code id="configured-api-url"></code>.</p>
                                </div>
                            </div>

                            <div class="controls card" style="margin-bottom: 24px;">
                                <div class="filters-grid">
                                    <div class="input-group">
                                        <label for="filter-target">Publish Targets</label>
                                        <select id="filter-target" multiple></select>
                                    </div>
                                    <div class="input-group">
                                        <label for="filter-loader">Mod Loaders</label>
                                        <select id="filter-loader" multiple></select>
                                    </div>
                                    <div class="input-group">
                                        <label for="filter-mc">Minecraft Versions</label>
                                        <select id="filter-mc" multiple></select>
                                    </div>
                                </div>
                                <div class="controls-actions">
                                    <div class="button-group">
                                        <button id="btn-apply-filters" class="btn btn-primary">Apply Filters</button>
                                        <button id="btn-clear-filters" class="btn btn-secondary">Clear</button>
                                    </div>
                                    <div class="toggle-group">
                                        <label class="switch">
                                            <input type="checkbox" id="auto-refresh" checked>
                                            <span class="slider"></span>
                                        </label>
                                        <span class="toggle-label">Auto Refresh (5s)</span>
                                    </div>
                                </div>
                            </div>

                            <div class="dashboard-grid">
                                <div class="card stat-card">
                                    <h3>Total Publishing Requests</h3>
                                    <div class="stat-huge" id="total-requests">0</div>
                                    <p class="stat-label">Successful publish operations recorded</p>
                                </div>
                                <div class="card chart-card">
                                    <h3>Publish Targets</h3>
                                    <div class="chart-container">
                                        <canvas id="targetsChart"></canvas>
                                    </div>
                                </div>
                                <div class="card chart-card">
                                    <h3>Mod Loaders</h3>
                                    <div class="chart-container">
                                        <canvas id="loadersChart"></canvas>
                                    </div>
                                </div>
                                <div class="card chart-card">
                                    <h3>Minecraft Versions (Top 10)</h3>
                                    <div class="chart-container">
                                        <canvas id="mcChart"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="tab-content-about" class="tab-pane">
                            <div class="content-section">
                                <h2>Anonymous Data Collection</h2>
                                <p>
                                    ModPublish telemetry operates on strict anonymity policies. The server collects statistics solely to compile ecosystem trends, helping the community understand which mod loaders and Minecraft versions are active.
                                </p>
                                <h3>Collected Attributes</h3>
                                <ul class="feature-bullet-list">
                                    <li><strong>Publish Target ID:</strong> Which platform (Modrinth, CurseForge, GitHub, or GitLab) the mod was published to.</li>
                                    <li><strong>Mod Loader ID:</strong> Which modloader framework (Fabric, Forge, NeoForge, Quilt, etc.) is targetted.</li>
                                    <li><strong>Minecraft Version:</strong> The target Minecraft client version (e.g. <code>1.20.4</code>).</li>
                                </ul>
                                <div class="alert alert-info">
                                    <strong>Privacy Guarantee:</strong> No developer names, usernames, computer IDs, IP addresses, file names, or file hashes are persisted or logged in the database.
                                </div>
                            </div>

                             <div class="content-section">
                                <h2>CORS Proxy Service</h2>
                                <p>
                                    To fetch extension details (such as download counts, version details, and updates) directly from platforms like JetBrains Marketplace, the web application routes requests through a localized CORS proxy.
                                </p>
                                <h3>Proxy Guardrails</h3>
                                <ul class="feature-bullet-list">
                                    <li><strong>Flexible Whitelisting:</strong> Supports both exact domain matching and full URL prefix matching to restrict access to specific endpoints only.</li>
                                    <li><strong>SSRF Loopback Block:</strong> The proxy refuses to resolve or forward to localhost, private IP subnets, or local networking scopes to protect backend security.</li>
                                    <li><strong>IP-Based Rate Limiting:</strong> Enforces a default limit of 30 requests per 5 minutes per client IP (fully configurable) to prevent denial-of-service abuse.</li>
                                    <li><strong>5MB Size Limit:</strong> Rejects incoming client request bodies or target server response bodies larger than 5MB to prevent memory bloat/exhaustion.</li>
                                    <li><strong>6-Hour Smart Cache:</strong> Responses are cached for 6 hours, conserving target API rate limits and optimizing page load speeds.</li>
                                </ul>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        `;
    },

    init: function () {
        this.apiBaseUrl = window.ModPublish.config.apiBaseUrl || 'http://localhost:3000';

        const dashboardBtn = document.getElementById('tab-btn-dashboard');
        const aboutBtn = document.getElementById('tab-btn-about');

        dashboardBtn.addEventListener('click', () => this.switchTab('dashboard'));
        aboutBtn.addEventListener('click', () => this.switchTab('about'));

        this.switchTab(this.activeTab);
    },

    switchTab: function (tabName) {
        this.activeTab = tabName;

        const dashboardBtn = document.getElementById('tab-btn-dashboard');
        const aboutBtn = document.getElementById('tab-btn-about');
        const dashboardPane = document.getElementById('tab-content-dashboard');
        const aboutPane = document.getElementById('tab-content-about');

        if (tabName === 'dashboard') {
            dashboardBtn.classList.add('active');
            aboutBtn.classList.remove('active');
            dashboardPane.classList.add('active');
            aboutPane.classList.remove('active');

            this.initDashboard();
        } else {
            dashboardBtn.classList.remove('active');
            aboutBtn.classList.add('active');
            dashboardPane.classList.remove('active');
            aboutPane.classList.add('active');

            this.cleanupDashboard();
        }
    },

    initDashboard: function () {
        this.cleanupDashboard();

        document.getElementById('configured-api-url').innerText = this.apiBaseUrl;

        this.initChoices();
        this.initCharts();

        this.fetchMCVersions();
        this.fetchStats();

        document.getElementById('btn-apply-filters').addEventListener('click', () => this.fetchStats());
        document.getElementById('btn-clear-filters').addEventListener('click', () => this.clearFilters());

        this.setupAutoRefresh();
        document.getElementById('auto-refresh').addEventListener('change', () => this.setupAutoRefresh());
    },

    cleanupDashboard: function () {
        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
            this.autoRefreshInterval = null;
        }

        Object.keys(this.charts).forEach(key => {
            if (this.charts[key]) {
                this.charts[key].destroy();
                this.charts[key] = null;
            }
        });

        Object.keys(this.choices).forEach(key => {
            if (this.choices[key]) {
                this.choices[key].destroy();
                this.choices[key] = null;
            }
        });
    },

    destroy: function () {
        this.cleanupDashboard();
    },

    initChoices: function () {
        const targetMap = {'0': 'modrinth', '1': 'curseforge', '2': 'github', '3': 'gitlab'};
        const loaderMap = {
            '0': 'fabric',
            '1': 'quilt',
            '2': 'forge',
            '3': 'neoforge',
            '4': 'rift',
            '5': 'litemod',
            '6': 'javaagent'
        };

        const targetSelect = document.getElementById('filter-target');
        if (targetSelect) {
            Object.entries(targetMap).forEach(([val, label]) => {
                const opt = document.createElement('option');
                opt.value = val;
                opt.text = label;
                targetSelect.add(opt);
            });
            this.choices.target = new Choices('#filter-target', {
                removeItemButton: true,
                searchEnabled: false,
                placeholderValue: 'Select targets'
            });
        }

        const loaderSelect = document.getElementById('filter-loader');
        if (loaderSelect) {
            Object.entries(loaderMap).forEach(([val, label]) => {
                const opt = document.createElement('option');
                opt.value = val;
                opt.text = label;
                loaderSelect.add(opt);
            });
            this.choices.loader = new Choices('#filter-loader', {
                removeItemButton: true,
                searchEnabled: false,
                placeholderValue: 'Select loaders',
                shouldSort: false
            });
        }

        const mcSelect = document.getElementById('filter-mc');
        if (mcSelect) {
            this.choices.mc = new Choices('#filter-mc', {
                removeItemButton: true,
                searchEnabled: true,
                searchPlaceholderValue: 'Search MC version...',
                placeholderValue: 'Select versions',
                shouldSort: false
            });
        }
    },

    initCharts: function () {
        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: '#a1a1aa',
                        font: {family: "'Inter', sans-serif", size: 11}
                    }
                }
            }
        };

        const barOptions = {
            ...chartOptions,
            plugins: {
                legend: {display: false}
            },
            scales: {
                x: {
                    ticks: {color: '#71717a', font: {family: "'Inter', sans-serif", size: 10}},
                    grid: {color: '#27272a'}
                },
                y: {
                    ticks: {color: '#71717a', font: {family: "'Inter', sans-serif", size: 10}},
                    grid: {color: '#27272a'},
                    beginAtZero: true
                }
            }
        };

        const ctxT = document.getElementById('targetsChart')?.getContext('2d');
        if (ctxT) {
            this.charts.targets = new Chart(ctxT, {
                type: 'doughnut',
                data: {labels: [], datasets: []},
                options: {
                    ...chartOptions,
                    cutout: '65%'
                }
            });
        }

        const ctxL = document.getElementById('loadersChart')?.getContext('2d');
        if (ctxL) {
            this.charts.loaders = new Chart(ctxL, {
                type: 'bar',
                data: {labels: [], datasets: []},
                options: barOptions
            });
        }

        const ctxM = document.getElementById('mcChart')?.getContext('2d');
        if (ctxM) {
            this.charts.mc = new Chart(ctxM, {
                type: 'bar',
                data: {labels: [], datasets: []},
                options: barOptions
            });
        }
    },

    fetchMCVersions: function () {
        fetch(`${this.apiBaseUrl}/api/mc_versions`)
            .then(res => {
                if (!res.ok) throw new Error('API offline');
                return res.json();
            })
            .then(versions => {
                if (versions && this.choices.mc) {
                    const choicesList = versions.map(v => ({value: v, label: v}));
                    this.choices.mc.setChoices(choicesList, 'value', 'label', true);
                    this.showApiStatus(true);
                }
            })
            .catch(err => {
                console.error("Failed to fetch MC versions:", err);
                this.showApiStatus(false);
            });
    },

    fetchStats: async function () {
        const target = this.choices.target ? this.choices.target.getValue(true).join(',') : '';
        const loader = this.choices.loader ? this.choices.loader.getValue(true).join(',') : '';
        const mc = this.choices.mc ? this.choices.mc.getValue(true).join(',') : '';

        const params = new URLSearchParams();
        if (target) params.append('target', target);
        if (loader) params.append('loader', loader);
        if (mc) params.append('mc_version', mc);

        try {
            const response = await fetch(`${this.apiBaseUrl}/api/stats?${params.toString()}`);
            if (!response.ok) throw new Error('API offline');
            const data = await response.json();

            const requestsEl = document.getElementById('total-requests');
            if (requestsEl) requestsEl.innerText = data.total_requests.toLocaleString();

            const targetMap = {'0': 'modrinth', '1': 'curseforge', '2': 'github', '3': 'gitlab'};
            const loaderMap = {
                '0': 'fabric',
                '1': 'quilt',
                '2': 'forge',
                '3': 'neoforge',
                '4': 'rift',
                '5': 'litemod',
                '6': 'javaagent'
            };

            const colors = [
                '#6366f1',
                '#0ea5e9',
                '#f43f5e',
                '#10b981',
                '#f59e0b',
                '#8b5cf6',
                '#ec4899'
            ];

            this.updateChart(this.charts.targets, data.publish_targets, 'doughnut', colors, null, targetMap);
            this.updateChart(this.charts.loaders, data.loaders, 'bar', '#6366f1', null, loaderMap);
            this.updateChart(this.charts.mc, data.minecraft_versions, 'bar', '#10b981', 10);

            this.showApiStatus(true);
        } catch (error) {
            console.error("Failed to fetch stats:", error);
            this.showApiStatus(false);
        }
    },

    updateChart: function (chart, dataMap, type, colors, limit = null, labelMap = null) {
        if (!chart) return;
        let entries = Object.entries(dataMap || {});
        entries.sort((a, b) => b[1] - a[1]);

        if (limit) {
            entries = entries.slice(0, limit);
        }

        const labels = entries.map(e => labelMap ? (labelMap[e[0]] || e[0]) : e[0]);
        const data = entries.map(e => e[1]);

        chart.data = {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: Array.isArray(colors) ? colors.slice(0, labels.length) : colors,
                borderWidth: 1,
                borderColor: '#18181b',
                borderRadius: type === 'bar' ? 4 : 0
            }]
        };
        chart.update();
    },

    clearFilters: function () {
        if (this.choices.target) this.choices.target.removeActiveItems();
        if (this.choices.loader) this.choices.loader.removeActiveItems();
        if (this.choices.mc) this.choices.mc.removeActiveItems();
        this.fetchStats();
    },

    setupAutoRefresh: function () {
        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
            this.autoRefreshInterval = null;
        }

        const autoEl = document.getElementById('auto-refresh');
        if (autoEl && autoEl.checked) {
            this.autoRefreshInterval = setInterval(() => {
                this.fetchStats();
            }, 5000);
        }
    },

    showApiStatus: function (online) {
        const banner = document.getElementById('api-status');
        if (!banner) return;
        if (online) {
            banner.style.display = 'none';
        } else {
            banner.style.display = 'block';
        }
    }
};
