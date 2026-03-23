package com.cryptodrop.service

import com.cryptodrop.config.OxapayProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
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
            throw IllegalStateException("OxaPay is disabled. Set OXAPAY_ENABLED=true to enable.")
        }
        val merchant = when {
            properties.sandboxEnabled -> properties.sandboxMerchant
            else -> properties.merchantApiKey
        }
        if (merchant.isBlank()) {
            throw IllegalStateException("OxaPay merchant key is not configured.")
        }
        if (properties.callbackUrl.isBlank()) {
            throw IllegalStateException("OxaPay callback URL is not configured.")
        }
        if (properties.returnUrl.isBlank()) {
            throw IllegalStateException("OxaPay return URL is not configured.")
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

        val body = restClient.post()
            .uri("/merchants/request")
            .body(payload)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                throw IllegalStateException("OxaPay request failed: ${response.statusCode} - $errorBody")
            }
            .body(String::class.java)
            ?: throw IllegalStateException("OxaPay returned empty response")

        val json = objectMapper.readTree(body)
        val result = json.path("result").asInt(-1)
        if (result != 100) {
            val message = json.path("message").asText("Unknown OxaPay error")
            throw IllegalStateException("OxaPay rejected invoice: $message")
        }

        val trackId = firstPresent(json, "trackId", "track_id")
        val paymentUrl = firstPresent(json, "payLink", "paymentUrl", "pay_link")
        if (trackId.isBlank() || paymentUrl.isBlank()) {
            throw IllegalStateException("OxaPay response is missing trackId/paymentUrl")
        }

        return OxapayInvoiceResponse(trackId = trackId, paymentUrl = paymentUrl)
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
}

data class OxapayInvoiceResponse(
    val trackId: String,
    val paymentUrl: String
)
