import { useEffect, useState } from 'react'
import { useWallet } from '@solana/wallet-adapter-react'
import { api } from '../services/api'
import type { Product, Order } from '../types'

interface ProductsPageResponse {
  products: Product[]
  total: number
  page: number
  limit: number
}

interface OrdersPageResponse {
  orders: Order[]
  total: number
  page: number
  limit: number
}

export default function SellerDashboard() {
  const { publicKey } = useWallet()
  const [products, setProducts] = useState<Product[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [showAddForm, setShowAddForm] = useState(false)
  const [newProduct, setNewProduct] = useState({
    categoryId: 1,
    title: '',
    description: '',
    priceUsd: '',
    images: '',
  })

  useEffect(() => {
    if (publicKey) {
      loadData()
    }
  }, [publicKey])

  const loadData = async () => {
    try {
      setLoading(true)
      if (publicKey) {
        localStorage.setItem('walletAddress', publicKey.toString())
        const [productsData, ordersData] = await Promise.all([
          api.getSellerProducts(publicKey.toString()),
          api.getSellerOrders(),
        ])
        setProducts((productsData as ProductsPageResponse).products)
        setOrders((ordersData as OrdersPageResponse).orders)
      }
    } catch (error) {
      console.error('Failed to load data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAddProduct = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!publicKey) return

    try {
      await api.createProduct({
        categoryId: newProduct.categoryId,
        title: newProduct.title,
        description: newProduct.description,
        priceUsd: parseFloat(newProduct.priceUsd),
        images: newProduct.images.split(',').map((url) => url.trim()).filter(Boolean),
      })
      alert('Товар добавлен!')
      setShowAddForm(false)
      setNewProduct({
        categoryId: 1,
        title: '',
        description: '',
        priceUsd: '',
        images: '',
      })
      loadData()
    } catch (error: any) {
      alert(`Ошибка: ${error.response?.data?.message || error.message}`)
    }
  }

  if (!publicKey) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">Подключите кошелек для доступа к панели продавца</p>
      </div>
    )
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Панель продавца</h1>
        <button onClick={() => setShowAddForm(!showAddForm)} className="btn-primary">
          {showAddForm ? 'Отмена' : '+ Добавить товар'}
        </button>
      </div>

      {showAddForm && (
        <div className="card mb-8">
          <h2 className="text-xl font-bold mb-4">Новый товар</h2>
          <form onSubmit={handleAddProduct} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Название</label>
              <input
                type="text"
                value={newProduct.title}
                onChange={(e) => setNewProduct({ ...newProduct, title: e.target.value })}
                className="input"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Описание</label>
              <textarea
                value={newProduct.description}
                onChange={(e) => setNewProduct({ ...newProduct, description: e.target.value })}
                className="input"
                rows={4}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Цена (USD)</label>
              <input
                type="number"
                step="0.01"
                value={newProduct.priceUsd}
                onChange={(e) => setNewProduct({ ...newProduct, priceUsd: e.target.value })}
                className="input"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Изображения (URL через запятую)</label>
              <input
                type="text"
                value={newProduct.images}
                onChange={(e) => setNewProduct({ ...newProduct, images: e.target.value })}
                className="input"
                placeholder="https://example.com/image1.jpg, https://example.com/image2.jpg"
              />
            </div>
            <button type="submit" className="btn-primary">Создать товар</button>
          </form>
        </div>
      )}

      {/* Products */}
      <div className="mb-8">
        <h2 className="text-2xl font-bold mb-4">Мои товары ({products.length})</h2>
        {loading ? (
          <div className="text-center py-8">Загрузка...</div>
        ) : products.length === 0 ? (
          <div className="card text-center py-8">
            <p className="text-gray-500">У вас пока нет товаров</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {products.map((product) => (
              <div key={product.id} className="card">
                {product.images && product.images.length > 0 && (
                  <img
                    src={product.images[0]}
                    alt={product.title}
                    className="w-full h-32 object-cover rounded-lg mb-2"
                  />
                )}
                <h3 className="font-semibold mb-1">{product.title}</h3>
                <p className="text-primary-600 font-bold">${product.priceUsd}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Orders */}
      <div>
        <h2 className="text-2xl font-bold mb-4">Заказы ({orders.length})</h2>
        {orders.length === 0 ? (
          <div className="card text-center py-8">
            <p className="text-gray-500">У вас пока нет заказов</p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div key={order.id} className="card">
                <h3 className="font-semibold mb-2">Заказ #{order.id}</h3>
                <p className="text-gray-600">Статус: <span className="font-medium">{order.status}</span></p>
                <p className="text-gray-600">Сумма: ${order.amountUsdc} USDC</p>
                {order.trackingNumber && (
                  <p className="text-gray-600 mt-2">Трекинг: {order.trackingNumber}</p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
