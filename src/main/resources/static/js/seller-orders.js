(function () {
    function showWalletRequiredModal(message) {
        var modal = document.getElementById('walletRequiredModal');
        var textEl = document.getElementById('walletRequiredModalText');
        if (textEl && message) textEl.textContent = message;
        if (modal) {
            modal.classList.remove('hidden');
            modal.setAttribute('aria-hidden', 'false');
        }
    }

    function hideWalletRequiredModal() {
        var modal = document.getElementById('walletRequiredModal');
        if (modal) {
            modal.classList.add('hidden');
            modal.setAttribute('aria-hidden', 'true');
        }
    }

    function showPayoutErrorModal(message) {
        var modal = document.getElementById('payoutErrorModal');
        var textEl = document.getElementById('payoutErrorModalText');
        if (textEl) textEl.textContent = message || '';
        if (modal) {
            modal.classList.remove('hidden');
            modal.setAttribute('aria-hidden', 'false');
        }
    }

    function hidePayoutErrorModal() {
        var modal = document.getElementById('payoutErrorModal');
        if (modal) {
            modal.classList.add('hidden');
            modal.setAttribute('aria-hidden', 'true');
        }
    }

    var walletBackdrop = document.getElementById('walletRequiredModalBackdrop');
    var walletClose = document.getElementById('walletRequiredModalClose');
    if (walletBackdrop) walletBackdrop.addEventListener('click', hideWalletRequiredModal);
    if (walletClose) walletClose.addEventListener('click', hideWalletRequiredModal);

    var payoutBackdrop = document.getElementById('payoutErrorModalBackdrop');
    var payoutClose = document.getElementById('payoutErrorModalClose');
    if (payoutBackdrop) payoutBackdrop.addEventListener('click', hidePayoutErrorModal);
    if (payoutClose) payoutClose.addEventListener('click', hidePayoutErrorModal);

    document.querySelectorAll('.ship-order-btn').forEach(function (btn) {
        btn.addEventListener('click', async function () {
            var orderId = btn.getAttribute('data-order-id');
            if (!orderId) return;
            btn.disabled = true;
            var label = btn.textContent;
            btn.textContent = 'Отправка…';
            try {
                var r = await fetch('/api/orders/' + encodeURIComponent(orderId) + '/status', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ status: 'SHIPPED' })
                });
                if (!r.ok) {
                    var errText = await r.text();
                    throw new Error(errText || r.statusText);
                }
                window.location.reload();
            } catch (e) {
                console.error(e);
                alert('Не удалось отметить заказ отправленным. ' + (e.message || ''));
                btn.disabled = false;
                btn.textContent = label;
            }
        });
    });

    document.querySelectorAll('.payout-order-btn').forEach(function (btn) {
        btn.addEventListener('click', async function () {
            var orderId = btn.getAttribute('data-order-id');
            if (!orderId) return;
            if (!confirm('Вывести средства по этому заказу на кошелёк из профиля через OxaPay?')) return;
            btn.disabled = true;
            var label = btn.textContent;
            btn.textContent = 'Запрос…';
            try {
                var r = await fetch('/api/orders/' + encodeURIComponent(orderId) + '/payout', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }
                });
                var errText = await r.text();
                if (!r.ok) {
                    var errJson = null;
                    try {
                        errJson = errText ? JSON.parse(errText) : null;
                    } catch (parseErr) { /* plain text */ }
                    if (r.status === 400 && errJson && errJson.error === 'WALLET_REQUIRED') {
                        showWalletRequiredModal(errJson.message);
                        btn.disabled = false;
                        btn.textContent = label;
                        return;
                    }
                    if (r.status === 400 && errJson && (errJson.error === 'INVALID_PAYOUT_ADDRESS' || errJson.error === 'OXAPAY_PAYOUT_ERROR')) {
                        showPayoutErrorModal(errJson.message);
                        btn.disabled = false;
                        btn.textContent = label;
                        return;
                    }
                    throw new Error((errJson && errJson.message) || errText || r.statusText);
                }
                window.location.reload();
            } catch (e) {
                console.error(e);
                alert('Не удалось вывести средства. ' + (e.message || ''));
                btn.disabled = false;
                btn.textContent = label;
            }
        });
    });
})();
