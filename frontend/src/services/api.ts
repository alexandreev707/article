import axios from 'axios'

const API_BASE_URL = '/api/v1'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add wallet address to headers
apiClient.interceptors.request.use((config) => {
  const walletAddress = localStorage.getItem('walletAddress')
  if (walletAddress) {
    config.headers['X-Wallet-Address'] = walletAddress
  }
  return config
})

export const api = {
  // Products
  async getProducts(params?: { category?: number; max_price?: number; query?: string; page?: number; limit?: number }) {
    const { data } = await apiClient.get('/products', { params })
    return data
  },

  async getProduct(id: number) {
    const { data } = await apiClient.get(`/products/${id}`)
    return data
  },

  async createProduct(product: {
    categoryId: number
    title: string
    description?: string
    priceUsd: number
    images?: string[]
  }) {
    const { data } = await apiClient.post('/products', product)
    return data
  },

  async getSellerProducts(wallet: string, page = 0, limit = 20) {
    const { data } = await apiClient.get(`/products/seller/${wallet}`, { params: { page, limit } })
    return data
  },

  // Categories
  async getCategories() {
    const { data } = await apiClient.get('/categories')
    return data
  },

  // Orders
  async createOrder(order: {
    productId: number
    shippingCountry?: string
    shippingAddress?: string
  }) {
    const { data } = await apiClient.post('/orders', order)
    return data
  },

  async getOrder(id: number) {
    const { data } = await apiClient.get(`/orders/${id}`)
    return data
  },

  async getMyOrders(page = 0, limit = 20) {
    const { data } = await apiClient.get('/orders/buyer/my-orders', { params: { page, limit } })
    return data
  },

  async getSellerOrders(page = 0, limit = 20) {
    const { data } = await apiClient.get('/orders/seller/my-orders', { params: { page, limit } })
    return data
  },

  async confirmPayment(orderId: number, txSignature: string) {
    const { data } = await apiClient.post(`/orders/${orderId}/confirm-payment`, null, {
      params: { txSignature },
    })
    return data
  },

  async confirmDelivery(orderId: number) {
    const { data } = await apiClient.post(`/orders/${orderId}/confirm-delivery`)
    return data
  },
}
