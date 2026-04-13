(function() {
    const csrf = document.querySelector('meta[name="_csrf"]')?.content || '';
    const params = new URLSearchParams(window.location.search);
    const cartProductId = params.get('productId');
    let cart = { items: [], subtotal: 0, totalItems: 0 };
    let deliveryOptions = [];
    let selectedDeliveryId = null;

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

    function renderDeliveryOptions() {
        const container = document.getElementById('deliveryOptions');
        if (!deliveryOptions.length) {
            container.innerHTML = '<p class="text-gray-500">No delivery options available.</p>';
            return;
        }
        container.innerHTML = deliveryOptions.map((opt, i) => `
            <div class="delivery-option flex items-center justify-between p-4 rounded-lg border cursor-pointer ${i === 0 ? 'border-primary bg-blue-50' : 'border-gray-200'}" data-id="${opt.id}">
                <div>
                    <span class="font-medium">${opt.name}</span>
                    <span class="text-gray-500 text-sm ml-2">${opt.estimatedDays ? opt.estimatedDays + ' days' : ''}</span>
                    ${opt.addressLine ? '<p class="text-sm text-gray-600 mt-1">' + opt.addressLine + '</p>' : ''}
                </div>
                <span class="font-semibold text-primary">${Number(opt.price) === 0 ? 'Free' : '$' + Number(opt.price).toFixed(2)}</span>
            </div>
        `).join('');
        if (!selectedDeliveryId && deliveryOptions[0]) selectedDeliveryId = deliveryOptions[0].id;
        document.getElementById('selectedDeliveryId').value = selectedDeliveryId || '';
        container.querySelectorAll('.delivery-option').forEach(el => {
            el.addEventListener('click', () => {
                container.querySelectorAll('.delivery-option').forEach(x => { x.classList.remove('border-primary', 'bg-blue-50'); x.classList.add('border-gray-200'); });
                el.classList.add('border-primary', 'bg-blue-50');
                el.classList.remove('border-gray-200');
                selectedDeliveryId = el.dataset.id;
                document.getElementById('selectedDeliveryId').value = selectedDeliveryId || '';
                updateSummary();
            });
        });
    }

    function updateSummary() {
        const delivery = deliveryOptions.find(d => d.id === selectedDeliveryId);
        const deliveryCost = delivery ? Number(delivery.price) : 0;
        const total = Math.max(0, Number(cart.subtotal) + deliveryCost);
        document.getElementById('summaryLine').textContent = cart.totalItems + ' item(s)';
        document.getElementById('itemsTotal').textContent = '$' + Number(cart.subtotal).toFixed(2);
        document.getElementById('deliveryCost').textContent = deliveryCost === 0 ? 'Free' : '$' + deliveryCost.toFixed(2);
        document.getElementById('totalPrice').textContent = '$' + total.toFixed(2);
    }

    async function loadCart() {
        try {
            cart = await api('GET', '/api/cart');
            if (!cart.items || cart.items.length === 0) {
                window.location.href = '/cart';
                return;
            }
            if (cartProductId) {
                const item = (cart.items || []).find(i => i.productId === cartProductId);
                if (!item) {
                    showToast('Selected product not found in cart', 'error');
                    setTimeout(() => window.location.href = '/cart', 1500);
                    return;
                }
                cart.items = [item];
                cart.totalItems = item.quantity;
                cart.subtotal = Number(item.price) * item.quantity;
            }
            updateSummary();
        } catch (e) {
            showToast('Failed to load cart', 'error');
            setTimeout(() => window.location.href = '/cart', 1500);
        }
    }

    async function loadDeliveryOptions() {
        try {
            deliveryOptions = await api('GET', '/api/delivery-options');
            renderDeliveryOptions();
            updateSummary();
        } catch (e) {
            document.getElementById('deliveryOptions').innerHTML = '<p class="text-red-500">Failed to load delivery options.</p>';
        }
    }

    async function submitCheckout() {
        if (!selectedDeliveryId) {
            showToast('Please select a delivery option', 'error');
            return;
        }
        const btn = document.getElementById('payBtn');
        btn.disabled = true;
        btn.textContent = 'Creating order...';
        try {
            const opt = { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf }, body: JSON.stringify({
                deliveryOptionId: selectedDeliveryId,
                shippingAddress: null,
                cartProductId: cartProductId
            }) };
            const response = await fetch('/api/checkout', opt);
            const raw = await response.text();
            let res = null;
            try {
                res = raw ? JSON.parse(raw) : null;
            } catch (parseErr) {
                res = null;
            }
            if (!response.ok) {
                const msg = (res && (res.message || res.error))
                    || (raw && raw.indexOf('<!DOCTYPE') !== -1
                        ? 'Server error during checkout. Check OXAPAY_MERCHANT_API_KEY, callback/return URL on the host, and server logs.'
                        : raw);
                throw new Error(typeof msg === 'string' ? msg : 'Checkout failed');
            }
            if (!res) throw new Error('Empty server response');
            const paymentUrl = res.paymentUrl;
            if (!paymentUrl) throw new Error('Payment URL is not available');
            window.location.href = paymentUrl;
        } catch (e) {
            btn.disabled = false;
            btn.textContent = 'Proceed to payment';
            showToast(e.message || 'Checkout failed', 'error');
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        loadCart().then(() => loadDeliveryOptions());
        document.getElementById('payBtn').addEventListener('click', submitCheckout);
    });
})();
