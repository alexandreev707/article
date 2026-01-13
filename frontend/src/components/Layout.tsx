import { ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useWallet } from '@solana/wallet-adapter-react'
import { useWalletModal } from '@solana/wallet-adapter-react-ui'

interface LayoutProps {
  children: ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const { publicKey, disconnect } = useWallet()
  const { setVisible } = useWalletModal()
  const navigate = useNavigate()

  const handleConnect = () => {
    setVisible(true)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <Link to="/" className="flex items-center space-x-2">
              <div className="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-xl">CD</span>
              </div>
              <span className="text-xl font-bold text-gray-900">CryptoDrop</span>
            </Link>

            <nav className="hidden md:flex items-center space-x-6">
              <Link to="/" className="text-gray-700 hover:text-primary-600 transition-colors">
                Каталог
              </Link>
              <Link to="/orders" className="text-gray-700 hover:text-primary-600 transition-colors">
                Мои заказы
              </Link>
              <Link to="/seller" className="text-gray-700 hover:text-primary-600 transition-colors">
                Продавцу
              </Link>
            </nav>

            <div className="flex items-center space-x-4">
              {publicKey ? (
                <>
                  <span className="text-sm text-gray-600 hidden sm:block">
                    {publicKey.toString().slice(0, 4)}...{publicKey.toString().slice(-4)}
                  </span>
                  <button
                    onClick={() => disconnect()}
                    className="btn-secondary text-sm py-2 px-4"
                  >
                    Отключить
                  </button>
                </>
              ) : (
                <button onClick={handleConnect} className="btn-primary text-sm py-2 px-4">
                  Подключить кошелек
                </button>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center text-gray-600">
            <p>© 2025 CryptoDrop. Децентрализованный маркетплейс на Solana.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
