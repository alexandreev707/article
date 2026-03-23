package com.cryptodrop.web.error

/**
 * OxaPay payout API rejected the request (e.g. invalid address for network).
 * [clientErrorCode] is sent to the UI: INVALID_PAYOUT_ADDRESS, OXAPAY_PAYOUT_ERROR.
 */
class OxapayPayoutException(
    val clientErrorCode: String,
    override val message: String
) : RuntimeException(message)
