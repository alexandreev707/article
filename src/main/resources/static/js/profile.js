(function() {
    const csrf = document.querySelector('meta[name="_csrf"]')?.content || '';

    async function api(method, url, body) {
        const opt = { method, headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf } };
        if (body) opt.body = JSON.stringify(body);
        const r = await fetch(url, opt);
        if (!r.ok) throw new Error(await r.text());
        return method === 'GET' ? r.json() : r;
    }

    function showMessage(msg, isError) {
        const el = document.getElementById('profileMessage');
        el.textContent = msg;
        el.className = 'mt-4 text-sm ' + (isError ? 'text-red-600' : 'text-green-600');
        el.classList.remove('hidden');
        setTimeout(() => el.classList.add('hidden'), 4000);
    }

    async function loadProfile() {
        try {
            const profile = await api('GET', '/api/profile');
            document.getElementById('email').value = profile.email || '';
            document.getElementById('username').value = profile.username || '';
            document.getElementById('fullName').value = profile.fullName || '';
            document.getElementById('phoneNumber').value = profile.phoneNumber || '';
            const walletEl = document.getElementById('walletAddress');
            if (walletEl) walletEl.value = profile.walletAddress || '';
        } catch (e) {
            showMessage('Could not load profile', true);
        }
    }

    document.getElementById('profileForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const btn = document.getElementById('saveBtn');
        btn.disabled = true;
        try {
            await api('PUT', '/api/profile', {
                fullName: document.getElementById('fullName').value.trim() || null,
                phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
                walletAddress: (document.getElementById('walletAddress') && document.getElementById('walletAddress').value.trim()) || null
            });
            showMessage('Saved successfully', false);
        } catch (err) {
            showMessage(err.message || 'Could not save profile', true);
        }
        btn.disabled = false;
    });

    document.addEventListener('DOMContentLoaded', loadProfile);
})();
