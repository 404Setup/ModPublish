/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

const vscode = acquireVsCodeApi();

function t(key, ...args) {
    const translations = window.I18N || {};
    let val = translations[key] || key;
    if (args && args.length > 0) {
        for (let i = 0; i < args.length; i++) {
            val = val.replace(`{${i}}`, args[i]);
        }
    }
    return val;
}

let dependencyList = [];
let allMinecraftVersions = [];
let editingIndex = -1;

const form = document.getElementById('publish-form');
const primaryFileSelect = document.getElementById('primary-file');
const versionNameInput = document.getElementById('version-name');
const versionNumberInput = document.getElementById('version-number');
const releaseChannelSelect = document.getElementById('release-channel');
const supportClientCheckbox = document.getElementById('support-client');
const supportServerCheckbox = document.getElementById('support-server');
const searchInput = document.getElementById('version-search');
const showSnapshotsCheckbox = document.getElementById('show-snapshots');
const mcVersionsContainer = document.getElementById('version-list');
const dependenciesContainer = document.getElementById('dependency-list');
const depEmptyEl = document.getElementById('dep-empty');
const changelogTextarea = document.getElementById('changelog');

const depModal = document.getElementById('dep-modal');
const btnAddDependency = document.getElementById('btn-add-dependency');
const btnModalCancel = document.getElementById('btn-modal-cancel');
const btnModalOk = document.getElementById('btn-modal-ok');
const depModrinthInput = document.getElementById('dep-modrinth-id');
const depCurseforgeInput = document.getElementById('dep-curseforge-id');
const depTypeSelect = document.getElementById('dep-type');
const depModalStatus = document.getElementById('dep-modal-status');
const depModalStatusText = document.getElementById('dep-modal-status-text');

const btnCancel = document.getElementById('btn-cancel');
const btnSave = document.getElementById('btn-save');
const btnPublish = document.getElementById('btn-publish');

const statusBar = document.getElementById('status-bar');
const statusText = document.getElementById('status-text');

function init() {
    localizeUI();
    loadFiles();
    loadVersions();
    loadPrefilledData();
    setupEventListeners();
    renderDependencies();
}

function localizeUI() {
    document.getElementById('title-main').textContent = t('action.modpublish.action.publish.text');
    document.getElementById('lbl-primary-file').textContent = t('component.name.primary-file');
    const descPrimaryFile = document.getElementById('desc-primary-file');
    if (descPrimaryFile) {
        descPrimaryFile.textContent = t('component.desc.primary-file');
    }
    document.getElementById('lbl-version-name').textContent = t('component.name.version-name');
    document.getElementById('lbl-version-number').textContent = t('component.name.version-number');
    document.getElementById('lbl-release-channel').textContent = t('component.name.release-channel');
    document.getElementById('lbl-support-target').textContent = t('dialog.modpublish.publish.support.title');
    document.getElementById('lbl-client').textContent = t('dialog.modpublish.publish.support.client');
    document.getElementById('lbl-server').textContent = t('dialog.modpublish.publish.support.server');
    document.getElementById('lbl-targets').textContent = t('component.name.targets');
    document.getElementById('lbl-loaders').textContent = t('component.name.loaders');
    document.getElementById('lbl-mc-version').textContent = t('component.name.mc-version');
    document.getElementById('lbl-show-snapshots').textContent = t('component.name.snapshot');
    document.getElementById('lbl-dependencies').textContent = t('component.name.dependencies');
    document.getElementById('btn-add-dependency').textContent = t('title.add-dependency');
    document.getElementById('lbl-changelog').textContent = t('component.name.changelog');
    
    document.getElementById('version-search').placeholder = t('component.desc.filter-versions');
    document.getElementById('dep-empty').textContent = t('component.desc.dep-empty');
    document.getElementById('changelog').placeholder = t('component.desc.changelog');

    document.getElementById('lbl-add-dependency-title').textContent = t('title.add-dependency');
    document.getElementById('lbl-dep-modrinth-id').textContent = t('component.name.depend-id.modrinth');
    document.getElementById('lbl-dep-curseforge-id').textContent = t('component.name.depend-id.curseforge');
    document.getElementById('lbl-dep-type').textContent = t('component.name.depend-status');
    document.getElementById('btn-modal-cancel').textContent = t('button.cancel');
    document.getElementById('btn-modal-ok').textContent = t('button.save');

    document.getElementById('btn-cancel').textContent = t('button.cancel');
    document.getElementById('btn-save').textContent = t('button.save');
    document.getElementById('btn-publish').textContent = t('button.publish');

    const btnUpdate = document.getElementById('btn-update-versions');
    if (btnUpdate) {
        btnUpdate.textContent = t('component.name.update-version-list');
        btnUpdate.title = t('component.tooltip.update-version-list');
    }

    const btnClear = document.getElementById('btn-clear-version-cache');
    if (btnClear) {
        btnClear.textContent = t('component.name.reset-version-list');
        btnClear.title = t('component.tooltip.reset-version-list');
    }
}

function loadFiles() {
    const files = window.FILES || [];
    const subtitle = document.getElementById('subtitle-files');
    subtitle.innerHTML = t('subtitle.files', files.length) + ': <br>' + files.map(f => `<code>${f.split(/[/\\]/).pop()}</code>`).join(', ');

    primaryFileSelect.innerHTML = '';
    files.forEach(f => {
        const opt = document.createElement('option');
        opt.value = f;
        opt.textContent = f.split(/[/\\]/).pop();
        primaryFileSelect.appendChild(opt);
    });
}

function loadVersions() {
    allMinecraftVersions = window.MINECRAFT_VERSIONS || [];
    filterAndRenderVersions();
}

function filterAndRenderVersions() {
    const searchText = searchInput.value.toLowerCase();
    const showSnapshots = showSnapshotsCheckbox.checked;

    mcVersionsContainer.innerHTML = '';

    const filtered = allMinecraftVersions.filter(v => {
        const matchesSearch = (v.v || '').toLowerCase().includes(searchText);
        const matchesSnapshot = showSnapshots || (v.t || 'release') === 'release';
        return matchesSearch && matchesSnapshot;
    });

    filtered.sort((a, b) => {
        const dateA = a.d ? new Date(a.d).getTime() : 0;
        const dateB = b.d ? new Date(b.d).getTime() : 0;
        return dateB - dateA;
    });

    const selectedSet = new Set(window.SELECTED_MC_VERSIONS || []);

    filtered.forEach(v => {
        const label = document.createElement('label');
        label.className = 'version-item checkbox-container';

        const cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.value = v.v;
        cb.checked = selectedSet.has(v.v);
        cb.addEventListener('change', () => {
            if (cb.checked) {
                selectedSet.add(v.v);
            } else {
                selectedSet.delete(v.v);
            }
            window.SELECTED_MC_VERSIONS = Array.from(selectedSet);
        });

        const span = document.createElement('span');
        span.className = 'checkbox-label';
        span.textContent = v.v;

        label.appendChild(cb);
        label.appendChild(span);

        if (v.t === 'snapshot') {
            const badge = document.createElement('span');
            badge.className = 'type-badge';
            badge.textContent = 'Snapshot';
            label.appendChild(badge);
        } else if (v.t === 'old_beta') {
            const badge = document.createElement('span');
            badge.className = 'type-badge';
            badge.textContent = 'Old Beta';
            label.appendChild(badge);
        } else if (v.t === 'old_alpha') {
            const badge = document.createElement('span');
            badge.className = 'type-badge';
            badge.textContent = 'Old Alpha';
            label.appendChild(badge);
        }

        mcVersionsContainer.appendChild(label);
    });
}

function loadPrefilledData() {
    const info = window.MOD_INFO || {};
    const config = window.CONFIG || {};

    versionNameInput.value = info.name ? `${info.name} ${info.version}` : '';
    versionNumberInput.value = info.version || '';

    if (info.publishTypes && info.publishTypes.length > 0) {
        info.publishTypes.forEach(type => {
            let id = type.toLowerCase();
            if (id === 'liteloader') id = 'litemod';
            const cb = document.getElementById(`loader-${id}`);
            if (cb) cb.checked = true;
        });
    }

    if (info.sideType === 'CLIENT') {
        supportClientCheckbox.checked = true;
        supportServerCheckbox.checked = false;
    } else if (info.sideType === 'SERVER') {
        supportClientCheckbox.checked = false;
        supportServerCheckbox.checked = true;
    } else {
        supportClientCheckbox.checked = true;
        supportServerCheckbox.checked = true;
    }

    const targets = ['modrinth', 'curseforge', 'github', 'gitlab'];
    targets.forEach(target => {
        const isConfigured = !!config[`${target}Configured`];
        const cb = document.getElementById(`target-${target}`);
        if (cb) {
            cb.checked = isConfigured;
            if (!isConfigured) {
                cb.disabled = true;
                cb.parentElement.style.opacity = '0.5';
                cb.parentElement.style.pointerEvents = 'none';
                cb.parentElement.title = (target === 'modrinth' || target === 'curseforge') ?
                    t(`tooltip.${target}.disable`) :
                    t('tooltip.git.disable', target.toUpperCase());
            } else {
                cb.disabled = false;
                cb.parentElement.style.opacity = '1';
                cb.parentElement.style.pointerEvents = 'auto';
                cb.parentElement.title = '';
            }
        }
    });

    if (config.dependencies) {
        dependencyList = config.dependencies;
    }

    if (config.releaseChannel) {
        releaseChannelSelect.value = config.releaseChannel;
    }
}

function showNotification(text, type = 'info') {
    vscode.postMessage({
        command: 'showNotification',
        type: type,
        text: text
    });
}

function openDependencyModal(index) {
    editingIndex = index;
    const dep = dependencyList[index];

    const isModrinthChecked = document.getElementById('target-modrinth').checked;
    const isCurseForgeChecked = document.getElementById('target-curseforge').checked;

    const pid = dep.projectId || "";
    let mId = "";
    let cId = "";
    if (pid.includes(',')) {
        const parts = pid.split(',');
        mId = parts[0] ? parts[0].trim() : "";
        cId = parts[1] ? parts[1].trim() : "";
    } else {
        if (isModrinthChecked) mId = pid.trim();
        if (isCurseForgeChecked) cId = pid.trim();
    }

    depModrinthInput.value = mId;
    depCurseforgeInput.value = cId;
    depTypeSelect.value = dep.type;

    depModalStatus.classList.add('hidden');

    document.getElementById('lbl-add-dependency-title').textContent = t('button.edit');

    const modrinthGroup = document.getElementById('dep-modrinth-group');
    if (isModrinthChecked) {
        modrinthGroup.classList.remove('hidden');
        const tokenAvail = window.CONFIG.modrinthTokenAvailable;
        depModrinthInput.disabled = !tokenAvail;
        depModrinthInput.title = tokenAvail ? "" : t('tooltip.modrinth.disable');
    } else {
        modrinthGroup.classList.add('hidden');
    }

    const curseforgeGroup = document.getElementById('dep-curseforge-group');
    if (isCurseForgeChecked) {
        curseforgeGroup.classList.remove('hidden');
        const tokenAvail = window.CONFIG.curseforgeStudioTokenAvailable;
        depCurseforgeInput.disabled = !tokenAvail;
        depCurseforgeInput.title = tokenAvail ? "" : t('failed.11');
    } else {
        curseforgeGroup.classList.add('hidden');
    }

    depTypeSelect.disabled = false;
    btnModalOk.disabled = false;
    btnModalCancel.disabled = false;

    depModal.classList.remove('hidden');
}

function setupEventListeners() {
    searchInput.addEventListener('input', filterAndRenderVersions);
    showSnapshotsCheckbox.addEventListener('change', filterAndRenderVersions);

    const btnUpdate = document.getElementById('btn-update-versions');
    const btnClear = document.getElementById('btn-clear-version-cache');

    if (btnUpdate) {
        btnUpdate.addEventListener('click', () => {
            showStatus(t('message.updating'));
            btnUpdate.disabled = true;
            vscode.postMessage({command: 'updateVersionList'});
        });
    }

    if (btnClear) {
        btnClear.addEventListener('click', () => {
            vscode.postMessage({
                command: 'clearVersionListCache',
                text: t('component.tooltip.reset-version-list')
            });
        });
    }

    btnAddDependency.addEventListener('click', () => {
        const isModrinthChecked = document.getElementById('target-modrinth').checked;
        const isCurseForgeChecked = document.getElementById('target-curseforge').checked;

        if (!isModrinthChecked && !isCurseForgeChecked) {
            const isGitChecked = document.getElementById('target-github').checked || document.getElementById('target-gitlab').checked;
            showNotification(t(isGitChecked ? 'message.dont-support-add-depends' : 'failed.8'), 'warning');
            return;
        }

        editingIndex = -1;
        depModrinthInput.value = '';
        depCurseforgeInput.value = '';
        depModalStatus.classList.add('hidden');
        document.getElementById('lbl-add-dependency-title').textContent = t('title.add-dependency');

        const modrinthGroup = document.getElementById('dep-modrinth-group');
        if (isModrinthChecked) {
            modrinthGroup.classList.remove('hidden');
            const tokenAvail = window.CONFIG.modrinthTokenAvailable;
            depModrinthInput.disabled = !tokenAvail;
            depModrinthInput.title = tokenAvail ? "" : t('tooltip.modrinth.disable');
        } else {
            modrinthGroup.classList.add('hidden');
        }

        const curseforgeGroup = document.getElementById('dep-curseforge-group');
        if (isCurseForgeChecked) {
            curseforgeGroup.classList.remove('hidden');
            const tokenAvail = window.CONFIG.curseforgeStudioTokenAvailable;
            depCurseforgeInput.disabled = !tokenAvail;
            depCurseforgeInput.title = tokenAvail ? "" : t('failed.11');
        } else {
            curseforgeGroup.classList.add('hidden');
        }

        depTypeSelect.disabled = false;
        btnModalOk.disabled = false;
        btnModalCancel.disabled = false;

        depModal.classList.remove('hidden');
        if (isModrinthChecked && window.CONFIG.modrinthTokenAvailable) {
            depModrinthInput.focus();
        } else if (isCurseForgeChecked && window.CONFIG.curseforgeStudioTokenAvailable) {
            depCurseforgeInput.focus();
        }
    });

    btnModalCancel.addEventListener('click', () => {
        depModal.classList.add('hidden');
    });

    btnModalOk.addEventListener('click', () => {
        const mId = depModrinthInput.value.trim();
        const cId = depCurseforgeInput.value.trim();
        const type = depTypeSelect.value;

        const isModrinthChecked = document.getElementById('target-modrinth').checked;
        const isCurseForgeChecked = document.getElementById('target-curseforge').checked;
        const modrinthTokenAvail = window.CONFIG.modrinthTokenAvailable;
        const curseforgeTokenAvail = window.CONFIG.curseforgeStudioTokenAvailable;

        if (isModrinthChecked && modrinthTokenAvail && !mId && isCurseForgeChecked && curseforgeTokenAvail && !cId) {
            showNotification(t('failed.9'), 'warning');
            return;
        }
        if (isModrinthChecked && modrinthTokenAvail && !mId && (!isCurseForgeChecked || !curseforgeTokenAvail)) {
            showNotification(t('failed.9'), 'warning');
            return;
        }
        if ((!isModrinthChecked || !modrinthTokenAvail) && isCurseForgeChecked && curseforgeTokenAvail && !cId) {
            showNotification(t('failed.9'), 'warning');
            return;
        }

        let projectId;
        if (isModrinthChecked && isCurseForgeChecked) {
            if (mId === cId && mId) {
                projectId = mId;
            } else {
                projectId = `${mId},${cId}`;
            }
        } else if (isModrinthChecked) {
            projectId = mId;
        } else {
            projectId = cId;
        }

        if (editingIndex >= 0) {
            const existing = dependencyList[editingIndex];
            if (existing.projectId === projectId) {
                existing.type = type;
                depModal.classList.add('hidden');
                renderDependencies();
                return;
            }
        }

        depModrinthInput.disabled = true;
        depCurseforgeInput.disabled = true;
        depTypeSelect.disabled = true;
        btnModalOk.disabled = true;
        btnModalCancel.disabled = true;
        depModalStatus.classList.remove('hidden');
        depModalStatusText.textContent = "Validating...";

        const dep = {
            projectId: projectId,
            type: type
        };

        vscode.postMessage({
            command: 'resolveDependency',
            dependency: dep
        });
    });

    btnCancel.addEventListener('click', () => {
        vscode.postMessage({command: 'cancel'});
    });

    btnSave.addEventListener('click', () => {
        const formData = getFormData();
        vscode.postMessage({
            command: 'saveConfig',
            data: formData
        });
    });

    form.addEventListener('submit', (e) => {
        e.preventDefault();

        const formData = getFormData();

        if (formData.targets.length === 0) {
            showNotification(t('failed.1'), 'warning');
            return;
        }
        if (!formData.clientRequired && !formData.serverRequired) {
            showNotification(t('failed.2'), 'warning');
            return;
        }
        if (formData.loaders.length === 0) {
            showNotification(t('failed.3'), 'warning');
            return;
        }
        if (formData.minecraftVersions.length === 0) {
            showNotification(t('failed.4'), 'warning');
            return;
        }

        showStatus(t('button.publishing'));
        btnPublish.disabled = true;

        vscode.postMessage({
            command: 'publish',
            data: formData
        });
    });
}

function getFormData() {
    const targets = [];
    ['modrinth', 'curseforge', 'github', 'gitlab'].forEach(t => {
        if (document.getElementById(`target-${t}`).checked) {
            targets.push(t);
        }
    });

    const loaders = [];
    ['fabric', 'quilt', 'forge', 'neoforge', 'rift', 'litemod', 'javaagent'].forEach(l => {
        if (document.getElementById(`loader-${l}`).checked) {
            loaders.push(l);
        }
    });

    return {
        primaryFile: primaryFileSelect.value,
        versionName: versionNameInput.value.trim(),
        versionNumber: versionNumberInput.value.trim(),
        releaseChannel: releaseChannelSelect.value,
        clientRequired: supportClientCheckbox.checked,
        serverRequired: supportServerCheckbox.checked,
        loaders: loaders,
        minecraftVersions: window.SELECTED_MC_VERSIONS || [],
        changelog: changelogTextarea.value,
        dependencies: dependencyList,
        targets: targets
    };
}

function renderDependencies() {
    dependenciesContainer.innerHTML = '';

    if (dependencyList.length === 0) {
        depEmptyEl.classList.remove('hidden');
        dependenciesContainer.appendChild(depEmptyEl);
        return;
    }

    depEmptyEl.classList.add('hidden');
    dependencyList.forEach((dep, index) => {
        const item = document.createElement('div');
        item.className = 'dep-item';

        const info = document.createElement('div');
        info.className = 'dep-info';

        const name = document.createElement('div');
        name.className = 'dep-name';

        let displayName = dep.customTitle || dep.projectId;
        if (dep.modrinthModInfo?.title || dep.curseforgeModInfo?.title) {
            const mTitle = dep.modrinthModInfo?.title;
            const cTitle = dep.curseforgeModInfo?.title;
            displayName = mTitle && cTitle && mTitle !== cTitle ? `${mTitle} / ${cTitle}` : (mTitle || cTitle);
            if (dep.customTitle) displayName = `${dep.customTitle} (${displayName})`;
        }
        name.textContent = displayName;

        const meta = document.createElement('div');
        meta.className = 'dep-meta';

        const platformIds = [];
        if (dep.modrinthModInfo?.modid) platformIds.push(`Modrinth: ${dep.modrinthModInfo.modid}`);
        if (dep.curseforgeModInfo?.modid) platformIds.push(`CurseForge: ${dep.curseforgeModInfo.modid}`);
        if (platformIds.length === 0) platformIds.push(`Raw ID: ${dep.projectId}`);
        meta.textContent = platformIds.join(' | ');

        info.appendChild(name);
        info.appendChild(meta);

        const badge = document.createElement('span');
        badge.className = 'dep-type-badge';
        badge.textContent = dep.type;

        const actions = document.createElement('div');
        actions.className = 'dep-actions';

        if (dep.modrinthModInfo?.slug || dep.curseforgeModInfo?.slug) {
            const openBtn = document.createElement('button');
            openBtn.type = 'button';
            openBtn.className = 'btn btn-secondary btn-sm';
            openBtn.textContent = t('button.open');
            openBtn.style.marginRight = '5px';
            openBtn.addEventListener('click', () => {
                if (dep.modrinthModInfo?.slug) {
                    vscode.postMessage({
                        command: 'openExternal',
                        url: `https://modrinth.com/mod/${dep.modrinthModInfo.slug}`
                    });
                } else if (dep.curseforgeModInfo?.slug) {
                    vscode.postMessage({
                        command: 'openExternal',
                        url: `https://www.curseforge.com/minecraft/mc-mods/${dep.curseforgeModInfo.slug}`
                    });
                }
            });
            actions.appendChild(openBtn);
        }

        const editBtn = document.createElement('button');
        editBtn.type = 'button';
        editBtn.className = 'btn btn-secondary btn-sm';
        editBtn.textContent = t('button.edit');
        editBtn.style.marginRight = '5px';
        editBtn.addEventListener('click', () => {
            openDependencyModal(index);
        });

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.className = 'btn btn-danger btn-sm';
        removeBtn.textContent = t('button.delete');
        removeBtn.addEventListener('click', () => {
            dependencyList.splice(index, 1);
            renderDependencies();
        });

        actions.appendChild(editBtn);
        actions.appendChild(removeBtn);

        item.appendChild(info);
        item.appendChild(badge);
        item.appendChild(actions);

        dependenciesContainer.appendChild(item);
    });
}

function showStatus(text) {
    statusText.textContent = text;
    statusBar.classList.remove('hidden');
}

function hideStatus() {
    statusBar.classList.add('hidden');
    btnPublish.disabled = false;
}

window.addEventListener('message', event => {
    const message = event.data;
    switch (message.command) {
        case 'resolvedDependency':
            const isModrinthChecked = document.getElementById('target-modrinth').checked;
            const isCurseForgeChecked = document.getElementById('target-curseforge').checked;
            depModrinthInput.disabled = !isModrinthChecked || !window.CONFIG.modrinthTokenAvailable;
            depCurseforgeInput.disabled = !isCurseForgeChecked || !window.CONFIG.curseforgeStudioTokenAvailable;
            depTypeSelect.disabled = false;
            btnModalOk.disabled = false;
            btnModalCancel.disabled = false;
            depModalStatus.classList.add('hidden');

            if (message.success) {
                depModal.classList.add('hidden');

                const resolvedDep = message.dependency;
                if (editingIndex === -1) {
                    dependencyList.push(resolvedDep);
                } else {
                    dependencyList[editingIndex] = resolvedDep;
                }
                renderDependencies();
            } else {
                showNotification(t('failed.7', message.error) || message.error, 'error');
            }
            break;

        case 'publishProgress':
            showStatus(message.text);
            break;

        case 'publishResult':
            hideStatus();
            if (message.success) {
                showNotification(t('message.success'));
            } else {
                showNotification(t('message.failed', message.error), 'error');
            }
            break;

        case 'versionsUpdated':
            hideStatus();
            const btnUpdate = document.getElementById('btn-update-versions');
            if (btnUpdate) btnUpdate.disabled = false;
            allMinecraftVersions = message.versions || [];
            filterAndRenderVersions();

            if (message.reason === 'clear') {
                showNotification(t('message.clear.success'));
            } else {
                showNotification(t('message.update.success'));
            }
            break;

        case 'versionsUpdateFailed':
            hideStatus();
            const btnUpdateErr = document.getElementById('btn-update-versions');
            if (btnUpdateErr) btnUpdateErr.disabled = false;
            showNotification(t('message.update.failed'), 'error');
            break;
    }
});

init();
