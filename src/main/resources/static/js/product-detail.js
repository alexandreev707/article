// src/main/resources/static/js/product-detail.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('ProductDetail JS loaded');
    console.log('📊 window.productData:', window.productData);
    console.log('📊 window.currentUser:', window.currentUser);

    initButtons();
});

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
        await apiCall('/api/cart/items', 'POST', { productId: parseInt(window.productData.id), quantity: 1 });
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
            productId: parseInt(window.productData.id),
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
        await apiCall(url, isFavorited ? 'DELETE' : 'POST');

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
