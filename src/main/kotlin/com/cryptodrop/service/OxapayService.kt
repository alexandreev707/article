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
import java.util.Locale

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
            throw OxapayPayoutException(
                "OXAPAY_DISABLED",
                "OxaPay is disabled. Set OXAPAY_ENABLED=true to enable payouts."
            )
        }
        val payoutKey = properties.payoutApiKey.trim()
        if (payoutKey.isBlank()) {
            throw OxapayPayoutException(
                "PAYOUT_KEY_MISSING",
                "OxaPay payout API key is not configured (OXAPAY_PAYOUT_API_KEY)."
            )
        }
        val currency = properties.payoutCurrency.trim().ifBlank { "USDT" }
        val trimmedAddress = recipientAddress.trim()
        val network = resolvePayoutNetwork(trimmedAddress)
        val payload = mutableMapOf<String, Any>(
            "address" to trimmedAddress,
            "currency" to currency,
            "amount" to amount,
            "network" to network
        )
        if (description.isNotBlank()) {
            payload["description"] = description
        }

        val body = try {
            restClient.post()
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
        } catch (e: OxapayPayoutException) {
            throw e
        } catch (e: RestClientException) {
            throw OxapayPayoutException(
                "OXAPAY_PAYOUT_ERROR",
                "Could not reach OxaPay (${properties.baseUrl}): ${e.message ?: e.javaClass.simpleName}"
            )
        } ?: throw OxapayPayoutException(
            "OXAPAY_PAYOUT_ERROR",
            "OxaPay payout returned an empty response."
        )

        val json = try {
            objectMapper.readTree(body)
        } catch (e: Exception) {
            throw OxapayPayoutException(
                "OXAPAY_PAYOUT_ERROR",
                "OxaPay payout response is not valid JSON: ${body.take(400)}"
            )
        }
        val data = json.path("data")
        var trackId = data.path("track_id").asText("").trim()
        if (trackId.isBlank()) trackId = data.path("trackId").asText("").trim()
        if (trackId.isBlank()) {
            trackId = firstPresent(json, "track_id", "trackId").trim()
        }
        if (trackId.isBlank()) {
            throw OxapayPayoutException(
                "OXAPAY_PAYOUT_ERROR",
                "OxaPay payout succeeded but no track id was found in the response. Raw body: ${body.take(500)}"
            )
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

    /** OxaPay network id for BSC / BEP-20 (from GET /v1/common/networks). */
    private val binanceSmartChainNetworkName = "Binance Smart Chain"

    /**
     * Payouts are restricted to BSC (BEP-20): always send OxaPay `network` = [binanceSmartChainNetworkName],
     * and require a valid EVM `0x` address (same format as on BSC).
     */
    private fun resolvePayoutNetwork(recipientAddress: String): String {
        validateBinanceSmartChainRecipient(recipientAddress)
        return normalizeToBinanceSmartChainOnly(properties.payoutNetwork)
    }

    private fun normalizeToBinanceSmartChainOnly(raw: String): String {
        val r = raw.trim()
        if (r.isBlank()) return binanceSmartChainNetworkName
        val compact = r.uppercase(Locale.ROOT).replace("-", "").replace(" ", "")
        val isBsc = compact == "BINANCESMARTCHAIN" ||
            compact in setOf("BEP20", "BSC", "BINANCE", "BNB")
        if (isBsc) return binanceSmartChainNetworkName
        throw OxapayPayoutException(
            "UNSUPPORTED_PAYOUT_NETWORK",
            "Payouts are only on BSC (BEP-20). Set OXAPAY_PAYOUT_NETWORK to Binance Smart Chain, BEP20, or BSC (current: $r)."
        )
    }

    private fun validateBinanceSmartChainRecipient(address: String) {
        val a = address.trim()
        val looksTron = a.startsWith("T") && a.length in 33..36 && a.all { it.isLetterOrDigit() }
        if (looksTron) {
            throw OxapayPayoutException(
                "INVALID_PAYOUT_ADDRESS",
                "Payouts use BSC (BEP-20) only. Use a Binance Smart Chain wallet address (0x…), not a Tron (T…) address."
            )
        }
        if (!isValidHexEvmAddress(a)) {
            throw OxapayPayoutException(
                "INVALID_PAYOUT_ADDRESS",
                "Payouts use BSC (BEP-20) only. Enter a valid EVM address: 0x followed by 40 hexadecimal characters."
            )
        }
    }

    private fun isValidHexEvmAddress(a: String): Boolean {
        if (a.length != 42 || !a.startsWith("0x", ignoreCase = true)) return false
        val hex = a.substring(2)
        return hex.length == 40 && hex.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Maps OxaPay JSON error to a client-friendly [OxapayPayoutException] (HTTP 400), not a servlet 500.
     */
    private fun mapPayoutError(errorBody: String, currency: String, network: String): OxapayPayoutException {
        val tree = runCatching { objectMapper.readTree(errorBody) }.getOrNull()
        val errorNode = tree?.path("error")
        val key = when {
            errorNode?.isObject == true -> errorNode.path("key").asText("").trim()
            else -> ""
        }.ifBlank { errorNode?.asText()?.trim().orEmpty() }

        val apiMsg = when {
            errorNode?.isObject == true -> errorNode.path("message").asText("").trim()
            else -> ""
        }.ifBlank { tree?.path("message")?.asText()?.trim().orEmpty() }
            .ifBlank { tree?.path("msg")?.asText()?.trim().orEmpty() }
            .ifBlank { tree?.path("detail")?.asText()?.trim().orEmpty() }
            .ifBlank { tree?.path("description")?.asText()?.trim().orEmpty() }
            .ifBlank {
                val errs = tree?.path("errors")
                if (errs != null && errs.isArray) {
                    errs.joinToString("; ") { el ->
                        el.path("message").asText("").trim()
                            .ifBlank { el.asText("").trim() }
                    }
                } else ""
            }

        val human = apiMsg.ifBlank { errorBody.trim().ifBlank { "Unknown OxaPay payout error" } }

        return when (key) {
            "invalid_address" -> OxapayPayoutException(
                "INVALID_PAYOUT_ADDRESS",
                buildString {
                    append("OxaPay rejected the wallet address: $human ")
                    append("(network: $network, currency: $currency). ")
                    append("Payouts are on BSC (BEP-20) only — use a valid BSC USDT address (EVM 0x…).")
                }
            )
            else -> OxapayPayoutException(
                "OXAPAY_PAYOUT_ERROR",
                "OxaPay payout failed: $human"
            )
        }
    }
}

data class OxapayInvoiceResponse(
    val trackId: String,
    val paymentUrl: String
)
