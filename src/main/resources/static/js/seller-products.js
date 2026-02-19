(function() {
    const csrf = document.querySelector('meta[name="_csrf"]')?.content || '';

    function api(method, url, body) {
        const opt = { method, headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf } };
        if (body) opt.body = JSON.stringify(body);
        return fetch(url, opt).then(r => {
            if (!r.ok) return r.text().then(t => { throw new Error(t); });
            return method === 'DELETE' ? r : r.json();
        });
    }

    function showToast(msg, type) {
        const el = document.createElement('div');
        el.className = 'fixed top-20 right-4 z-50 px-6 py-4 rounded-xl shadow-2xl text-white font-semibold ' + (type === 'success' ? 'bg-green-500' : 'bg-red-500');
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 3000);
    }

    const modal = document.getElementById('productModal');
    const modalTitle = document.getElementById('modalTitle');
    const form = document.getElementById('productForm');
    const productIdInput = document.getElementById('productId');
    const productTitleInput = document.getElementById('productTitle');
    const productDescriptionInput = document.getElementById('productDescription');
    const productCategoryInput = document.getElementById('productCategory');
    const productPriceInput = document.getElementById('productPrice');
    const productStockInput = document.getElementById('productStock');
    const productImagesInput = document.getElementById('productImages');

    function openAddModal() {
        productIdInput.value = '';
        productTitleInput.value = '';
        productDescriptionInput.value = '';
        productCategoryInput.value = '';
        productPriceInput.value = '';
        productStockInput.value = '0';
        productImagesInput.value = '/images/placeholder.png';
        modalTitle.textContent = 'Add product';
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function openEditModal(product) {
        productIdInput.value = product.id;
        productTitleInput.value = product.title;
        productDescriptionInput.value = product.description;
        productCategoryInput.value = product.category;
        productPriceInput.value = product.price;
        productStockInput.value = product.stock;
        productImagesInput.value = (product.images || []).join('\n');
        modalTitle.textContent = 'Edit product';
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function closeModal() {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }

    function parseImages(text) {
        return text.split('\n').map(s => s.trim()).filter(Boolean);
    }

    document.getElementById('addProductBtn')?.addEventListener('click', openAddModal);
    document.getElementById('addProductBtnEmpty')?.addEventListener('click', openAddModal);
    document.getElementById('modalCancel')?.addEventListener('click', closeModal);

    document.getElementById('modalSave')?.addEventListener('click', async () => {
        const id = productIdInput.value;
        const payload = {
            title: productTitleInput.value,
            description: productDescriptionInput.value,
            category: productCategoryInput.value,
            price: parseFloat(productPriceInput.value),
            stock: parseInt(productStockInput.value) || 0,
            images: parseImages(productImagesInput.value)
        };
        try {
            if (id) {
                await api('PUT', '/api/products/' + id, payload);
                showToast('Product updated', 'success');
            } else {
                await api('POST', '/api/products', payload);
                showToast('Product created as draft', 'success');
            }
            closeModal();
            window.location.reload();
        } catch (e) {
            showToast(e.message || 'Error', 'error');
        }
    });

    document.querySelectorAll('.seller-edit-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const id = btn.dataset.id;
            try {
                const res = await fetch('/api/products/' + id);
                const product = await res.json();
                openEditModal(product);
            } catch (err) {
                showToast('Failed to load product', 'error');
            }
        });
    });

    document.querySelectorAll('.seller-publish-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const id = btn.dataset.id;
            try {
                await api('POST', '/api/products/' + id + '/publish');
                showToast('Product published', 'success');
                window.location.reload();
            } catch (err) {
                showToast(err.message || 'Failed', 'error');
            }
        });
    });

    document.querySelectorAll('.seller-unpublish-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const id = btn.dataset.id;
            try {
                await api('POST', '/api/products/' + id + '/unpublish');
                showToast('Product unpublished', 'success');
                window.location.reload();
            } catch (err) {
                showToast(err.message || 'Failed', 'error');
            }
        });
    });

    document.querySelectorAll('.seller-delete-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            if (!confirm('Delete this product?')) return;
            const id = btn.dataset.id;
            try {
                await api('DELETE', '/api/products/' + id);
                showToast('Product deleted', 'success');
                document.querySelector(`.seller-product-card[data-id="${id}"]`)?.remove();
                window.location.reload();
            } catch (err) {
                showToast(err.message || 'Failed', 'error');
            }
        });
    });
})();
