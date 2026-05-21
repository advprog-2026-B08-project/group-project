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

function buildJastiperRatingHtml(catalog) {
    if (catalog.jastiperRatingAverage === null || catalog.jastiperRatingAverage === undefined) {
        return '<span class="text-muted">-</span>';
    }

    const average = Number(catalog.jastiperRatingAverage);
    let stars = '';

    for (let i = 1; i <= 5; i += 1) {
        const color = average >= (i - 0.5) ? '#f5b301' : '#d0d0d0';
        stars += `<span style="color: ${color};">&#9733;</span>`;
    }

    return `${stars}<span class="text-muted" style="font-size: 0.8rem;"> (${average.toFixed(1)})</span>`;
}

function buildCatalogRow(catalog) {
    const imageHtml = catalog.imageUrl
        ? `<img src="${escapeHtml(catalog.imageUrl)}" alt="Product Image">`
        : '<span class="no-image">📷</span>';

    const sellerName = catalog.jastiperUsername ? escapeHtml(catalog.jastiperUsername) : '-';
    const sellerId = catalog.jastiperId ? escapeHtml(catalog.jastiperId) : '';
    const travelDate = formatDate(catalog.travelDate);
    const stockId = `stock-${escapeHtml(catalog.id)}`;
    const stock = Number(catalog.stock || 0);

    return `
        <tr data-catalog-id="${escapeHtml(catalog.id)}">
            <td>${imageHtml}</td>
            <td>${escapeHtml(catalog.name || '')}</td>
            <td>${escapeHtml(catalog.description || '')}</td>
            <td>Rp ${formatPrice(catalog.price)}</td>
            <td>${buildRatingHtml(catalog)}</td>
            <td>${buildJastiperRatingHtml(catalog)}</td>
            <td id="${stockId}">${escapeHtml(catalog.stock)}</td>
            <td>${escapeHtml(catalog.originLocation || '')}</td>
            <td>${travelDate}</td>
            <td>
                <a href="/catalog/${sellerId}" class="text-primary font-weight-bold">${sellerName}</a>
            </td>
            <td>
                <a href="/order/checkout/${escapeHtml(catalog.id)}" class="btn btn-success btn-sm">
                    🛒 Order
                </a>
            </td>
        </tr>
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
