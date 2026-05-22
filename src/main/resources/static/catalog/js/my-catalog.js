/* ── My Catalog — Pagination + Delete ──────────── */

const MY_ITEMS_PER_PAGE = 12;
let myCurrentPage = 1;
let myAllCards = [];

function deleteCatalog(id) {
    if (confirm('Yakin ingin menghapus produk ini?')) {
        fetch('/api/catalogs/' + id, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(function(response) {
            if (response.ok) {
                alert('Produk berhasil dihapus.');
                location.reload();
            } else {
                alert('Gagal menghapus produk.');
            }
        })
        .catch(function() { alert('Terjadi kesalahan. Coba lagi.'); });
    }
}

function renderMyPagination(totalItems) {
    var container = document.getElementById('my-catalog-pagination');
    if (!container) return;

    var totalPages = Math.ceil(totalItems / MY_ITEMS_PER_PAGE);
    if (totalPages <= 1) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'flex';
    var html = '';

    if (myCurrentPage > 1) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="myGoToPage(' + (myCurrentPage - 1) + ')">&laquo; Prev</button>';
    }

    var maxVisible = 5;
    var startPage = Math.max(1, myCurrentPage - Math.floor(maxVisible / 2));
    var endPage = Math.min(totalPages, startPage + maxVisible - 1);
    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(1, endPage - maxVisible + 1);
    }

    if (startPage > 1) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="myGoToPage(1)">1</button>';
        if (startPage > 2) html += '<span style="padding: 0 4px; color: var(--me-text-muted);">…</span>';
    }

    for (var i = startPage; i <= endPage; i++) {
        if (i === myCurrentPage) {
            html += '<button class="me-btn me-btn--primary me-btn--sm">' + i + '</button>';
        } else {
            html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="myGoToPage(' + i + ')">' + i + '</button>';
        }
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) html += '<span style="padding: 0 4px; color: var(--me-text-muted);">…</span>';
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="myGoToPage(' + totalPages + ')">' + totalPages + '</button>';
    }

    if (myCurrentPage < totalPages) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="myGoToPage(' + (myCurrentPage + 1) + ')">Next &raquo;</button>';
    }

    container.innerHTML = html;
}

function myDisplayPage() {
    var start = (myCurrentPage - 1) * MY_ITEMS_PER_PAGE;
    var end = start + MY_ITEMS_PER_PAGE;

    myAllCards.forEach(function(card, index) {
        card.style.display = (index >= start && index < end) ? '' : 'none';
    });

    renderMyPagination(myAllCards.length);
}

function myGoToPage(page) {
    var totalPages = Math.ceil(myAllCards.length / MY_ITEMS_PER_PAGE);
    if (page < 1 || page > totalPages) return;
    myCurrentPage = page;
    myDisplayPage();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.addEventListener('DOMContentLoaded', function() {
    var grid = document.getElementById('my-catalog-grid');
    if (!grid) return;

    myAllCards = Array.from(grid.querySelectorAll('.my-catalog-card'));

    // Card click → detail page (exclude clicks on buttons/links)
    myAllCards.forEach(function(card) {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function(e) {
            if (e.target.closest('a, button')) return;
            var id = card.getAttribute('data-catalog-id');
            if (id) window.location.href = '/catalog/detail/' + id;
        });
    });

    if (myAllCards.length > MY_ITEMS_PER_PAGE) {
        myDisplayPage();
    }
});
