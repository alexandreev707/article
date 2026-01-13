import React, { createContext, useContext, useEffect, ReactNode } from 'react'
import { ConnectionProvider, WalletProvider as SolanaWalletProvider, useWallet as useSolanaWallet } from '@solana/wallet-adapter-react'
import { WalletAdapterNetwork } from '@solana/wallet-adapter-base'
import { PhantomWalletAdapter, SolflareWalletAdapter } from '@solana/wallet-adapter-wallets'
import { WalletModalProvider } from '@solana/wallet-adapter-react-ui'
import { clusterApiUrl } from '@solana/web3.js'
import '@solana/wallet-adapter-react-ui/styles.css'

interface WalletContextType {
  walletAddress: string | null
  isConnected: boolean
  connect: () => Promise<void>
  disconnect: () => void
}

const WalletContext = createContext<WalletContextType | undefined>(undefined)

export const useWallet = () => {
  const context = useContext(WalletContext)
  if (!context) {
    throw new Error('useWallet must be used within WalletProvider')
  }
  return context
}

const WalletContextProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { publicKey, disconnect: solanaDisconnect, connecting, connected } = useSolanaWallet()

  useEffect(() => {
    if (publicKey) {
      localStorage.setItem('walletAddress', publicKey.toString())
    } else {
      localStorage.removeItem('walletAddress')
    }
  }, [publicKey])

  const connect = async () => {
    // Connection handled by wallet adapter modal
  }

  const disconnect = async () => {
    await solanaDisconnect()
    localStorage.removeItem('walletAddress')
  }

  return (
    <WalletContext.Provider
      value={{
        walletAddress: publicKey?.toString() || null,
        isConnected: connected,
        connect,
        disconnect,
      }}
    >
      {children}
    </WalletContext.Provider>
  )
}

export const WalletProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const network = WalletAdapterNetwork.Mainnet
  const endpoint = clusterApiUrl(network)
  const wallets = [
    new PhantomWalletAdapter(),
    new SolflareWalletAdapter(),
  ]

  return (
    <ConnectionProvider endpoint={endpoint}>
      <SolanaWalletProvider wallets={wallets} autoConnect>
        <WalletModalProvider>
          <WalletContextProvider>{children}</WalletContextProvider>
        </WalletModalProvider>
      </SolanaWalletProvider>
    </ConnectionProvider>
  )
}
