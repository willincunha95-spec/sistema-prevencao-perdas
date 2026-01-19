/**
 * LOSS PREVENTION SYSTEM - FRONTEND CORE (app.js)
 * 
 * Este arquivo controla toda a lógica da interface do usuário:
 * - Busca de dados do backend (Spring Boot).
 * - Renderização dinâmica de cards de produtos.
 * - Motor de filtragem avançada (Rua, Andar, Setor, Valor, Risco).
 * - Sistema de Carrossel de Imagens integrado com IA/Mercado Livre.
 * - Controle de modais e finalização de auditorias (DFL ou Found).
 */

const API_URL = '/api'; // Rota base para comunicação com o servidor Java
let currentModule = 'registers'; // Aba atual selecionada (Pendentes, Análise, DFL, etc)
let currentPage = 0;
const pageSize = 100; // Quantidade de itens por carregamento

// --- CONFIGURAÇÕES E DADOS DE SIMULAÇÃO ---
const EXCHANGE_RATE = 6.05; // Taxa de conversão realista para transformar R$ em $ (Dólar)

// --- ESTADO GLOBAL DOS FILTROS ---
let filterState = {
    search: '', floor: '', sector: '', block: '', inventoryLocation: '', week: '', risk: '', street: '',
    valueRange: '', category: '', type: '', systemType: ''
};

// --- UTILITÁRIOS DE INTERFACE (UI UTILS) ---
const UIUtils = {
    // Formata datas para o padrão brasileiro DD/MM/AAAA
    formatDate: (dateStr) => {
        if (!dateStr) return '---';
        return new Date(dateStr).toLocaleDateString('pt-BR');
    },
    // Converte e formata moedas (Reais -> Dólares) conforme solicitado
    formatCurrency: (value) => {
        if (!value) return '---';
        const brl = parseFloat(value);
        const usd = brl / EXCHANGE_RATE; // Divisão pela taxa de câmbio
        return `$ ${usd.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    },
    // Calcula o nível de risco e define a cor do badge com base no tempo decorrido
    getPriorityInfo: (dateStr) => {
        if (!dateStr) return { class: 'priority-green', label: 'Regular' };
        const diffDays = Math.floor((new Date() - new Date(dateStr)) / (1000 * 60 * 60 * 24));

        if (diffDays >= 80) return { class: 'priority-red', label: 'Urgente' };
        if (diffDays >= 60) return { class: 'priority-orange', label: 'Próximo do Vencimento' };
        if (diffDays >= 30) return { class: 'priority-yellow', label: 'Atenção' };
        return { class: 'priority-green', label: 'Regular' };
    },
    // Identifica o produto via palavras-chave e retorna a imagem "Referência" (org.webp)
    getProductImage: (name, description) => {
        const s = `${name || ''} ${description || ''}`.toLowerCase();
        if (s.includes('iphone')) return '/img/iphoneorg.webp';
        if (s.includes('s25') || s.includes('samsung') || s.includes('galaxy')) return '/img/S25org.webp';
        if (s.includes('monitor') || s.includes('dell') || s.includes('tela')) return '/img/monitororg.webp';
        return null;
    },
    // Controla o overlay de carregamento global
    showLoading: (show) => {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) overlay.style.display = show ? 'flex' : 'none';
    }
};

// --- MOTOR DE FILTRAGEM (FILTER ENGINE) ---
/**
 * O FilterEngine é responsável por processar a lista de itens recebida do servidor
 * e aplicar os critérios de busca, localidade e risco definidos pelo usuário.
 */
const FilterEngine = {
    // Atribui uma pontuação de relevância para ordenação (mais antigos/caros no topo)
    getScore: (item) => {
        const dateStr = item.dateLost || item.timestamp || item.dateFound || item.dateReported || item.dateAnalyzed;
        const date = dateStr ? new Date(dateStr) : new Date();
        const diffDays = Math.floor((new Date() - date) / (1000 * 60 * 60 * 24));
        let score = diffDays;
        if (diffDays >= 80) score += 1000;
        else if (diffDays >= 60) score += 500;
        if (item.salePrice) score += (item.salePrice / 10);
        return score;
    },
    // Calcula o número da semana para filtros sazonais
    getWeekNumber: (d) => {
        d = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
        d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay() || 7));
        var yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
        var weekNo = Math.ceil((((d - yearStart) / 86400000) + 1) / 7);
        return weekNo.toString();
    },
    // Aplica todos os filtros em cadeia no conjunto de dados
    apply: (items) => {
        return items.filter(item => {
            const dateStr = item.dateLost || item.timestamp || item.dateFound || item.dateReported || item.dateAnalyzed;
            const diffDays = dateStr ? Math.floor((new Date() - new Date(dateStr)) / (1000 * 60 * 60 * 24)) : 0;
            const salePrice = item.salePrice || 0;
            const location = (item.locationFound || item.location || '').toLowerCase();
            const productName = item.productName || item.itemName || item.title || '';

            // Filtro de Texto Global
            const matchSearch = !filterState.search || productName.toLowerCase().includes(filterState.search);

            // Filtros de Localidade (Andares)
            let matchFloor = true;
            if (filterState.floor) {
                if (filterState.floor === 'andar_0') matchFloor = item.floor === '0';
                else if (filterState.floor === 'andar_1') matchFloor = item.floor === '1';
                else if (filterState.floor === 'andar_2') matchFloor = item.floor === '2';
                else if (filterState.floor === 'andar_3') matchFloor = item.floor === '3';
                else if (filterState.floor === 'andar_4') matchFloor = item.floor === '4';
            }

            // Filtros de Setor (Depósito, Docas, Inventário e Siglas)
            let matchSector = true;
            if (filterState.sector) {
                if (filterState.sector === 'deposito') matchSector = location.includes('depósito') || location.includes('deposito');
                else if (filterState.sector === 'docas') matchSector = location.includes('docas');
                else if (filterState.sector === 'recebimento') matchSector = location.includes('recebimento');
                else if (filterState.sector === 'inventario') matchSector = location.includes('an ') || /an\s*\d/.test(location);
                else {
                    // Para siglas (DV, RK, HV, MTU), verifica se a sigla está contida na string de localização
                    matchSector = location.includes(filterState.sector.toLowerCase());
                }
            }

            // Filtro por Blocos Logísticos (E, F, G) baseado em numeração de rua
            let matchBlock = true;
            if (filterState.block) {
                const streetNum = parseInt(item.street, 10);
                if (!isNaN(streetNum)) {
                    if (filterState.block === 'bloco_e') matchBlock = streetNum >= 1 && streetNum <= 120;
                    else if (filterState.block === 'bloco_f') matchBlock = streetNum >= 121 && streetNum <= 300;
                    else if (filterState.block === 'bloco_g') matchBlock = streetNum >= 301 && streetNum <= 400;
                } else {
                    matchBlock = false;
                }
            }

            const matchStreet = !filterState.street || item.street === filterState.street;
            const itemWeek = item.week || (dateStr ? FilterEngine.getWeekNumber(new Date(dateStr)) : '');
            const matchWeek = !filterState.week || itemWeek === filterState.week;
            const matchCategory = !filterState.category || item.category === filterState.category;

            // Filtro de Faixa de Valor (Convertido para Dólar)
            let matchValue = true;
            if (filterState.valueRange) {
                const usdPrice = salePrice / EXCHANGE_RATE;
                if (filterState.valueRange === 'low') matchValue = usdPrice < 60;
                else if (filterState.valueRange === 'medium') matchValue = usdPrice >= 60 && usdPrice <= 100;
                else if (filterState.valueRange === 'high') matchValue = usdPrice > 100;
            }

            // Filtro de Inventário (Texto parcial na localização)
            const matchInventory = !filterState.inventoryLocation || location.includes(filterState.inventoryLocation);

            const matchType = !filterState.type || productName.toLowerCase().includes(filterState.type);

            // Filtro de Criticidade (Risco)
            let matchRisk = true;
            if (filterState.risk) {
                if (filterState.risk === 'critical') matchRisk = diffDays >= 80;
                else if (filterState.risk === 'high') matchRisk = diffDays >= 60 && diffDays < 80;
                else if (filterState.risk === 'medium') matchRisk = diffDays >= 30 && diffDays < 60;
                else if (filterState.risk === 'low') matchRisk = diffDays < 30;
            }

            // Filtro de Tipo Sistêmico
            const itemSystemType = (item.systemType || '').toLowerCase();
            const matchSystemType = !filterState.systemType || itemSystemType === filterState.systemType.toLowerCase();

            return matchSearch && matchFloor && matchSector && matchBlock && matchStreet &&
                matchWeek && matchCategory && matchValue && matchType && matchRisk &&
                matchInventory && matchSystemType;
        }).sort((a, b) => FilterEngine.getScore(b) - FilterEngine.getScore(a));
    }
};

// --- COMPONENTES VISUAIS (UI COMPONENTS) ---
const UIComponents = {
    // Renderiza o Card individual de um produto para a lista
    renderCard: (item, index) => {
        const name = item.productName || item.itemName || item.title || 'Produto';
        const location = item.locationFound || item.location || 'Sem Local';
        const priceSale = UIUtils.formatCurrency(item.salePrice);
        const priceComp = UIUtils.formatCurrency(item.compensationPrice);
        const priority = UIUtils.getPriorityInfo(item.dateLost || item.timestamp || item.dateFound || item.dateAnalyzed);
        const productImg = UIUtils.getProductImage(name, item.description);

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
                <div class="card-photo-area">
                    ${productImg ? `<img src="${productImg}" alt="Produto" class="card-thumb">` : '<span>Foto</span>'}
                </div>
            </div>
        `;
    }
};

// --- FUNÇÕES CORE E COMUNICAÇÃO EXTERNA ---

// Busca registros do banco de dados via API REST
async function fetchData() {
    const container = document.getElementById('cards-container');
    if (!container) return;
    container.innerHTML = '<div class="loading-state"><i class="fas fa-spinner fa-spin"></i> Filtrando dados...</div>';

    try {
        let url = `${API_URL}/${currentModule}?page=${currentPage}&size=${pageSize}`;
        if (currentModule === 'registers' || currentModule === 'analyses') url += '&status=PENDING';

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

// Organiza e exibe os cards agrupados por Rua
function renderCards(items, totalFetched = 0) {
    const container = document.getElementById('cards-container');
    const countSpan = document.getElementById('item-count');
    const titleHeader = document.getElementById('current-module-title');

    if (!container) return;

    if (items.length === 0) {
        container.innerHTML = '<div class="empty-state">Nenhum registro encontrado.</div>';
    } else {
        container.innerHTML = '';
    }

    if (countSpan) countSpan.innerText = `(${items.length} produtos)`;

    // Agrupamento por Rua
    const groups = items.reduce((acc, item) => {
        const street = item.street || 'S/ RUA';
        if (!acc[street]) acc[street] = [];
        acc[street].push(item);
        return acc;
    }, {});

    const sortedStreets = Object.keys(groups).sort((a, b) => {
        if (a === 'S/ RUA') return 1;
        if (b === 'S/ RUA') return -1;
        return parseInt(a) - parseInt(b);
    });

    let globalIdx = 0;
    sortedStreets.forEach(street => {
        const visibleItems = groups[street];
        if (visibleItems && visibleItems.length > 0) {
            if (!filterState.street) {
                container.innerHTML += `<div class="street-group-header">RUA ${street}</div>`;
            }
            visibleItems.forEach(item => {
                container.innerHTML += UIComponents.renderCard(item, globalIdx++);
            });
        }
    });
}

// Abre o Modal com detalhes do produto e dispara a busca de imagens com IA
window.currentDetailId = null;
function openDetail(item) {
    window.currentDetailId = item.id;
    const modal = document.getElementById('product-modal');
    const content = document.getElementById('detail-view-content');
    if (!modal || !content) return;

    const dateStr = UIUtils.formatDate(item.dateLost || item.timestamp || item.dateFound || item.dateAnalyzed);
    const sale = UIUtils.formatCurrency(item.salePrice);
    const comp = UIUtils.formatCurrency(item.compensationPrice);
    const initialImg = UIUtils.getProductImage(item.productName || item.itemName || item.title, item.description);

    content.innerHTML = `
        <div class="detail-photo-header">
            <span class="close-modal" onclick="closeModal()">&times;</span>
            ${initialImg ? `<img id="ml-carousel-img" src="${initialImg}" style="max-height: 100%; max-width: 100%; object-fit: contain;">` : '<span>FOTO</span>'}
        </div>
        <div class="detail-body">
            <div class="detail-name-row">
                <h3 class="detail-title">${item.productName || item.itemName || item.title || 'Produto'}</h3>
            </div>
            <p class="detail-description">${item.description || 'Sem descrição.'}</p>
            <div class="detail-info-grid">
                <div class="info-row-main">
                    <div class="info-item">
                        <span class="info-label">Localização</span>
                        <span class="info-value big">${item.locationFound || item.location || '---'}</span>
                    </div>
                </div>
                <div class="secondary-info-row">
                    <div class="info-item"><span class="info-label">Info Data</span><span class="info-value">${dateStr}</span></div>
                    <div class="info-item"><span class="info-label">Valor</span><span class="info-value green">${sale}</span></div>
                    <div class="info-item"><span class="info-label">Ajuste</span><span class="info-value orange">${comp}</span></div>
                </div>
            </div>
            <div class="detail-identifiers">
                <div class="identifier-row"><span class="info-label">ID</span><span class="id-badge">${item.identifierId || 'S/ID'}</span></div>
                <div class="identifier-row"><span class="info-label">SKU</span><span class="id-badge">${item.sku || '---'}</span></div>
            </div>
            ${(currentModule === 'registers' || currentModule === 'analyses') ? `
                <div class="analysis-actions">
                    <button class="btn-action dfl" onclick="processAction('dfl', ${item.id})">
                        <i class="fas fa-file-invoice"></i> Gerar DFL
                    </button>
                    <button class="btn-action found" onclick="processAction('found', ${item.id})">
                        <i class="fas fa-box-open"></i> Marcar Found
                    </button>
                </div>
            ` : ''}
        </div>
    `;

    modal.style.display = 'flex';
    // Chama a busca por carrossel do Mercado Livre (Injetada com IA Simulation)
    searchMLPhotos(item, !!initialImg);
}

// --- SISTEMA DE CARROSSEL DE IMAGENS E IA ---
let currentMLImages = [];
let currentImageIndex = 0;

/**
 * Busca de fotos automatizada:
 * - Tenta encontrar imagens reais no Mercado Livre.
 * - Caso seja um produto simulado (iPhone/Samsung/Monitor), a IA injeta
 *   as fotos locais de alta qualidade enviadas pelo usuário.
 */
async function searchMLPhotos(item, hasInitialImage) {
    const header = document.querySelector('.detail-photo-header');
    if (!header) return;

    try {
        const rawQuery = (item.productName || item.itemName || item.title || '').trim();
        const descMatch = (item.description || '').trim();
        let query = rawQuery;
        if (descMatch && descMatch !== 'Sem descrição.') query += ' ' + descMatch;

        if (!query) throw new Error("Sem critério de busca.");

        const res = await fetch(`${API_URL}/automation/analyze-ml?description=${encodeURIComponent(query)}&_t=${new Date().getTime()}`);
        if (!res.ok) throw new Error("Erro API");

        const data = await res.json();
        if (window.currentDetailId != item.id) return; // Proteção contra troca rápida de modais

        if (data.imageUrls && data.imageUrls.length > 0) {
            currentMLImages = data.imageUrls;
            currentImageIndex = 0;

            header.innerHTML = `
                <div style="position: relative; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background-color: #f0f0f0; overflow: hidden;">
                    <img id="ml-carousel-img" src="${currentMLImages[0]}" style="max-height: 100%; max-width: 100%; object-fit: contain; transition: opacity 0.3s ease;">
                    ${currentMLImages.length > 1 ? `
                        <button class="carousel-nav-btn prev" onclick="event.stopPropagation(); prevImage()" style="position: absolute; left: 10px; background: rgba(0,0,0,0.5); color: white; border: none; padding: 10px; cursor: pointer; border-radius: 50%; width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; z-index: 10;"><i class="fas fa-chevron-left"></i></button>
                        <button class="carousel-nav-btn next" onclick="event.stopPropagation(); nextImage()" style="position: absolute; right: 10px; background: rgba(0,0,0,0.5); color: white; border: none; padding: 10px; cursor: pointer; border-radius: 50%; width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; z-index: 10;"><i class="fas fa-chevron-right"></i></button>
                        <div id="ml-carousel-counter" style="position: absolute; bottom: 10px; background: rgba(0,0,0,0.5); color: white; padding: 2px 10px; border-radius: 15px; font-size: 0.8rem;">1/${currentMLImages.length}</div>
                    ` : ''}
                    <div style="position: absolute; top: 10px; right: 10px; z-index: 10; display: flex; align-items: center; gap: 8px;">
                        <a href="${data.productUrl}" target="_blank" style="color: white; background: #3483fa; padding: 6px 12px; border-radius: 4px; text-decoration: none; font-weight: bold; font-size: 0.75rem;">ML <i class="fas fa-external-link-alt"></i></a>
                        <span class="close-modal" onclick="closeModal()" style="color: #333; cursor: pointer; font-size: 1.8rem; width: 32px; height: 32px; background: white; border-radius: 50%; display: flex; align-items: center; justify-content: center;">&times;</span>
                    </div>
                </div>
            `;
        }
    } catch (e) {
        console.warn('IA Search failed:', e);
    }
}

// Controle de troca de imagem do carrossel
function updateCarouselImage() {
    const imgEl = document.getElementById('ml-carousel-img');
    const countEl = document.getElementById('ml-carousel-counter');
    if (imgEl && currentMLImages.length > 0) {
        imgEl.src = currentMLImages[currentImageIndex];
        if (countEl) countEl.innerText = `${currentImageIndex + 1}/${currentMLImages.length}`;
    }
}
function nextImage() { currentImageIndex = (currentImageIndex + 1) % currentMLImages.length; updateCarouselImage(); }
function prevImage() { currentImageIndex = (currentImageIndex - 1 + currentMLImages.length) % currentMLImages.length; updateCarouselImage(); }

// --- OUTROS CONTROLES ---
function closeModal() { document.getElementById('product-modal').style.display = 'none'; }

// Atualiza os contadores (badges) na navegação inferior
async function updateNavBadges() {
    try {
        const res = await fetch(`${API_URL}/dashboard/stats`);
        const stats = await res.json();
        document.getElementById('count-registers').innerText = `(${stats.registersPending || 0})`;
        document.getElementById('count-analysis').innerText = `(${stats.analysesInProgress || 0})`;
        document.getElementById('count-dfls').innerText = `(${stats.dflsTotal || 0})`;
        document.getElementById('count-found-items').innerText = `(${stats.foundsTotal || 0})`;
    } catch (e) { }
}

// Troca de Módulos (Pendentes <-> Análises <-> DFLs <-> Founds)
function loadModule(module, label, element) {
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    if (element) element.classList.add('active');
    currentModule = module === 'analysis' ? 'analyses' : (module === 'found-items' ? 'found-items' : (module === 'found-inv' ? 'found-inv' : module));
    document.getElementById('current-module-title').innerText = label;
    currentPage = 0; // Reset pagination
    fetchData();
}

// Processa ações de DFL ou Found
async function processAction(type, id) {
    if (type === 'dfl') {
        if (!confirm('Deseja realmente gerar um DFL para este item?')) return;
        try {
            const res = await fetch(`${API_URL}/registers/${id}/dfl`, { method: 'POST' });
            if (res.ok) {
                closeModal();
                fetchData();
            }
        } catch (e) { console.error(e); }
    } else if (type === 'found') {
        window.pendingFoundId = id;
        document.getElementById('found-modal').style.display = 'flex';
    }
}

// Lógica do Modal de Found
function initFoundModal() {
    const modal = document.getElementById('found-modal');
    const closeBtn = document.getElementById('close-found-modal');
    const cancelBtn = document.getElementById('btn-cancel-found');
    const confirmBtn = document.getElementById('btn-confirm-found');
    const input = document.getElementById('found-location-input');

    const close = () => {
        modal.style.display = 'none';
        if (input) input.value = '';
    };

    if (closeBtn) closeBtn.onclick = close;
    if (cancelBtn) cancelBtn.onclick = close;

    if (confirmBtn) {
        confirmBtn.onclick = async () => {
            const location = input.value.trim();
            if (!location) return alert('Por favor, informe a localização.');

            try {
                const res = await fetch(`${API_URL}/registers/${window.pendingFoundId}/found?location=${encodeURIComponent(location)}`, { method: 'POST' });
                if (res.ok) {
                    close();
                    closeModal();
                    fetchData();
                }
            } catch (e) { console.error(e); }
        };
    }
}

// Inicialização de eventos da página
function initEvents() {
    // Pesquisa Global (Input de Texto)
    const search = document.getElementById('global-search');
    if (search) {
        search.oninput = (e) => {
            filterState.search = e.target.value.toLowerCase();
            triggerFilter();
        };
    }

    // Controle de Abertura/Fechamento do Painel de Filtros
    const btnToggle = document.getElementById('btn-filter-toggle');
    const btnClose = document.getElementById('btn-filter-close');
    const panel = document.getElementById('filter-panel');
    const sectorSelect = document.getElementById('filter-sector');
    const blockSelect = document.getElementById('filter-block');

    if (sectorSelect && blockSelect) {
        sectorSelect.onchange = () => {
            blockSelect.style.display = (sectorSelect.value === 'rk') ? 'block' : 'none';
        };
    }

    if (btnToggle && panel) {
        btnToggle.onclick = (e) => {
            e.stopPropagation();
            const isVisible = panel.style.display === 'block';
            panel.style.display = isVisible ? 'none' : 'block';
            btnToggle.classList.toggle('active', !isVisible);
        };
    }

    if (btnClose && panel) {
        btnClose.onclick = () => {
            panel.style.display = 'none';
            if (btnToggle) btnToggle.classList.remove('active');
        };
    }

    // Fechar painel ao clicar fora dele
    document.addEventListener('click', (e) => {
        if (panel && panel.style.display === 'block') {
            if (!panel.contains(e.target) && e.target !== btnToggle && !btnToggle.contains(e.target)) {
                panel.style.display = 'none';
                if (btnToggle) btnToggle.classList.remove('active');
            }
        }
    });

    // Botão Aplicar Filtros
    const btnApply = document.getElementById('btn-apply-filters');
    if (btnApply) {
        btnApply.onclick = () => {
            filterState.floor = document.getElementById('filter-floor').value;
            filterState.sector = document.getElementById('filter-sector').value;
            filterState.block = document.getElementById('filter-block').value;
            filterState.inventoryLocation = document.getElementById('filter-inventory').value.toLowerCase();
            filterState.week = document.getElementById('filter-week').value;
            filterState.risk = document.getElementById('filter-risk').value;
            filterState.valueRange = document.getElementById('filter-value-range').value;
            filterState.category = document.getElementById('filter-category').value;
            filterState.systemType = document.getElementById('filter-system-type').value;
            filterState.type = document.getElementById('filter-type').value.toLowerCase();

            if (panel) panel.style.display = 'none';
            if (btnToggle) btnToggle.classList.remove('active');

            fetchData();
        };
    }

    // Botão Limpar Filtros
    const btnClear = document.getElementById('btn-clear-filters');
    if (btnClear) {
        btnClear.onclick = () => {
            // Reseta o estado global (mantendo a busca global se houver)
            const currentSearch = search ? search.value : '';
            filterState = {
                search: currentSearch.toLowerCase(),
                floor: '', sector: '', block: '', inventoryLocation: '',
                week: '', risk: '', street: '', valueRange: '',
                category: '', type: '', systemType: ''
            };

            // Reseta os elementos da UI
            ['filter-floor', 'filter-sector', 'filter-block', 'filter-risk',
                'filter-value-range', 'filter-category', 'filter-system-type'].forEach(id => {
                    const el = document.getElementById(id);
                    if (el) el.value = '';
                });

            ['filter-inventory', 'filter-week', 'filter-type'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.value = '';
            });

            fetchData();
        };
    }

    // Botão Sync (Sincronização)
    const btnSync = document.getElementById('btn-sync');
    if (btnSync) {
        btnSync.onclick = async () => {
            UIUtils.showLoading(true);
            btnSync.classList.add('syncing');
            try {
                // Simulação ou chamada real de sincronização
                await new Promise(r => setTimeout(r, 1000));
                fetchData();
                updateNavBadges();
            } finally {
                UIUtils.showLoading(false);
                btnSync.classList.remove('syncing');
            }
        };
    }
}

// Timer para evitar excesso de requisições enquanto o usuário digita
let filterTimeout;
function triggerFilter() {
    clearTimeout(filterTimeout);
    filterTimeout = setTimeout(fetchData, 300);
}

document.addEventListener('DOMContentLoaded', () => {
    initEvents();
    initFoundModal();
    loadModule('registers', 'PENDENTES');
    updateNavBadges();
});
