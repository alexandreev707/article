package com.cryptodrop.service

import com.cryptodrop.config.OxapayProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import com.cryptodrop.web.error.OxapayInvoiceException
import com.cryptodrop.web.error.OxapayPayoutException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.math.BigDecimal

@Service
class OxapayService(
    private val properties: OxapayProperties,
    private val objectMapper: ObjectMapper
) {
    private val restClient = RestClient.builder()
        .baseUrl(properties.baseUrl.trimEnd('/'))
        .build()

    fun createInvoice(orderId: String, amount: BigDecimal): OxapayInvoiceResponse {
        if (!properties.enabled) {
            throw OxapayInvoiceException("OxaPay is disabled (OXAPAY_ENABLED=false).")
        }
        val merchant = when {
            properties.sandboxEnabled -> properties.sandboxMerchant
            else -> properties.merchantApiKey
        }
        if (merchant.isBlank()) {
            throw OxapayInvoiceException(
                "OXAPAY_MERCHANT_API_KEY is not set. Add your OxaPay merchant key to the server environment (e.g. Render)."
            )
        }
        if (properties.callbackUrl.isBlank()) {
            throw OxapayInvoiceException("OXAPAY_CALLBACK_URL is not set.")
        }
        if (properties.returnUrl.isBlank()) {
            throw OxapayInvoiceException("OXAPAY_RETURN_URL is not set.")
        }

        val payload = mapOf(
            "merchant" to merchant,
            "amount" to amount,
            "currency" to properties.currency,
            "lifeTime" to properties.invoiceLifetimeMinutes,
            "orderId" to orderId,
            "callbackUrl" to properties.callbackUrl,
            "returnUrl" to properties.returnUrl,
            "sandbox" to properties.sandboxEnabled
        )

        val body = try {
            restClient.post()
                .uri("/merchants/request")
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError) { _, response ->
                    val errorBody = String(response.body.readAllBytes())
                    val parsed = runCatching { objectMapper.readTree(errorBody) }.getOrNull()
                    val apiMsg = parsed?.path("message")?.asText()?.trim().orEmpty()
                        .ifBlank { errorBody.take(500) }
                    throw OxapayInvoiceException("OxaPay rejected the invoice request: $apiMsg")
                }
                .body(String::class.java)
        } catch (e: OxapayInvoiceException) {
            throw e
        } catch (e: RestClientException) {
            throw OxapayInvoiceException(
                "Could not reach OxaPay (${properties.baseUrl}). Check network connectivity and API availability.",
                e
            )
        } ?: throw OxapayInvoiceException("OxaPay returned an empty response.")

        val json = objectMapper.readTree(body)
        val result = json.path("result").asInt(-1)
        if (result != 100) {
            val message = json.path("message").asText("Unknown OxaPay error")
            val hint = when {
                message.contains("Invalid merchant", ignoreCase = true) ||
                    message.contains("merchant API key", ignoreCase = true) -> {
                    " Set OXAPAY_MERCHANT_API_KEY to the Merchant key from the OxaPay dashboard (payment acceptance), not the Payout API key. " +
                        "If sandbox is enabled (OXAPAY_SANDBOX_ENABLED=true), use the sandbox merchant key or OXAPAY_SANDBOX_MERCHANT."
                }
                else -> ""
            }
            throw OxapayInvoiceException("OxaPay: $message.$hint")
        }

        val trackId = firstPresent(json, "trackId", "track_id")
        val paymentUrl = firstPresent(json, "payLink", "paymentUrl", "pay_link")
        if (trackId.isBlank() || paymentUrl.isBlank()) {
            throw OxapayInvoiceException("OxaPay response has no payment link. Check the API response format.")
        }

        return OxapayInvoiceResponse(trackId = trackId, paymentUrl = paymentUrl)
    }

    /**
     * Sends crypto to [recipientAddress] via OxaPay Payout API (POST /v1/payout).
     * [amount] is sent as the payout amount in the configured [OxapayProperties.payoutCurrency]
     * (often USDT 1:1 with order total in USD for stablecoins).
     */
    fun createPayout(recipientAddress: String, amount: BigDecimal, description: String): String {
        if (!properties.enabled) {
            throw IllegalStateException("OxaPay is disabled. Set OXAPAY_ENABLED=true to enable.")
        }
        val payoutKey = properties.payoutApiKey.trim()
        if (payoutKey.isBlank()) {
            throw IllegalStateException("OxaPay payout API key is not configured (OXAPAY_PAYOUT_API_KEY).")
        }
        val currency = properties.payoutCurrency.trim().ifBlank { "USDT" }
        // OxaPay requires "network" for payouts (e.g. USDT on TRC20 vs ERC20).
        val network = properties.payoutNetwork.trim().ifBlank { "TRC20" }
        val payload = mutableMapOf<String, Any>(
            "address" to recipientAddress.trim(),
            "currency" to currency,
            "amount" to amount,
            "network" to network
        )
        if (description.isNotBlank()) {
            payload["description"] = description
        }

        val body = restClient.post()
            .uri("/v1/payout")
            .header("payout_api_key", payoutKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                throw mapPayoutError(errorBody, currency, network)
            }
            .body(String::class.java)
            ?: throw IllegalStateException("OxaPay payout returned empty response")

        val json = objectMapper.readTree(body)
        val data = json.path("data")
        var trackId = data.path("track_id").asText("").trim()
        if (trackId.isBlank()) trackId = data.path("trackId").asText("").trim()
        if (trackId.isBlank()) {
            trackId = firstPresent(json, "track_id", "trackId").trim()
        }
        if (trackId.isBlank()) {
            throw IllegalStateException("OxaPay payout response is missing track_id: $body")
        }
        return trackId
    }

    fun isPaidCallback(payload: Map<String, Any?>): Boolean {
        val root: JsonNode = objectMapper.valueToTree(payload)
        val status = firstPresent(root, "status", "paymentStatus", "payment_status").lowercase()
        val result = root.path("result").asInt(-1)
        return status in setOf("paid", "completed", "confirming") || result == 100
    }

    fun extractTrackId(payload: Map<String, Any?>): String? {
        val root: JsonNode = objectMapper.valueToTree(payload)
        val track = firstPresent(root, "trackId", "track_id", "txid", "transaction_id").trim()
        return track.ifBlank { null }
    }

    private fun firstPresent(node: JsonNode, vararg fields: String): String {
        for (field in fields) {
            val value = node.path(field).asText("")
            if (value.isNotBlank()) return value
        }
        return ""
    }

    /**
     * Maps OxaPay JSON error to a client-friendly [OxapayPayoutException] (HTTP 400), not a servlet 500.
     */
    private fun mapPayoutError(errorBody: String, currency: String, network: String): OxapayPayoutException {
        val tree = runCatching { objectMapper.readTree(errorBody) }.getOrNull()
        val key = tree?.path("error")?.path("key")?.asText()?.trim().orEmpty()
        val apiMsg = tree?.path("error")?.path("message")?.asText()?.trim().orEmpty()
            .ifBlank { tree?.path("message")?.asText()?.trim().orEmpty() }

        return when (key) {
            "invalid_address" -> OxapayPayoutException(
                "INVALID_PAYOUT_ADDRESS",
                buildString {
                    append("OxaPay rejected the wallet address. ")
                    append("Current network: $network, currency: $currency. ")
                    append("For USDT on TRC20 use a Tron address (starts with T, Base58). ")
                    append("For an Ethereum wallet (0x…), set OXAPAY_PAYOUT_NETWORK=ERC20 (or BEP20 for BSC) in settings. ")
                    append("Update the address in your profile.")
                }
            )
            else -> OxapayPayoutException(
                "OXAPAY_PAYOUT_ERROR",
                apiMsg.ifBlank { "OxaPay: $errorBody" }
            )
        }
    }
}

data class OxapayInvoiceResponse(
    val trackId: String,
    val paymentUrl: String
)
