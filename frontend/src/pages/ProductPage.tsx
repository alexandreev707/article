import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useWallet } from '@solana/wallet-adapter-react'
import { api } from '../services/api'
import type { Product } from '../types'

export default function ProductPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { publicKey } = useWallet()
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [shippingCountry, setShippingCountry] = useState('US')

  useEffect(() => {
    if (id) {
      loadProduct()
    }
  }, [id])

  const loadProduct = async () => {
    try {
      setLoading(true)
      const data = await api.getProduct(parseInt(id!))
      setProduct(data)
    } catch (error) {
      console.error('Failed to load product:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleBuy = async () => {
    if (!publicKey) {
      alert('Пожалуйста, подключите кошелек')
      return
    }

    if (!product) return

    try {
      // Сохраняем адрес кошелька для API
      localStorage.setItem('walletAddress', publicKey.toString())
      
      const order = await api.createOrder({
        productId: product.id,
        shippingCountry,
        shippingAddress: JSON.stringify({
          country: shippingCountry,
        }),
      })

      alert(`Заказ создан! Escrow: ${order.solanaEscrow || 'N/A'}`)
      navigate('/orders')
    } catch (error: any) {
      alert(`Ошибка: ${error.response?.data?.message || error.message}`)
    }
  }

  if (loading) {
    return <div className="text-center py-12">Загрузка...</div>
  }

  if (!product) {
    return <div className="text-center py-12">Товар не найден</div>
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Images */}
        <div>
          {product.images && product.images.length > 0 ? (
            <img
              src={product.images[0]}
              alt={product.title}
              className="w-full rounded-xl shadow-lg"
              onError={(e) => {
                (e.target as HTMLImageElement).src = 'https://via.placeholder.com/600x600?text=No+Image'
              }}
            />
          ) : (
            <div className="w-full h-96 bg-gray-200 rounded-xl flex items-center justify-center">
              <span className="text-gray-400">Нет изображения</span>
            </div>
          )}
        </div>

        {/* Product Info */}
        <div>
          <h1 className="text-3xl font-bold mb-4">{product.title}</h1>
          
          {product.description && (
            <p className="text-gray-600 mb-6">{product.description}</p>
          )}

          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <div className="flex items-baseline space-x-2 mb-2">
              <span className="text-4xl font-bold text-primary-600">
                ${product.priceUsd}
              </span>
              <span className="text-gray-500">USDC</span>
            </div>
            <p className="text-sm text-gray-600">
              Комиссия платформы: 1.5%
            </p>
          </div>

          {/* Shipping */}
          <div className="mb-6">
            <label className="block text-sm font-medium mb-2">Страна доставки</label>
            <select
              value={shippingCountry}
              onChange={(e) => setShippingCountry(e.target.value)}
              className="input"
            >
              <option value="US">США</option>
              <option value="EU">ЕС</option>
              <option value="IN">Индия</option>
            </select>
          </div>

          {/* Seller Info */}
          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <p className="text-sm text-gray-600 mb-1">Продавец</p>
            <p className="font-mono text-sm">{product.sellerWallet}</p>
          </div>

          {/* Buy Button */}
          <button
            onClick={handleBuy}
            disabled={!publicKey}
            className="btn-primary w-full py-4 text-lg"
          >
            {publicKey ? 'Купить за USDC' : 'Подключите кошелек для покупки'}
          </button>

          {product.specs && (
            <div className="mt-8">
              <h2 className="text-xl font-bold mb-4">Характеристики</h2>
              <div className="bg-gray-50 rounded-lg p-4">
                <pre className="text-sm whitespace-pre-wrap">{product.specs}</pre>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
