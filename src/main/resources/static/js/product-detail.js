// Product detail page functionality

let stompClient = null;
const productId = window.productId || '';
const sellerId = window.sellerId || '';

document.addEventListener('DOMContentLoaded', function() {
    initializeProductDetail();
});

function initializeProductDetail() {
    // Add to favorites
    const addToFavoritesBtn = document.getElementById('addToFavorites');
    if (addToFavoritesBtn) {
        addToFavoritesBtn.addEventListener('click', toggleFavorite);
    }

    // Buy now
    const buyNowBtn = document.getElementById('buyNow');
    if (buyNowBtn) {
        buyNowBtn.addEventListener('click', buyNow);
    }

    // Chat with seller
    const chatWithSellerBtn = document.getElementById('chatWithSeller');
    if (chatWithSellerBtn) {
        chatWithSellerBtn.addEventListener('click', openChat);
    }

    // Load reviews
    loadReviews();

    // Add review button
    const addReviewBtn = document.getElementById('addReviewBtn');
    if (addReviewBtn) {
        addReviewBtn.addEventListener('click', showReviewForm);
    }
}

async function toggleFavorite() {
    try {
        const response = await apiCall(`/api/favorites/${productId}`, 'POST');
        const btn = document.getElementById('addToFavorites');
        if (response.isFavorite) {
            btn.textContent = '❤️ Remove from Favorites';
            btn.classList.remove('btn-secondary');
            btn.classList.add('bg-red-500', 'hover:bg-red-600', 'text-white');
        } else {
            btn.textContent = '❤️ Add to Favorites';
            btn.classList.remove('bg-red-500', 'hover:bg-red-600', 'text-white');
            btn.classList.add('btn-secondary');
        }
        showNotification(response.isFavorite ? 'Added to favorites' : 'Removed from favorites', 'success');
    } catch (error) {
        showNotification('Failed to update favorites', 'error');
    }
}

function buyNow() {
    const address = prompt('Enter shipping address:\nFormat: Street, City, State, ZIP, Country');
    if (!address) return;

    const parts = address.split(',').map(s => s.trim());
    if (parts.length !== 5) {
        showNotification('Invalid address format', 'error');
        return;
    }

    const orderData = {
        productId: productId,
        quantity: 1,
        shippingAddress: {
            street: parts[0],
            city: parts[1],
            state: parts[2],
            zipCode: parts[3],
            country: parts[4]
        }
    };

    apiCall('/api/orders', 'POST', orderData)
        .then(() => {
            showNotification('Order created successfully!', 'success');
        })
        .catch(error => {
            showNotification('Failed to create order', 'error');
        });
}

function openChat() {
    const chatModal = document.getElementById('chatModal');
    chatModal.classList.remove('hidden');
    connectWebSocket();
    loadChatHistory();
}

function closeChat() {
    const chatModal = document.getElementById('chatModal');
    chatModal.classList.add('hidden');
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
}

document.getElementById('closeChatBtn')?.addEventListener('click', closeChat);

function connectWebSocket() {
    const socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        // Subscribe to personal messages
        const userId = getCurrentUserId(); // You'll need to implement this
        stompClient.subscribe(`/topic/chat/${userId}`, function(message) {
            const chatMessage = JSON.parse(message.body);
            displayMessage(chatMessage);
        });
    });
}

function sendMessage() {
    const input = document.getElementById('chatInput');
    const text = input.value.trim();
    if (!text) return;

    const messageData = {
        receiverId: sellerId,
        productId: productId,
        text: text
    };

    if (stompClient && stompClient.connected) {
        stompClient.send('/app/chat.send', {}, JSON.stringify(messageData));
    } else {
        apiCall('/api/chat', 'POST', messageData)
            .then(() => {
                loadChatHistory();
            });
    }

    input.value = '';
}

document.getElementById('sendMessageBtn')?.addEventListener('click', sendMessage);
document.getElementById('chatInput')?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});

function displayMessage(message) {
    const container = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${message.senderId === getCurrentUserId() ? 'sent' : 'received'}`;
    messageDiv.innerHTML = `
        <div class="font-semibold mb-1">${message.senderName}</div>
        <div>${message.text}</div>
        <div class="text-xs opacity-75 mt-1">${new Date(message.timestamp).toLocaleString('ru-RU')}</div>
    `;
    container.appendChild(messageDiv);
    container.scrollTop = container.scrollHeight;
}

async function loadChatHistory() {
    try {
        const response = await apiCall(`/api/chat/conversation?userId=${sellerId}&productId=${productId}`);
        const container = document.getElementById('chatMessages');
        container.innerHTML = '';
        response.messages.forEach(message => {
            displayMessage(message);
        });
    } catch (error) {
        console.error('Failed to load chat history:', error);
    }
}

async function loadReviews() {
    try {
        const response = await apiCall(`/api/reviews/product/${productId}`);
        const container = document.getElementById('reviewsContainer');
        container.innerHTML = '';
        
        if (response.reviews && response.reviews.length > 0) {
            response.reviews.forEach(review => {
                const reviewDiv = document.createElement('div');
                reviewDiv.className = 'bg-gray-50 rounded-lg p-4 mb-4';
                reviewDiv.innerHTML = `
                    <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-2">
                            <strong class="text-gray-900">${review.authorName}</strong>
                            <div class="flex items-center gap-1">
                                ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
                            </div>
                        </div>
                        <small class="text-gray-500">${new Date(review.createdAt).toLocaleString('ru-RU')}</small>
                    </div>
                    <p class="text-gray-700">${review.comment}</p>
                `;
                container.appendChild(reviewDiv);
            });
        } else {
            container.innerHTML = '<p class="text-center text-gray-500 py-8">Пока нет отзывов. Будьте первым!</p>';
        }
    } catch (error) {
        console.error('Failed to load reviews:', error);
    }
}

function showReviewForm() {
    const rating = prompt('Enter rating (1-5):');
    if (!rating || rating < 1 || rating > 5) {
        showNotification('Invalid rating', 'error');
        return;
    }

    const comment = prompt('Enter your review:');
    if (!comment) return;

    const reviewData = {
        productId: productId,
        rating: parseInt(rating),
        comment: comment
    };

    apiCall('/api/reviews', 'POST', reviewData)
        .then(() => {
            showNotification('Review added successfully!', 'success');
            loadReviews();
        })
        .catch(error => {
            showNotification('Failed to add review', 'error');
        });
}

function getCurrentUserId() {
    // This should be set from the server or retrieved from an API
    return window.currentUserId || '';
}

