import { useEffect, useState } from 'react'
import { useWallet } from '@solana/wallet-adapter-react'
import { api } from '../services/api'
import type { Order } from '../types'

interface OrdersPageResponse {
  orders: Order[]
  total: number
  page: number
  limit: number
}

export default function OrdersPage() {
  const { publicKey } = useWallet()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (publicKey) {
      loadOrders()
    }
  }, [publicKey])

  const loadOrders = async () => {
    try {
      setLoading(true)
      if (publicKey) {
        localStorage.setItem('walletAddress', publicKey.toString())
      }
      const data: OrdersPageResponse = await api.getMyOrders()
      setOrders(data.orders)
    } catch (error) {
      console.error('Failed to load orders:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmDelivery = async (orderId: number) => {
    try {
      await api.confirmDelivery(orderId)
      alert('Доставка подтверждена! Выплата продавцу выполнена.')
      loadOrders()
    } catch (error: any) {
      alert(`Ошибка: ${error.response?.data?.message || error.message}`)
    }
  }

  if (!publicKey) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">Подключите кошелек для просмотра заказов</p>
      </div>
    )
  }

  if (loading) {
    return <div className="text-center py-12">Загрузка...</div>
  }

  if (orders.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">У вас пока нет заказов</p>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Мои заказы</h1>
      <div className="space-y-4">
        {orders.map((order) => (
          <div key={order.id} className="card">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-semibold text-lg mb-2">Заказ #{order.id}</h3>
                <p className="text-gray-600">Статус: <span className="font-medium">{order.status}</span></p>
                <p className="text-gray-600">Сумма: ${order.amountUsdc} USDC</p>
                {order.trackingNumber && (
                  <p className="text-gray-600 mt-2">
                    Трекинг: <span className="font-mono">{order.trackingNumber}</span>
                  </p>
                )}
                {order.solanaEscrow && (
                  <p className="text-xs text-gray-400 mt-1">
                    Escrow: {order.solanaEscrow.slice(0, 20)}...
                  </p>
                )}
              </div>
              <div>
                {order.status === 'delivered' && (
                  <button
                    onClick={() => handleConfirmDelivery(order.id)}
                    className="btn-primary"
                  >
                    Подтвердить доставку
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
