(function() {
    const csrf = document.querySelector('meta[name="_csrf"]')?.content || '';

    async function api(method, url, body) {
        const opt = { method, headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf } };
        if (body) opt.body = JSON.stringify(body);
        const r = await fetch(url, opt);
        if (!r.ok) throw new Error(await r.text());
        return method === 'GET' ? r.json() : r;
    }

    function showToast(msg, type) {
        const el = document.createElement('div');
        el.className = 'fixed top-20 right-4 z-50 px-6 py-4 rounded-xl shadow-2xl text-white font-semibold max-w-sm ' + (type === 'success' ? 'bg-green-500' : 'bg-red-500');
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 3000);
    }

    function renderCart(data) {
        const container = document.getElementById('cartItems');
        const empty = document.getElementById('cartEmpty');
        const proceed = document.getElementById('proceedToCheckout');
        const badge = document.getElementById('cartCountBadge');
        const summaryItems = document.getElementById('summaryItems');
        const summarySubtotal = document.getElementById('summarySubtotal');

        if (!data.items || data.items.length === 0) {
            container.classList.add('hidden');
            empty.classList.remove('hidden');
            proceed.classList.add('hidden');
            badge.textContent = '';
            summaryItems.textContent = '0 items';
            summarySubtotal.textContent = '$0.00';
            return;
        }

        empty.classList.add('hidden');
        container.classList.remove('hidden');
        proceed.classList.remove('hidden');
        badge.textContent = data.totalItems ? `(${data.totalItems})` : '';
        summaryItems.textContent = data.totalItems + ' item(s)';
        summarySubtotal.textContent = '$' + Number(data.subtotal).toFixed(2);

        container.innerHTML = data.items.map(item => {
            const img = item.imageUrl || '/images/placeholder.jpg';
            const price = Number(item.price).toFixed(2);
            const lineTotal = (Number(item.price) * item.quantity).toFixed(2);
            return `
                <div class="cart-item-card bg-white rounded-xl border border-gray-200 p-4 flex flex-wrap gap-4 items-start" data-product-id="${item.productId}">
                    <div class="flex-shrink-0 w-24 h-24 bg-gray-100 rounded-lg overflow-hidden">
                        <img src="${img}" alt="" class="w-full h-full object-cover" onerror="this.parentElement.innerHTML='<div class=\\'w-full h-full flex items-center justify-center text-gray-400 text-xs\\'>No photo</div>'">
                    </div>
                    <div class="flex-1 min-w-0">
                        <a href="/products/${item.productId}" class="font-medium text-gray-900 hover:text-primary line-clamp-2">${escapeHtml(item.title)}</a>
                        <p class="text-primary font-semibold mt-1">$${price}</p>
                        <div class="flex flex-wrap items-center gap-2 mt-2">
                            <button type="button" class="cart-qty-minus inline-flex items-center justify-center w-8 h-8 rounded-lg border border-gray-300 bg-white hover:bg-gray-50 text-gray-700" data-product-id="${item.productId}" ${item.quantity <= 1 ? 'disabled' : ''}>−</button>
                            <span class="cart-qty-value font-medium w-8 text-center" data-product-id="${item.productId}">${item.quantity}</span>
                            <button type="button" class="cart-qty-plus inline-flex items-center justify-center w-8 h-8 rounded-lg border border-gray-300 bg-white hover:bg-gray-50 text-gray-700" data-product-id="${item.productId}" ${item.quantity >= item.stock ? 'disabled' : ''}>+</button>
                            ${item.quantity >= item.stock ? '<span class="text-red-500 text-xs">Limited stock</span>' : ''}
                        </div>
                        <div class="flex gap-2 mt-2">
                            <button type="button" class="cart-fav p-2 rounded-lg border border-gray-200 hover:bg-red-50 text-gray-500 hover:text-red-500" data-product-id="${item.productId}" title="Add to favorites">❤️</button>
                            <button type="button" class="cart-remove p-2 rounded-lg border border-gray-200 hover:bg-red-50 text-gray-500 hover:text-red-500" data-product-id="${item.productId}" title="Remove">🗑️</button>
                            <a href="/products/${item.productId}" class="btn-primary text-sm py-2 px-4">Buy</a>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        container.querySelectorAll('.cart-qty-minus').forEach(btn => btn.addEventListener('click', () => updateQty(btn.dataset.productId, -1)));
        container.querySelectorAll('.cart-qty-plus').forEach(btn => btn.addEventListener('click', () => updateQty(btn.dataset.productId, 1)));
        container.querySelectorAll('.cart-remove').forEach(btn => btn.addEventListener('click', () => removeItem(btn.dataset.productId)));
        container.querySelectorAll('.cart-fav').forEach(btn => btn.addEventListener('click', () => toggleFavorite(btn.dataset.productId)));
    }

    function escapeHtml(s) {
        const div = document.createElement('div');
        div.textContent = s;
        return div.innerHTML;
    }

    async function loadCart() {
        try {
            const data = await api('GET', '/api/cart');
            renderCart(data);
        } catch (e) {
            document.getElementById('cartItems').innerHTML = '<p class="text-center py-12 text-red-500">Failed to load cart</p>';
            document.getElementById('cartEmpty').classList.add('hidden');
        }
    }

    async function updateQty(productId, delta) {
        const valEl = document.querySelector('.cart-qty-value[data-product-id="' + productId + '"]');
        const current = parseInt(valEl.textContent, 10);
        const newQty = Math.max(1, current + delta);
        try {
            await api('PUT', '/api/cart/items/' + productId, { quantity: newQty });
            await loadCart();
        } catch (e) {
            showToast('Failed to update quantity', 'error');
        }
    }

    async function removeItem(productId) {
        try {
            await api('DELETE', '/api/cart/items/' + productId);
            await loadCart();
            showToast('Removed from cart', 'success');
        } catch (e) {
            showToast('Failed to remove', 'error');
        }
    }

    async function toggleFavorite(productId) {
        try {
            await api('POST', '/api/favorites/toggle/' + productId);
            showToast('Added to favorites', 'success');
        } catch (e) {
            showToast('Failed to add to favorites', 'error');
        }
    }

    document.addEventListener('DOMContentLoaded', loadCart);
})();
