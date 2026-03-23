package com.cryptodrop.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "oxapay")
class OxapayProperties {
    var enabled: Boolean = true
    var sandboxEnabled: Boolean = true
    var sandboxMerchant: String = "sandbox"
    var baseUrl: String = "https://api.oxapay.com"
    var merchantApiKey: String = ""
    var payoutApiKey: String = ""
    var callbackUrl: String = ""
    var returnUrl: String = ""
    var invoiceLifetimeMinutes: Int = 30
    var currency: String = "USD"
    /** Cryptocurrency symbol for seller payouts (e.g. USDT, BTC) */
    var payoutCurrency: String = "USDT"
    /** Blockchain network (required by OxaPay for USDT; default TRC20). Override via OXAPAY_PAYOUT_NETWORK. */
    var payoutNetwork: String = "TRC20"
}
