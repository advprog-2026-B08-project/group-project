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

    return new Intl.DateTimeFormat('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    }).format(date);
}

function formatPrice(value) {
    const amount = Number(value || 0);
    return new Intl.NumberFormat('id-ID', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
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

    return `${stars}<span class="text-muted"> (${ratingCount})</span>`;
}

function buildCatalogRow(catalog) {
    const imageHtml = catalog.imageUrl
        ? `<img src="${escapeHtml(catalog.imageUrl)}" alt="Product Image" style="width: 80px; height: 80px; object-fit: cover;">`
        : '<span class="text-muted">No image</span>';

    const sellerName = catalog.jastiperUsername ? escapeHtml(catalog.jastiperUsername) : '-';
    const sellerId = catalog.jastiperId ? escapeHtml(catalog.jastiperId) : '';
    const travelDate = formatDate(catalog.travelDate);
    const stockId = `stock-${escapeHtml(catalog.id)}`;
    const quantityId = `qty-${escapeHtml(catalog.id)}`;

    return `
        <tr data-catalog-id="${escapeHtml(catalog.id)}">
            <td>${imageHtml}</td>
            <td>${escapeHtml(catalog.name || '')}</td>
            <td>${escapeHtml(catalog.description || '')}</td>
            <td>Rp ${formatPrice(catalog.price)}</td>
            <td>${buildRatingHtml(catalog)}</td>
            <td id="${stockId}">${escapeHtml(catalog.stock)}</td>
            <td>${escapeHtml(catalog.originLocation || '')}</td>
            <td>${travelDate}</td>
            <td>
                <a href="/catalog/${sellerId}" class="text-primary font-weight-bold">${sellerName}</a>
            </td>
            <td>
                <div class="d-flex" style="gap: 0.5rem;">
                    <input type="number" min="1" value="1" class="form-control form-control-sm" style="width: 85px;" id="${quantityId}">
                    <button class="btn btn-success btn-sm" data-catalog-id="${escapeHtml(catalog.id)}" onclick="submitOrderTest(this.dataset.catalogId)">
                        Order Test
                    </button>
                </div>
            </td>
        </tr>
    `;
}

function renderCatalogTable(catalogs) {
    const tableBody = document.getElementById('catalog-table-body');
    if (!tableBody) {
        return;
    }

    if (!catalogs || catalogs.length === 0) {
        tableBody.innerHTML = `
            <tr id="catalog-empty-row">
                <td colspan="10" class="text-center text-muted">
                    <em>No products found</em>
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = catalogs.map(buildCatalogRow).join('');
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
    event.preventDefault();

    try {
        const catalogs = await loadCatalogs();
        renderCatalogTable(catalogs);
    } catch (error) {
        alert('Failed to search catalogs.');
    }
}

async function resetSearch() {
    const searchInput = document.getElementById('catalogSearchInput');

    if (searchInput) {
        searchInput.value = '';
    }

    try {
        const catalogs = await loadCatalogs();
        renderCatalogTable(catalogs);
    } catch (error) {
        alert('Failed to reset catalog search.');
    }
}

async function submitOrderTest(catalogId) {
    const quantityInput = document.getElementById('qty-' + catalogId);
    const quantity = Number(quantityInput ? quantityInput.value : 0);

    if (!Number.isInteger(quantity) || quantity <= 0) {
        alert('Quantity must be a positive integer.');
        return;
    }

    const buyerId = document.querySelector('meta[name="current-user-id"]')?.content;
    if (!buyerId) {
        alert('You must be logged in to place an order.');
        return;
    }

    const shippingAddress = prompt('Enter your shipping address:', 'Jl. Contoh No. 1, Jakarta');
    if (!shippingAddress || shippingAddress.trim() === '') {
        alert('Shipping address is required.');
        return;
    }

    try {
        const response = await fetch('/api/orders/checkout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                buyerId: buyerId,
                productId: catalogId,
                quantity: quantity,
                shippingAddress: shippingAddress.trim()
            })
        });

        if (response.ok) {
            const order = await response.json();
            const stockCell = document.getElementById('stock-' + catalogId);
            if (stockCell) {
                stockCell.textContent = Math.max(0, Number(stockCell.textContent) - quantity);
            }
            alert(`Order placed! Status: ${order.status}. View it in Order List.`);
            return;
        }

        let message = 'Failed to create order.';
        try {
            const text = await response.text();
            if (text) {
                message = text;
            }
        } catch (e) {
        }

        alert(message);
    } catch (error) {
        alert('Failed to call order endpoint.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.getElementById('catalog-search-form');
    const resetButton = document.getElementById('catalog-search-reset');

    if (searchForm) {
        searchForm.addEventListener('submit', handleSearch);
    }

    if (resetButton) {
        resetButton.addEventListener('click', resetSearch);
    }
});
