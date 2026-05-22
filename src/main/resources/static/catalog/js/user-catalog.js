/* ── User Catalog (Jastiper Profile) — Pagination ── */

const USER_ITEMS_PER_PAGE = 12;
let userCurrentPage = 1;
let userAllCards = [];

function renderUserPagination(totalItems) {
    var container = document.getElementById('user-catalog-pagination');
    if (!container) return;

    var totalPages = Math.ceil(totalItems / USER_ITEMS_PER_PAGE);
    if (totalPages <= 1) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'flex';
    var html = '';

    if (userCurrentPage > 1) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="userGoToPage(' + (userCurrentPage - 1) + ')">&laquo; Prev</button>';
    }

    var maxVisible = 5;
    var startPage = Math.max(1, userCurrentPage - Math.floor(maxVisible / 2));
    var endPage = Math.min(totalPages, startPage + maxVisible - 1);
    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(1, endPage - maxVisible + 1);
    }

    if (startPage > 1) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="userGoToPage(1)">1</button>';
        if (startPage > 2) html += '<span style="padding: 0 4px; color: var(--me-text-muted);">…</span>';
    }

    for (var i = startPage; i <= endPage; i++) {
        if (i === userCurrentPage) {
            html += '<button class="me-btn me-btn--primary me-btn--sm">' + i + '</button>';
        } else {
            html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="userGoToPage(' + i + ')">' + i + '</button>';
        }
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) html += '<span style="padding: 0 4px; color: var(--me-text-muted);">…</span>';
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="userGoToPage(' + totalPages + ')">' + totalPages + '</button>';
    }

    if (userCurrentPage < totalPages) {
        html += '<button class="me-btn me-btn--outline me-btn--sm" onclick="userGoToPage(' + (userCurrentPage + 1) + ')">Next &raquo;</button>';
    }

    container.innerHTML = html;
}

function userDisplayPage() {
    var start = (userCurrentPage - 1) * USER_ITEMS_PER_PAGE;
    var end = start + USER_ITEMS_PER_PAGE;

    userAllCards.forEach(function(card, index) {
        card.style.display = (index >= start && index < end) ? '' : 'none';
    });

    renderUserPagination(userAllCards.length);
}

function userGoToPage(page) {
    var totalPages = Math.ceil(userAllCards.length / USER_ITEMS_PER_PAGE);
    if (page < 1 || page > totalPages) return;
    userCurrentPage = page;
    userDisplayPage();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.addEventListener('DOMContentLoaded', function() {
    var grid = document.getElementById('user-catalog-grid');
    if (!grid) return;

    userAllCards = Array.from(grid.querySelectorAll('.catalog-card'));

    // Card click → detail page (exclude clicks on buttons/links)
    userAllCards.forEach(function(card) {
        card.style.cursor = 'pointer';
        card.addEventListener('click', function(e) {
            if (e.target.closest('a, button')) return;
            var id = card.getAttribute('data-catalog-id');
            if (id) window.location.href = '/catalog/detail/' + id;
        });
    });

    if (userAllCards.length > USER_ITEMS_PER_PAGE) {
        userDisplayPage();
    }
});
