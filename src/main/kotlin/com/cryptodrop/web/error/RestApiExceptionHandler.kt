package com.cryptodrop.web.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Only applies to @RestController so MVC pages (e.g. Thymeleaf) keep default error handling. */
@RestControllerAdvice(annotations = [RestController::class])
class RestApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(OxapayInvoiceException::class)
    fun oxapayInvoice(ex: OxapayInvoiceException): ResponseEntity<Map<String, String>> {
        log.warn("OxaPay invoice failed: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
            mapOf(
                "error" to "OXAPAY_INVOICE_FAILED",
                "message" to (ex.message ?: "Payment link could not be created")
            )
        )
    }

    @ExceptionHandler(WalletRequiredException::class)
    fun walletRequired(ex: WalletRequiredException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "WALLET_REQUIRED",
                "message" to (ex.message ?: "Wallet address is required")
            )
        )

    @ExceptionHandler(OxapayPayoutException::class)
    fun oxapayPayout(ex: OxapayPayoutException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to ex.clientErrorCode,
                "message" to (ex.message ?: "OxaPay payout error")
            )
        )

    /** Business-rule failures from services (e.g. order cannot be cancelled) — return a clear message to the client. */
    @ExceptionHandler(IllegalStateException::class)
    fun illegalState(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        log.warn("IllegalState: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "BAD_REQUEST",
                "message" to (ex.message ?: "Request cannot be completed")
            )
        )
    }
}
