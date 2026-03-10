// src/main/resources/static/js/product-detail.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('ProductDetail JS loaded');
    console.log('📊 window.productData:', window.productData);
    console.log('📊 window.currentUser:', window.currentUser);

    initButtons();
    loadReviews();
});

function loadReviews() {
    const container = document.getElementById('reviewsContainer');
    const productId = window.productData && (window.productData.id != null) ? String(window.productData.id) : null;
    if (!container || !productId) return;

    fetch('/api/reviews/product/' + encodeURIComponent(productId) + '?page=0&size=20')
        .then(function(res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        })
        .then(function(data) {
            var raw = data.reviews;
            var reviews = Array.isArray(raw) ? raw : (raw && raw.content) ? raw.content : [];
            if (reviews.length === 0) {
                container.innerHTML = '<p class="text-center py-8 text-gray-500">No reviews yet. Be the first to leave one!</p>';
            } else {
                container.innerHTML = reviews.map(function(r) {
                    var rating = Math.min(5, Math.max(0, parseInt(r.rating, 10) || 0));
                    var starsFull = '';
                    var starsEmpty = '';
                    for (var i = 0; i < rating; i++) starsFull += '★';
                    for (var i = rating; i < 5; i++) starsEmpty += '☆';
                    var dateStr = r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '';
                    return '<div class="bg-gray-50 rounded-lg p-4">' +
                        '<div class="flex items-center justify-between gap-2 mb-2">' +
                        '<span class="font-medium text-gray-900">' + escapeHtml(r.authorName || 'Anonymous') + '</span>' +
                        '<span class="text-amber-500">' + starsFull + starsEmpty + '</span>' +
                        '</div>' +
                        '<p class="text-gray-700 text-sm">' + escapeHtml(r.comment || '') + '</p>' +
                        (dateStr ? '<p class="text-gray-400 text-xs mt-2">' + dateStr + '</p>' : '') +
                        '</div>';
                }).join('');
            }
        })
        .catch(function(err) {
            console.error('Load reviews error:', err);
            container.innerHTML = '<p class="text-center py-8 text-gray-500">Could not load reviews.</p>';
        });
}

function escapeHtml(s) {
    if (s == null) return '';
    var div = document.createElement('div');
    div.textContent = s;
    return div.innerHTML;
}

function initButtons() {
    const addToCartBtn = document.getElementById('addToCart');
    const buyBtn = document.getElementById('buyNow');
    const favBtn = document.getElementById('addToFavorites');

    if (addToCartBtn && window.currentUser) {
        addToCartBtn.addEventListener('click', addToCart);
    }
    if (buyBtn && window.currentUser) {
        buyBtn.addEventListener('click', buyNow);
    }
    if (favBtn && window.currentUser) {
        favBtn.addEventListener('click', toggleFavorite);
    }
}

async function addToCart() {
    const btn = document.getElementById('addToCart');
    if (!btn || !window.productData) return;
    btn.disabled = true;
    btn.textContent = 'Adding...';
    try {
        await apiCall('/api/cart/items', 'POST', { productId: String(window.productData.id), quantity: 1 });
        showToast('Added to cart', 'success');
        btn.textContent = 'Add to cart';
    } catch (e) {
        showToast(e.message || 'Failed to add to cart', 'error');
        btn.textContent = 'Add to cart';
    } finally {
        btn.disabled = false;
    }
}

async function apiCall(url, method = 'GET', data = null) {
    const config = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        }
    };
    if (data) config.body = JSON.stringify(data);

    const response = await fetch(url, config);
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errorText}`);
    }
    return method === 'GET' ? await response.json() : response;
}

// Buy now
async function buyNow() {
    const btn = document.getElementById('buyNow');
    btn.disabled = true;
    btn.innerHTML = '⏳ Creating order...';

    try {
        const orderData = {
            productId: String(window.productData.id),
            quantity: 1,
            shippingAddress: {
                street: "123 Test St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
            }
        };

        await apiCall('/api/orders', 'POST', orderData);

        showToast('Order created! Redirecting to /orders...', 'success');
        setTimeout(() => window.location.href = '/orders', 1500);

    } catch (error) {
        console.error('Order error:', error);
        showToast(error.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '🛒 Buy now';
    }
}

// Favorites (heart button on image)
async function toggleFavorite() {
    const btn = document.getElementById('addToFavorites');
    if (!btn) return;
    const isFavorited = btn.dataset.favorited === 'true';

    try {
        const url = `/api/favorites/toggle/${window.productData.id}`;
        await apiCall(url, 'POST');

        btn.dataset.favorited = (!isFavorited).toString();
        btn.classList.toggle('favorited', !isFavorited);

        showToast(isFavorited ? 'Removed from favorites' : 'Added to favorites!', 'success');

    } catch (error) {
        console.error('Favorites error:', error);
        showToast(error.message || 'Error', 'error');
    }
}


function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `fixed top-20 right-4 z-50 px-6 py-4 rounded-xl shadow-2xl text-white font-semibold max-w-sm
        ${type === 'success' ? 'bg-green-500' : 'bg-red-500'}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}
