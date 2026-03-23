package com.cryptodrop.web.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestApiExceptionHandler {

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
}
