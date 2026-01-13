package com.cryptodrop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class CryptoDropApplication

fun main(args: Array<String>) {
    runApplication<CryptoDropApplication>(*args)
}
