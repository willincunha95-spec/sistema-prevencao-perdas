const API_URL = '/api';
let currentModule = 'registers';
let currentPage = 0;
const pageSize = 100;

// --- CONFIG & DUMMY DATA ---
const DUMMY_PRODUCT = {
    id: 50000004,
    productName: 'Dualsense PS6 para jogar jogos',
    location: 'AN 2-104-04-00',
    description: 'Controle DualSense para PS6 com feedback tátil e gatilhos adaptáveis. Cor Branca.',
    identifierId: 'XJTK10560',
    sku: '7891234560789',
    floor: '2', street: '104', position: '04', subPosition: '00',
    dateLost: new Date(2025, 8, 19).toISOString(),
    salePrice: 70.00,
    compensationPrice: 65.00,
    week: '38'
};

// --- STATE MANAGEMENT ---
let filterState = {
    search: '', floor: '', sector: '', block: '', inventoryLocation: '', week: '', risk: '', street: '',
    valueRange: '', category: '', type: '', systemType: ''
};

// --- UI UTILS ---
const UIUtils = {
    formatDate: (dateStr) => {
        if (!dateStr) return '---';
        return new Date(dateStr).toLocaleDateString('pt-BR');
    },
    formatCurrency: (value) => {
        return value ? `$ ${parseFloat(value).toFixed(2)}` : '---';
    },
    getPriorityInfo: (dateStr) => {
        if (!dateStr) return { class: 'priority-green', label: 'Regular' };
        const diffDays = Math.floor((new Date() - new Date(dateStr)) / (1000 * 60 * 60 * 24));

        if (diffDays >= 80) return { class: 'priority-red', label: 'Urgente' };
        if (diffDays >= 60) return { class: 'priority-orange', label: 'Próximo do Vencimento' };
        if (diffDays >= 30) return { class: 'priority-yellow', label: 'Atenção' };
        return { class: 'priority-green', label: 'Regular' };
    },
    showLoading: (show) => {
        document.getElementById('loading-overlay').style.display = show ? 'flex' : 'none';
    }
};

// --- FILTER ENGINE ---
const FilterEngine = {
    getScore: (item) => {
        const date = new Date(item.dateLost || item.timestamp || item.dateFound || item.dateReported);
        const diffDays = Math.floor((new Date() - date) / (1000 * 60 * 60 * 24));
        let score = diffDays;
        if (diffDays >= 80) score += 1000;
        else if (diffDays >= 60) score += 500;
        if (item.salePrice) score += (item.salePrice / 10);
        return score;
    },
    getWeekNumber: (d) => {
        d = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
        d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay() || 7));
        var yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
        var weekNo = Math.ceil((((d - yearStart) / 86400000) + 1) / 7);
        return weekNo.toString();
    },
    apply: (items) => {
        return items.filter(item => {
            const dateStr = item.dateLost || item.timestamp || item.dateFound || item.dateReported || item.dateAnalyzed;
            const diffDays = dateStr ? Math.floor((new Date() - new Date(dateStr)) / (1000 * 60 * 60 * 24)) : 0;
            const salePrice = item.salePrice || 0;
            const location = (item.location || '').toLowerCase();

            const matchSearch = !filterState.search || item.productName?.toLowerCase().includes(filterState.search);

            // Location Filter Logic (Floors)
            let matchFloor = true;
            if (filterState.floor) {
                if (filterState.floor === 'andar_0') matchFloor = item.floor === '0';
                else if (filterState.floor === 'andar_1') matchFloor = item.floor === '1';
                else if (filterState.floor === 'andar_2') matchFloor = item.floor === '2';
                else if (filterState.floor === 'andar_3') matchFloor = item.floor === '3';
                else if (filterState.floor === 'andar_4') matchFloor = item.floor === '4';
            }

            // Location Filter Logic (Sectors)
            let matchSector = true;
            if (filterState.sector) {
                if (filterState.sector === 'deposito') matchSector = location.includes('depósito') || location.includes('deposito');
                else if (filterState.sector === 'docas') matchSector = location.includes('docas');
                else if (filterState.sector === 'recebimento') matchSector = location.includes('recebimento');
                else if (filterState.sector === 'inventario') matchSector = location.includes('an ') || /an\s*\d/.test(location);
            }

            // Inventory Block Filter Logic (E=1-120, F=121-300, G=301-400)
            let matchBlock = true;
            if (filterState.block) {
                const streetNum = parseInt(item.street, 10);
                if (!isNaN(streetNum)) {
                    if (filterState.block === 'bloco_e') matchBlock = streetNum >= 1 && streetNum <= 120;
                    else if (filterState.block === 'bloco_f') matchBlock = streetNum >= 121 && streetNum <= 300;
                    else if (filterState.block === 'bloco_g') matchBlock = streetNum >= 301 && streetNum <= 400;
                } else {
                    matchBlock = false; // Item has no valid street number for block filtering
                }
            }

            // Inventory Location Filter Logic
            let matchInventory = true;
            if (filterState.inventoryLocation) {
                // Check if user typed something valid
                const searchInv = filterState.inventoryLocation.toLowerCase();
                matchInventory = location.includes(searchInv);
            }

            const matchStreet = !filterState.street || item.street === filterState.street;

            const itemWeek = item.week || (dateStr ? FilterEngine.getWeekNumber(new Date(dateStr)) : '');
            const matchWeek = !filterState.week || itemWeek === filterState.week;

            const matchCategory = !filterState.category || item.category === filterState.category;
            const matchSystem = !filterState.systemType || item.systemType === filterState.systemType;

            // Value Range Filter Logic
            let matchValue = true;
            if (filterState.valueRange) {
                if (filterState.valueRange === 'low') matchValue = salePrice < 60;
                else if (filterState.valueRange === 'medium') matchValue = salePrice >= 60 && salePrice <= 100;
                else if (filterState.valueRange === 'high') matchValue = salePrice > 100;
            }

            const matchType = !filterState.type || item.productName?.toLowerCase().includes(filterState.type);

            let matchRisk = true;
            if (filterState.risk) {
                if (filterState.risk === 'critical') matchRisk = diffDays >= 80;
                else if (filterState.risk === 'high') matchRisk = diffDays >= 60 && diffDays < 80;
                else if (filterState.risk === 'medium') matchRisk = diffDays >= 30 && diffDays < 60;
                else if (filterState.risk === 'low') matchRisk = diffDays < 30;
            }

            return matchSearch && matchFloor && matchSector && matchBlock && matchInventory && matchStreet && matchWeek && matchCategory && matchSystem && matchValue && matchType && matchRisk;
        }).sort((a, b) => FilterEngine.getScore(b) - FilterEngine.getScore(a));
    }
};

// --- UI COMPONENTS ---
const UIComponents = {
    renderCard: (item, index) => {
        const name = item.productName || item.itemName || item.title || 'Produto';
        const location = item.location || item.locationFound || 'Sem Local';
        const priceSale = UIUtils.formatCurrency(item.salePrice);
        const priceComp = UIUtils.formatCurrency(item.compensationPrice);
        const priority = UIUtils.getPriorityInfo(item.dateLost || item.timestamp);

        return `
            <div class="item-card" style="animation-delay: ${index * 0.05}s" onclick="openDetail(${JSON.stringify(item).replace(/"/g, '&quot;')})">
                <div class="card-main-info">
                    <div class="item-details">
                        <div class="item-name">${name}</div>
                        <div class="item-meta">
                            <div class="priority-badge ${priority.class}">
                                <i class="fas fa-triangle-exclamation"></i>
                                <span>${priority.label}</span>
                            </div>
                            <div class="item-location">${location}</div>
                        </div>
                    </div>
                </div>
                <div class="card-pricing-badges">
                    <div class="price-pill sale" title="Preço de Venda">
                        <i class="fas fa-tags"></i><span>${priceSale}</span>
                    </div>
                    <div class="price-pill comp" title="Indenização">
                        <i class="fas fa-hand-holding-dollar"></i><span>${priceComp}</span>
                    </div>
                </div>
                <div class="card-photo-area"><span>Foto</span></div>
            </div>
        `;
    }
};

// --- CORE FUNCTIONS ---
async function fetchData() {
    const container = document.getElementById('cards-container');
    container.innerHTML = '<div class="loading-state"><i class="fas fa-spinner fa-spin"></i> Filtrando dados...</div>';

    try {
        let url = `${API_URL}/${currentModule}?page=${currentPage}&size=${pageSize}`;
        if (currentModule === 'registers') url += '&status=PENDING';
        if (currentModule === 'analyses') url += '&status=PENDING';
        if (currentModule === 'system-analysis') {
            url = `${API_URL}/analyses?page=${currentPage}&size=${pageSize}&status=COMPLETED`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error(`Erro HTTP: ${response.status}`);

        const data = await response.json();
        let items = data.content || (Array.isArray(data) ? data : []);

        const filteredItems = FilterEngine.apply(items);
        renderCards(filteredItems, items.length);
        updateNavBadges();
    } catch (error) {
        console.error('Fetch error:', error);
        container.innerHTML = `<div class="error-state"><i class="fas fa-exclamation-circle"></i> Erro ao carregar dados.</div>`;
    }
}

function renderCards(items, totalFetched = 0) {
    const container = document.getElementById('cards-container');
    const countSpan = document.getElementById('item-count');
    const titleHeader = document.getElementById('current-module-title');

    if (items.length === 0) {
        if (totalFetched > 0) {
            container.innerHTML = '<div class="empty-state">Nenhum item encontrado com esses filtros (Itens ocultos).</div>';
        } else {
            container.innerHTML = '<div class="empty-state">Nenhum registro encontrado no sistema.</div>';
        }
    } else {
        container.innerHTML = '';
    }

    countSpan.innerText = `(${items.length} produtos)`;

    // Update main title based on filter or module
    if (filterState.street) {
        titleHeader.innerText = `RUA ${filterState.street}`;
    } else {
        const labels = { 'registers': 'PENDENTES', 'analyses': 'EM ANÁLISE', 'dfls': 'DFLS', 'found-items': 'FOUNDS' };
        titleHeader.innerText = labels[currentModule] || currentModule.toUpperCase();
    }

    // Grouping Logic
    const groups = items.reduce((acc, item) => {
        const street = item.street || 'S/ RUA';
        if (!acc[street]) acc[street] = [];
        acc[street].push(item);
        return acc;
    }, {});

    // Sort streets numerically
    const sortedStreets = Object.keys(groups).sort((a, b) => {
        if (a === 'S/ RUA') return 1;
        if (b === 'S/ RUA') return -1;
        return parseInt(a) - parseInt(b);
    });

    let globalIdx = 0;
    sortedStreets.forEach(street => {
        // Don't show header if filtered by a single street (it's redundant with the main title)
        if (!filterState.street) {
            container.innerHTML += `<div class="street-group-header">RUA ${street}</div>`;
        }

        groups[street].forEach(item => {
            container.innerHTML += UIComponents.renderCard(item, globalIdx++);
        });
    });
}

function openDetail(item) {
    const modal = document.getElementById('product-modal');
    const content = document.getElementById('detail-view-content');
    const dateStr = UIUtils.formatDate(item.dateLost || item.timestamp);
    const sale = UIUtils.formatCurrency(item.salePrice);
    const comp = UIUtils.formatCurrency(item.compensationPrice);

    content.innerHTML = `
        <div class="detail-photo-header">
            <span class="close-modal" onclick="closeModal()">&times;</span>
            <span>FOTO</span>
        </div>
        <div class="detail-body">
            <div class="detail-name-row">
                <h3 class="detail-title">${item.productName || item.itemName || item.title || 'Produto'}</h3>
                ${item.color ? `<span class="color-badge"><i class="fas fa-palette"></i> ${item.color}</span>` : ''}
            </div>
            <p class="detail-description">${item.description || 'Sem descrição.'}</p>
            <div class="detail-info-grid">
                <div class="info-row-main">
                    <div class="info-item">
                        <span class="info-label">Localização</span>
                        <span class="info-value big">${item.locationFound || item.location || '---'}</span>
                    </div>
                    ${currentModule === 'registers' ?
            `<button class="analyze-action-btn" id="btn-start-analysis"><i class="fas fa-play"></i> Analisar</button>` :
            (currentModule === 'analyses' || currentModule === 'system-analysis' ?
                `<div class="analysis-actions">
                            <button class="btn-action dfl" id="btn-to-dfl"><i class="fas fa-trash-can"></i> DFL</button>
                            <button class="btn-action found" id="btn-to-found"><i class="fas fa-check-circle"></i> Found</button>
                        </div>` : '')
        }
                </div>
                <div class="secondary-info-row">
                    <div class="info-item"><span class="info-label">Perdido em</span><span class="info-value">${dateStr}</span></div>
                    <div class="info-item"><span class="info-label">Preço de Venda</span><span class="info-value green">${sale}</span></div>
                    <div class="info-item"><span class="info-label">Indenização</span><span class="info-value orange">${comp}</span></div>
                </div>
            </div>
            <div class="detail-identifiers">
                <div class="identifier-row"><span class="info-label">ID Localizador</span><span class="id-badge">${item.identifierId || 'S/ID'}</span></div>
                <div class="identifier-row"><span class="info-label">SKU</span><span class="id-badge">${item.sku || '---'}</span></div>
                ${item.sellerId ? `<div class="identifier-row"><span class="info-label">Seller ID</span><span class="id-badge">${item.sellerId}</span></div>` : ''}
                ${item.meliId ? `<div class="identifier-row"><span class="info-label">Meli ID</span><span class="id-badge">${item.meliId}</span></div>` : ''}
            </div>
        </div>
    `;

    if (document.getElementById('btn-start-analysis')) {
        document.getElementById('btn-start-analysis').onclick = () => startAnalysis(item);
    }
    if (document.getElementById('btn-to-dfl')) {
        document.getElementById('btn-to-dfl').onclick = () => finalizeAnalysis(item, 'dfl');
    }
    if (document.getElementById('btn-to-found')) {
        document.getElementById('btn-to-found').onclick = () => finalizeAnalysis(item, 'found');
    }
    modal.style.display = 'flex';
}

async function startAnalysis(item) {
    UIUtils.showLoading(true);
    try {
        const payload = {
            title: item.productName || 'Novo Registro',
            productName: item.productName,
            color: item.color,
            sku: item.sku,
            week: item.week || '',
            floor: item.floor,
            street: item.street,
            position: item.position,
            subPosition: item.subPosition,
            identifierId: item.identifierId,
            sellerId: item.sellerId,
            meliId: item.meliId,
            salePrice: item.salePrice,
            compensationPrice: item.compensationPrice,
            salePrice: item.salePrice,
            compensationPrice: item.compensationPrice,
            location: item.location || (item.floor ? `AN ${item.floor}-${item.street}-${item.position}-${item.subPosition}` : 'N/A'),
            status: 'PENDING',
            registerId: item.id,
            responsible: { id: 1 }
        };

        const res = await fetch(`${API_URL}/analyses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Erro ao iniciar análise');
        }

        UIUtils.showLoading(false);
        closeModal();
        loadModule('analysis', 'produtos em analise');
        updateNavBadges();
    } catch (e) {
        UIUtils.showLoading(false);
        alert(e.message);
        closeModal();
    }
}

// Found Modal UI Logic
let currentFoundItem = null;

function openFoundModal(item) {
    currentFoundItem = item;
    const modal = document.getElementById('found-modal');
    modal.style.display = 'flex';
    document.getElementById('found-location-input').value = item.location || ''; // Pre-fill if exists
    document.getElementById('found-location-input').focus();

    // Bind events locally or ensure global binding doesn't duplicate
    document.getElementById('btn-confirm-found').onclick = () => confirmFoundLocation();
    document.getElementById('btn-cancel-found').onclick = () => closeFoundModal();
    document.getElementById('close-found-modal').onclick = () => closeFoundModal();
}

function closeFoundModal() {
    document.getElementById('found-modal').style.display = 'none';
    currentFoundItem = null;
    document.getElementById('found-location-input').value = '';
}

function confirmFoundLocation() {
    if (!currentFoundItem) return;
    const loc = document.getElementById('found-location-input').value;
    if (!loc) {
        alert("Por favor, digite a localização.");
        return;
    }
    finalizeAnalysis(currentFoundItem, 'found', loc);
    closeFoundModal();
}

async function finalizeAnalysis(item, type, locationOverride = null) {
    // If it's a 'found' action and we don't have the locationOverride yet, open the modal
    if (type === 'found' && !locationOverride) {
        openFoundModal(item);
        return;
    }

    UIUtils.showLoading(true);
    try {
        const endpoint = type === 'dfl' ? 'to-dfl' : 'to-found';
        const payload = type === 'dfl' ? {
            productName: item.productName || item.title,
            color: item.color,
            sku: item.sku || 'N/A',
            quantity: 1,
            unitValue: item.salePrice || 0,
            location: item.location || 'N/A',
            floor: item.floor,
            street: item.street,
            position: item.position,
            subPosition: item.subPosition,
            identifierId: item.identifierId,
            sellerId: item.sellerId,
            meliId: item.meliId,
            reportedBy: { id: 1 }
        } : {
            itemName: item.productName || item.title,
            color: item.color,
            sku: item.sku,
            locationFound: locationOverride, // Use the passed location
            location: item.location || 'N/A',
            floor: item.floor,
            street: item.street,
            position: item.position,
            subPosition: item.subPosition,
            identifierId: item.identifierId,
            sellerId: item.sellerId,
            meliId: item.meliId,
            salePrice: item.salePrice,
            compensationPrice: item.compensationPrice,
            foundBy: { id: 1 }
        };

        const res = await fetch(`${API_URL}/analyses/${item.id}/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error();

        UIUtils.showLoading(false);
        closeModal(); // Close detail modal
        loadModule(type === 'dfl' ? 'dfls' : 'found-items', type.toUpperCase());
        updateNavBadges();
    } catch (e) {
        UIUtils.showLoading(false);
        alert('Erro ao finalizar operação.');
    }
}

// --- INITIALIZATION ---
function initEvents() {
    const syncBtn = document.getElementById('btn-sync');
    const filterBtn = document.getElementById('btn-filter-toggle');
    const filterClose = document.getElementById('btn-filter-close');
    const filterPanel = document.getElementById('filter-panel');

    syncBtn.onclick = syncRecords;
    filterBtn.onclick = () => toggleFilterPanel(true);
    filterClose.onclick = () => toggleFilterPanel(false);

    const binder = (id, key) => {
        const el = document.getElementById(id);
        if (el) {
            el.oninput = (e) => filterState[key] = e.target.value.toLowerCase();
            el.onchange = (e) => filterState[key] = e.target.value.toLowerCase();
        }
    };

    binder('global-search', 'search');
    // Global search triggers immediately (usually desired), strictly distinct from advanced filters
    document.getElementById('global-search').oninput = (e) => {
        filterState.search = e.target.value.toLowerCase();
        triggerFilter();
    };

    binder('filter-floor', 'floor');

    // Custom binder for sector to handle Block visibility
    const sectorEl = document.getElementById('filter-sector');
    if (sectorEl) {
        sectorEl.addEventListener('change', (e) => {
            const val = e.target.value;
            filterState.sector = val;

            const blockSelect = document.getElementById('filter-block');
            if (blockSelect) {
                if (val === 'inventario') {
                    blockSelect.style.display = 'block';
                } else {
                    blockSelect.style.display = 'none';
                    blockSelect.value = '';
                    filterState.block = ''; // Reset block filter
                }
            }
            triggerFilter();
        });
    }
    // binder('filter-sector', 'sector'); // Replaced by custom handler above

    binder('filter-block', 'block');
    binder('filter-inventory', 'inventoryLocation');
    binder('filter-street', 'street');
    binder('filter-week', 'week');
    binder('filter-risk', 'risk');
    binder('filter-value-range', 'valueRange');
    binder('filter-category', 'category');
    binder('filter-type', 'type');
    binder('filter-system-type', 'systemType');

    document.getElementById('btn-apply-filters').onclick = () => {
        triggerFilter();
        toggleFilterPanel(false);
    };

    document.getElementById('btn-clear-filters').onclick = () => {
        filterState = { search: '', floor: '', sector: '', block: '', inventoryLocation: '', street: '', week: '', risk: '', valueRange: '', category: '', type: '', systemType: '' };
        document.querySelectorAll('.filter-panel select, .filter-panel input').forEach(i => i.value = '');
        // Keep global search if it was outside the panel? usually clear all.
        document.getElementById('global-search').value = '';
        triggerFilter();
    };
}

let filterTimeout;
function triggerFilter() {
    clearTimeout(filterTimeout);
    filterTimeout = setTimeout(fetchData, 100);
}

function addToSyncQueue(type, payload) {
    syncQueue.push({ type, payload, timestamp: new Date().toISOString() });
    localStorage.setItem('syncQueue', JSON.stringify(syncQueue));
    updateSyncUI();
}

function updateSyncUI() {
    const btn = document.getElementById('btn-sync');
    const badge = document.getElementById('sync-badge');
    btn.classList.toggle('pending', syncQueue.length > 0);
    badge.innerText = syncQueue.length;
    badge.style.display = syncQueue.length > 0 ? 'block' : 'none';
}

async function syncRecords() {
    if (!syncQueue.length) return alert('Nada para sincronizar.');
    const btn = document.getElementById('btn-sync');
    btn.classList.add('syncing');

    // Simple sync logic for refactor demo
    const failed = [];
    for (const item of syncQueue) {
        try {
            const res = await fetch(`${API_URL}/analyses`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(item.payload)
            });
            if (!res.ok) throw new Error();
        } catch (e) { failed.push(item); }
    }
    syncQueue = failed;
    localStorage.setItem('syncQueue', JSON.stringify(syncQueue));
    updateSyncUI();
    btn.classList.remove('syncing');
    alert(failed.length ? 'Alguns itens falharam.' : 'Sincronizado!');
}

async function loadModule(module, label, element) {
    // Update active tab UI
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    if (element) {
        element.classList.add('active');
    } else {
        // Fallback for search by text if element not passed
        const items = document.querySelectorAll('.nav-item');
        for (const item of items) {
            if (item.innerText.toLowerCase().includes(label.toLowerCase().split(' ')[0])) {
                item.classList.add('active');
                break;
            }
        }
    }

    const map = { 'registers': 'registers', 'analysis': 'analyses', 'dfls': 'dfls', 'found-items': 'found-items' };
    currentModule = map[module] || module;
    document.getElementById('current-module-title').innerText = label;
    fetchData();
}

async function updateNavBadges() {
    try {
        const res = await fetch(`${API_URL}/dashboard/stats`);
        if (!res.ok) return;
        const stats = await res.json();

        document.getElementById('count-registers').innerText = `(${stats.registersPending})`;
        document.getElementById('count-analysis').innerText = `(${stats.analysesInProgress})`;
        document.getElementById('count-dfls').innerText = `(${stats.dflsTotal})`;
        document.getElementById('count-found-items').innerText = `(${stats.foundsTotal})`;
        document.getElementById('count-system-analysis').innerText = `(${stats.systemicTotal})`;
        document.getElementById('count-found-inv').innerText = `(${stats.foundInvTotal})`;
    } catch (e) {
        console.error('Badge update error:', e);
    }
}

function toggleFilterPanel(show) {
    const panel = document.getElementById('filter-panel');
    const btn = document.getElementById('btn-filter-toggle');

    // Toggle check if show is undefined (toggle mode)
    if (show === undefined) show = panel.style.display !== 'block';

    panel.style.display = show ? 'block' : 'none';
    if (btn) btn.classList.toggle('active', show);

    // Lock/Unlock body scroll
    document.body.style.overflow = show ? 'hidden' : '';
}

function closeModal() { document.getElementById('product-modal').style.display = 'none'; }

document.addEventListener('DOMContentLoaded', () => {
    initEvents();
    // Simulate click on the first nav item to load default module (Registers/Pendentes)
    const firstNavItem = document.querySelector('.nav-item');
    if (firstNavItem) firstNavItem.click();

    updateSyncUI();
    updateNavBadges();
});
