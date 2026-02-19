// Admin panel - user management
(function() {
    const csrf = document.querySelector('meta[name="_csrf"]')?.content || '';

    async function api(method, url, body) {
        const opt = { method, headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf } };
        if (body) opt.body = JSON.stringify(body);
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
        const btn = e.target.closest('.admin-block-btn');
        if (btn) {
            e.preventDefault();
            const id = btn.dataset.userId;
            if (!id || !confirm('Block this user?')) return;
            try {
                await api('POST', '/api/admin/users/' + id + '/block');
                toast('User blocked', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
            return;
        }
        const unblockBtn = e.target.closest('.admin-unblock-btn');
        if (unblockBtn) {
            e.preventDefault();
            const id = unblockBtn.dataset.userId;
            if (!id || !confirm('Unblock this user?')) return;
            try {
                await api('POST', '/api/admin/users/' + id + '/unblock');
                toast('User unblocked', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
            return;
        }
        const verifyBtn = e.target.closest('.admin-verify-btn');
        if (verifyBtn) {
            e.preventDefault();
            const id = verifyBtn.dataset.userId;
            if (!id) return;
            try {
                await api('POST', '/api/admin/users/' + id + '/verify');
                toast('User verified', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
            return;
        }
        const unverifyBtn = e.target.closest('.admin-unverify-btn');
        if (unverifyBtn) {
            e.preventDefault();
            const id = unverifyBtn.dataset.userId;
            if (!id) return;
            try {
                await api('POST', '/api/admin/users/' + id + '/unverify');
                toast('User unverified', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
            return;
        }
        const grantSellerBtn = e.target.closest('.admin-grant-seller-btn');
        if (grantSellerBtn) {
            e.preventDefault();
            const id = grantSellerBtn.dataset.userId;
            if (!id) return;
            try {
                await api('POST', '/api/admin/users/' + id + '/roles/SELLER');
                toast('Role SELLER granted', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
            return;
        }
        const revokeSellerBtn = e.target.closest('.admin-revoke-seller-btn');
        if (revokeSellerBtn) {
            e.preventDefault();
            const id = revokeSellerBtn.dataset.userId;
            if (!id || !confirm('Revoke SELLER role?')) return;
            try {
                await api('DELETE', '/api/admin/users/' + id + '/roles/SELLER');
                toast('Role SELLER revoked', 'success');
                location.reload();
            } catch (err) {
                toast(err.message || 'Failed', 'error');
            }
        }
    });
})();
