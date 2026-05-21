/* ── Helpers ─────────────────────────────────── */

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function formatDate(value) {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return new Intl.DateTimeFormat('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }).format(date);
}

function formatPrice(value) {
    const amount = Number(value || 0);
    return new Intl.NumberFormat('id-ID', { minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(amount);
}

function buildStarsHtml(average) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        const cls = average >= (i - 0.5) ? 'me-star-on' : 'me-star-off';
        stars += '<span class="' + cls + '">&#9733;</span>';
    }
    return stars;
}

/* ── Pagination State ───────────────────────── */

const ITEMS_PER_PAGE = 12;
let currentPage = 1;
let allCatalogs = [];

/* ── Card Builder ───────────────────────────── */

function buildCatalogCard(catalog) {
    const imageHtml = catalog.imageUrl
        ? '<img src="' + escapeHtml(catalog.imageUrl) + '" alt="' + escapeHtml(catalog.name) + '" class="catalog-card__img">'
        : '<div class="catalog-card__img-placeholder">📷</div>';

    const avg = Number(catalog.ratingAverage || 0);
    const count = Number(catalog.ratingCount || 0);
    const stock = Number(catalog.stock || 0);
    const sellerName = catalog.jastiperUsername ? escapeHtml(catalog.jastiperUsername) : '-';
    const sellerId = catalog.jastiperId ? escapeHtml(catalog.jastiperId) : '';
    const travelDate = formatDate(catalog.travelDate);
    const origin = catalog.originLocation ? escapeHtml(catalog.originLocation) : '-';

    const currentUserId = document.querySelector('meta[name="current-user-id"]')?.content || '';
    const isOwner = currentUserId && sellerId === currentUserId;

    let actionHtml;
    if (isOwner) {
        actionHtml = '<a href="/catalog/edit/' + escapeHtml(catalog.id) + '" class="me-btn me-btn--outline me-btn--sm">✏️ Edit</a>';
    } else if (stock > 0) {
        actionHtml = '<a href="/order/checkout/' + escapeHtml(catalog.id) + '" class="me-btn me-btn--success me-btn--sm">🛒 Beli</a>';
    } else {
        actionHtml = '<span class="me-badge me-badge--danger">Habis</span>';
    }

    const stockBadge = stock > 0
        ? '<span class="me-badge me-badge--success">Stok: ' + stock + '</span>'
        : '<span class="me-badge me-badge--danger">Habis</span>';

    return '<div class="me-card catalog-card" data-catalog-id="' + escapeHtml(catalog.id) + '">'
        + '<div class="catalog-card__image">' + imageHtml + '</div>'
        + '<div class="catalog-card__body">'
        +   '<div class="catalog-card__name">' + escapeHtml(catalog.name || '') + '</div>'
        +   '<div class="catalog-card__desc">' + escapeHtml(catalog.description || '') + '</div>'
        +   '<div class="catalog-card__price">Rp ' + formatPrice(catalog.price) + '</div>'
        +   '<div class="catalog-card__rating me-stars">'
        +     buildStarsHtml(avg)
        +     ' <span style="font-size: 0.8rem; color: var(--me-text-muted);">' + avg.toFixed(1) + ' (' + count + ')</span>'
        +   '</div>'
        +   '<div class="catalog-card__meta">'
        +     '<span>📍 ' + origin + '</span>'
        +     '<span>🗓 ' + travelDate + '</span>'
        +   '</div>'
        +   '<div class="catalog-card__seller">'
        +     '<a href="/catalog/' + sellerId + '" style="font-weight: 700; color: var(--me-info);">👤 ' + sellerName + '</a>'
        +   '</div>'
        + '</div>'
        + '<div class="catalog-card__footer">'
        +   stockBadge
        +   actionHtml
        + '</div>'
        + '</div>';
}

/* ── Rendering ──────────────────────────────── */

function renderCatalogGrid(catalogs) {
    var grid = document.getElementById('catalog-grid');
    if (!grid) return;

    if (!catalogs || catalogs.length === 0) {
        grid.innerHTML = '<div class="me-card me-center" style="grid-column: 1 / -1; padding: 48px; border-style: dashed; background: var(--me-surface);">'
            + '<span style="font-size: 3rem; display: block; margin-bottom: 16px;">🔍</span>'
            + '<p style="font-weight: 700; font-size: 1.1rem; margin-bottom: 0;">Tidak ada produk ditemukan.</p>'
            + '</div>';
        return;
    }

    grid.innerHTML = catalogs.map(buildCatalogCard).join('');
    bindCardClicks();
}

function renderPagination(totalItems) {
    var container = document.getElementById('catalog-pagination');
    if (!container) return;

    var totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    var html = '';

    // Previous button
    if (currentPage > 1) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="goToPage(' + (currentPage - 1) + ')">&laquo; Prev</button>';
    }

    // Page numbers
    var maxVisible = 5;
    var startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2));
    var endPage = Math.min(totalPages, startPage + maxVisible - 1);
    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(1, endPage - maxVisible + 1);
    }

    if (startPage > 1) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="goToPage(1)">1</button>';
        if (startPage > 2) html += '<span style="padding: 0 4px; color: var(--me-text-muted);">…</span>';
    }

    for (var i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            html += '<button class="me-btn me-btn--primary me-btn--sm">' + i + '</button>';
        } else {
            html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="goToPage(' + i + ')">' + i + '</button>';
        }
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) html += '<span style="padding: 0 4px; color: var(--me-text-muted);">…</span>';
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="goToPage(' + totalPages + ')">' + totalPages + '</button>';
    }

    // Next button
    if (currentPage < totalPages) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="goToPage(' + (currentPage + 1) + ')">Next &raquo;</button>';
    }

    container.innerHTML = html;
}

function displayPage() {
    var start = (currentPage - 1) * ITEMS_PER_PAGE;
    var end = start + ITEMS_PER_PAGE;
    var pageItems = allCatalogs.slice(start, end);
    renderCatalogGrid(pageItems);
    renderPagination(allCatalogs.length);

    // Reveal grid + pagination, hide loading state once data is rendered.
    var loadingEl = document.getElementById('catalog-loading');
    var gridEl = document.getElementById('catalog-grid');
    if (loadingEl) loadingEl.style.display = 'none';
    if (gridEl) gridEl.style.display = '';
}

function showLoadingState() {
    var loadingEl = document.getElementById('catalog-loading');
    var gridEl = document.getElementById('catalog-grid');
    if (loadingEl) loadingEl.style.display = '';
    if (gridEl) gridEl.style.display = 'none';
}

function showErrorState(message) {
    var loadingEl = document.getElementById('catalog-loading');
    var gridEl = document.getElementById('catalog-grid');
    if (loadingEl) {
        loadingEl.innerHTML = '<span style="font-size: 2.5rem; display: block; margin-bottom: 12px;">⚠️</span>'
            + '<p style="font-weight: 700; color: var(--me-danger); margin: 0;">' + message + '</p>';
        loadingEl.style.display = '';
    }
    if (gridEl) gridEl.style.display = 'none';
}

function goToPage(page) {
    var totalPages = Math.ceil(allCatalogs.length / ITEMS_PER_PAGE);
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    displayPage();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/* ── Search / Load ──────────────────────────── */

async function loadCatalogs() {
    var searchInput = document.getElementById('catalogSearchInput');
    var params = new URLSearchParams();

    if (searchInput && searchInput.value.trim()) {
        params.set('keyword', searchInput.value.trim());
    }

    var response = await fetch('/api/catalogs/search?' + params.toString());
    if (!response.ok) throw new Error('Failed to load catalogs');
    return response.json();
}

async function handleSearch(event) {
    if (event) event.preventDefault();
    showLoadingState();
    try {
        allCatalogs = await loadCatalogs();
        currentPage = 1;
        displayPage();
    } catch (error) {
        showErrorState('Gagal mencari produk.');
    }
}

function handleSearchBtn() {
    handleSearch(null);
}

async function resetSearch() {
    var searchInput = document.getElementById('catalogSearchInput');
    if (searchInput) searchInput.value = '';
    showLoadingState();
    try {
        allCatalogs = await loadCatalogs();
        currentPage = 1;
        displayPage();
    } catch (error) {
        showErrorState('Gagal mereset pencarian.');
    }
}

/* ── Card Click → Detail ────────────────────── */

function bindCardClicks() {
    document.querySelectorAll('.catalog-card[data-catalog-id]').forEach(function(card) {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function(e) {
            if (e.target.closest('a, button')) return;
            window.location.href = '/catalog/detail/' + card.dataset.catalogId;
        });
    });
}

/* ── Init ───────────────────────────────────── */

document.addEventListener('DOMContentLoaded', async function() {
    var searchForm = document.getElementById('catalog-search-form');
    var searchInput = document.getElementById('catalogSearchInput');
    var resetButton = document.getElementById('catalog-search-reset');

    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleSearch(null);
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleSearch(null);
            }
        });
    }

    if (resetButton) {
        resetButton.addEventListener('click', resetSearch);
    }

    // Initial load via AJAX — page shell is rendered server-side, data fetched client-side.
    try {
        allCatalogs = await loadCatalogs();
        displayPage();
    } catch (error) {
        showErrorState('Gagal memuat produk.');
    }
});
