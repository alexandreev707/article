package com.cryptodrop.web.error

/**
 * Thrown when seller requests payout but has no wallet address in profile.
 * Handled as HTTP 400 with [error]=[WALLET_REQUIRED] for the UI.
 */
class WalletRequiredException(message: String) : RuntimeException(message)
