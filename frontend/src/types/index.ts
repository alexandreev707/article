export interface Product {
  id: number
  title: string
  description?: string
  sellerWallet: string
  priceUsd: number
  images: string[]
  specs?: string
  shippingProfiles?: string
  categoryId: number
  status: string
  createdAt: string
}

export interface Category {
  id: number
  name: string
  slug: string
  parentId?: number
  iconUrl?: string
}

export interface Order {
  id: number
  productId: number
  buyerWallet: string
  sellerWallet: string
  amountUsdc: string
  shippingCountry?: string
  shippingAddress?: string
  shippingCostUsd?: string
  solanaEscrow?: string
  solanaTxId?: string
  status: string
  trackingNumber?: string
  createdAt: string
  shippedAt?: string
  deliveredAt?: string
  completedAt?: string
}
