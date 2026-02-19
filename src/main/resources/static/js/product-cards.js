// Product card actions: favorite toggle and add to cart
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

    window.toggleFavoriteCard = async function(productId, btn) {
        if (!btn || !productId) return;
        const isFavorited = btn.dataset.favorited === 'true';
        try {
            await api('POST', '/api/favorites/toggle/' + productId);
            btn.dataset.favorited = (!isFavorited).toString();
            updateFavoriteButton(btn, !isFavorited);
            showToast(isFavorited ? 'Removed from favorites' : 'Added to favorites!', 'success');
        } catch (e) {
            showToast(e.message || 'Error', 'error');
        }
    };

    window.addToCartCard = async function(productId, btn) {
        if (!btn || !productId) return;
        btn.disabled = true;
        const origHtml = btn.innerHTML;
        btn.innerHTML = '<span class="inline-block animate-spin mr-2">⏳</span> Adding...';
        try {
            await api('POST', '/api/cart/items', { productId: String(productId), quantity: 1 });
            showToast('Added to cart', 'success');
            btn.innerHTML = '✓ In cart';
            btn.classList.add('bg-green-600');
            setTimeout(() => {
                btn.innerHTML = origHtml;
                btn.classList.remove('bg-green-600');
                btn.disabled = false;
            }, 2000);
        } catch (e) {
            showToast(e.message || 'Add to cart failed', 'error');
            btn.innerHTML = origHtml;
            btn.disabled = false;
        }
    };

    function updateFavoriteButton(btn, isFavorited) {
        btn.dataset.favorited = isFavorited.toString();
        btn.classList.toggle('favorited', isFavorited);
    }
})();
