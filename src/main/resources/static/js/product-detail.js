// src/main/resources/static/js/product-detail.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 ProductDetail JS загружен');
    console.log('📊 window.productData:', window.productData);
    console.log('📊 window.currentUser:', window.currentUser);

    initButtons();
});

function initButtons() {
    const buyBtn = document.getElementById('buyNow');
    const favBtn = document.getElementById('addToFavorites');

    console.log('🔍 buyBtn:', buyBtn);
    console.log('🔍 favBtn:', favBtn);
    console.log('🔍 currentUser:', window.currentUser);

    // КУПИТЬ
    if (buyBtn && window.currentUser) {
        buyBtn.style.border = '2px solid green'; // ✅ ЗЕЛЁНАЯ РАМКА
        buyBtn.addEventListener('click', buyNow);
        console.log('✅ КУПИТЬ подключена');
    } else {
        console.log('❌ КУПИТЬ:', buyBtn ? 'есть' : 'НЕТ', window.currentUser ? 'есть' : 'НЕТ');
    }

    // ИЗБРАННОЕ
    if (favBtn && window.currentUser) {
        favBtn.style.border = '2px solid blue'; // ✅ СИНЯЯ РАМКА
        favBtn.addEventListener('click', toggleFavorite);
        console.log('✅ ИЗБРАННОЕ подключена');
    } else {
        console.log('❌ ИЗБРАННОЕ:', favBtn ? 'есть' : 'НЕТ', window.currentUser ? 'есть' : 'НЕТ');
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

// 🛒 КУПИТЬ
async function buyNow() {
    const btn = document.getElementById('buyNow');
    btn.disabled = true;
    btn.innerHTML = '⏳ Создание заказа...';

    try {
        const orderData = {
            productId: parseInt(window.productData.id),
            quantity: 1,
            shippingAddress: {
                street: "ул. Тестовая 1",
                city: "Москва",
                state: "Московская область",
                zipCode: "123456",
                country: "Россия"
            }
        };

        console.log('📤 Отправка заказа:', orderData);
        await apiCall('/api/orders', 'POST', orderData);

        showToast('✅ Заказ создан! Переход на /orders...', 'success');
        setTimeout(() => window.location.href = '/orders', 1500);

    } catch (error) {
        console.error('❌ Ошибка заказа:', error);
        showToast('❌ ' + error.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '🛒 Купить сейчас';
    }
}

// ❤️ ИЗБРАННОЕ (НОВЫЙ URL)
async function toggleFavorite() {
    const btn = document.getElementById('addToFavorites');
    const isFavorited = btn.dataset.favorited === 'true';

    try {
        // 🔥 НОВЫЙ URL - БЕЗ /users/{id}/favorites/
        const url = `/api/favorites/toggle/${window.productData.id}`;
        console.log('📤 Избранное:', url, isFavorited ? 'DELETE' : 'POST');

        await apiCall(url, isFavorited ? 'DELETE' : 'POST');

        btn.dataset.favorited = (!isFavorited).toString();
        btn.innerHTML = isFavorited ? '❤️ В избранное' : '❤️ Уже в избранном';
        btn.classList.toggle('bg-red-100', !isFavorited);
        btn.classList.toggle('text-red-600', !isFavorited);

        showToast(isFavorited ? '❌ Удалено из избранного' : '❤️ Добавлено!', 'success');

    } catch (error) {
        console.error('❌ Ошибка избранного:', error);
        showToast('❌ ' + error.message, 'error');
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
