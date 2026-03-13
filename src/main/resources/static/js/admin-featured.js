(function() {
    const csrf = document.querySelector('meta[name="_csrf"]')?.content || '';

    async function api(method, url) {
        const opt = { method, headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf } };
        const r = await fetch(url, opt);
        if (!r.ok) throw new Error(await r.text());
        return r.json();
    }

    function toast(msg, type) {
        const el = document.createElement('div');
        el.className = 'fixed top-20 right-4 z-50 px-6 py-4 rounded-xl shadow-2xl text-white font-semibold ' + (type === 'success' ? 'bg-green-500' : 'bg-red-500');
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 3000);
    }

    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('.admin-feature-btn');
        if (btn) {
            e.preventDefault();
            const id = btn.dataset.productId;
            if (!id) return;
            try {
                await api('POST', '/api/admin/products/' + id + '/featured');
                toast('Product featured on homepage', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
            return;
        }
        const unfeatureBtn = e.target.closest('.admin-unfeature-btn');
        if (unfeatureBtn) {
            e.preventDefault();
            const id = unfeatureBtn.dataset.productId;
            if (!id) return;
            try {
                await api('DELETE', '/api/admin/products/' + id + '/featured');
                toast('Removed from homepage', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
        }
    });
})();
