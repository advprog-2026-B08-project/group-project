function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function formatDate(value) {
    if (!value) {
        return '-';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return '-';
    }

    return new Intl.DateTimeFormat('id-ID', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    }).format(date);
}

function formatPrice(value) {
    const amount = Number(value || 0);
    return new Intl.NumberFormat('id-ID', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

function buildRatingHtml(catalog) {
    const average = Number(catalog.ratingAverage ?? 0);
    const ratingCount = Number(catalog.ratingCount ?? 0);
    let stars = '';

    for (let i = 1; i <= 5; i += 1) {
        const color = average >= (i - 0.5) ? '#f5b301' : '#d0d0d0';
        stars += `<span style="color: ${color};">&#9733;</span>`;
    }

    return `${stars}<span class="rating-count">${average.toFixed(1)} / 5 (${ratingCount} ulasan)</span>`;
}

function getCurrentUserId() {
    const meta = document.querySelector('meta[name="current-user-id"]');
    return meta ? meta.getAttribute('content') : '';
}

function buildFooterAction(catalog) {
    const currentUserId = getCurrentUserId();
    const jastiperId = catalog.jastiperId ? String(catalog.jastiperId) : '';
    const isOwner = currentUserId && currentUserId === jastiperId;

    if (isOwner) {
        return `
            <div class="own-product-actions">
                <span class="own-badge">✨ Produk Kamu</span>
                <a href="/catalog/edit/${escapeHtml(catalog.id)}" class="edit-own-btn">✏️ Edit</a>
            </div>
        `;
    }

    return `<a href="/order/checkout/${escapeHtml(catalog.id)}" class="order-btn">🛒 Order</a>`;
}

function buildCatalogCard(catalog) {
    const imageHtml = catalog.imageUrl
        ? `<img src="${escapeHtml(catalog.imageUrl)}" alt="Product Image">`
        : '<span class="no-image">📷</span>';

    const sellerName = catalog.jastiperUsername ? escapeHtml(catalog.jastiperUsername) : '-';
    const sellerId = catalog.jastiperId ? escapeHtml(catalog.jastiperId) : '';
    const travelDate = formatDate(catalog.travelDate);
    const stockId = `stock-${escapeHtml(catalog.id)}`;
    const stock = Number(catalog.stock || 0);

    return `
        <div class="catalog-card" data-catalog-id="${escapeHtml(catalog.id)}">
            <div class="catalog-card-image">${imageHtml}</div>
            <div class="catalog-card-body">
                <div class="catalog-card-name">${escapeHtml(catalog.name || '')}</div>
                <div class="catalog-card-desc">${escapeHtml(catalog.description || '')}</div>
                <div class="catalog-card-meta">
                    <span class="meta-chip location">📍 ${escapeHtml(catalog.originLocation || '')}</span>
                    <span class="meta-chip date">📅 ${travelDate}</span>
                </div>
                <div class="catalog-card-rating">${buildRatingHtml(catalog)}</div>
                <div style="margin-top: auto; padding-top: 6px;">
                    <a href="/catalog/${sellerId}" class="jastiper-link">👤 ${sellerName}</a>
                </div>
            </div>
            <div class="catalog-card-footer">
                <div>
                    <div class="catalog-card-price">Rp ${formatPrice(catalog.price)}</div>
                    <span class="stock-badge${stock <= 0 ? ' out' : ''}" id="${stockId}">${stock > 0 ? 'Stok: ' + stock : 'Habis'}</span>
                </div>
                ${buildFooterAction(catalog)}
            </div>
        </div>
    `;
}

function renderCatalogGrid(catalogs) {
    const grid = document.getElementById('catalog-grid');
    if (!grid) {
        return;
    }

    if (!catalogs || catalogs.length === 0) {
        grid.innerHTML = `
            <div class="catalog-empty" style="grid-column: 1 / -1;">
                <span class="catalog-empty-icon">🔍</span>
                <p>Tidak ada produk ditemukan.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = catalogs.map(buildCatalogCard).join('');
    bindCardClicks();
}

async function loadCatalogs() {
    const searchInput = document.getElementById('catalogSearchInput');
    const params = new URLSearchParams();

    if (searchInput && searchInput.value.trim()) {
        const keyword = searchInput.value.trim();
        params.set('keyword', keyword);
    }

    const response = await fetch('/api/catalogs/search?' + params.toString());
    if (!response.ok) {
        throw new Error('Failed to load catalogs');
    }

    return response.json();
}

async function handleSearch(event) {
    if (event) {
        event.preventDefault();
    }

    try {
        const catalogs = await loadCatalogs();
        renderCatalogGrid(catalogs);
    } catch (error) {
        alert('Gagal mencari produk.');
    }
}

function handleSearchBtn() {
    handleSearch(null);
}

async function resetSearch() {
    const searchInput = document.getElementById('catalogSearchInput');

    if (searchInput) {
        searchInput.value = '';
    }

    try {
        const catalogs = await loadCatalogs();
        renderCatalogGrid(catalogs);
    } catch (error) {
        alert('Gagal mereset pencarian.');
    }
}

function bindCardClicks() {
    document.querySelectorAll('.catalog-card[data-catalog-id]').forEach(function(card) {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function(e) {
            if (e.target.closest('a, button')) return;
            window.location.href = '/catalog/detail/' + card.dataset.catalogId;
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('catalogSearchInput');
    const resetButton = document.getElementById('catalog-search-reset');

    if (searchInput) {
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleSearch(null);
            }
        });
    }

    if (resetButton) {
        resetButton.addEventListener('click', resetSearch);
    }

    bindCardClicks();
});
