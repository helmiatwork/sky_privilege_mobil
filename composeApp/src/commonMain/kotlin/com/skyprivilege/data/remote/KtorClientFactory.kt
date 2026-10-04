package com.skyprivilege.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.getTimeMillis
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.random.Random

object KtorClientFactory {
    fun createHttpClient(
        baseUrl: String = "http://10.0.2.2:3001",
        deviceId: String = "DEV-TABLET-001",
        outletId: Long = 2L,
        authTokenProvider: (() -> String?)? = null,
        networkIfaceProvider: () -> String = { "wifi" },
        signerProvider: ((ByteArray) -> ByteArray)? = null,
        engine: HttpClientEngine? = null,
        timeoutMillis: Long = 30_000L
    ): HttpClient {
        val config: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
            install(HttpTimeout) {
                requestTimeoutMillis = timeoutMillis
                connectTimeoutMillis = if (timeoutMillis < 10_000L) timeoutMillis else 10_000L
                socketTimeoutMillis = timeoutMillis
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                header("X-Device-Id", deviceId)
                header("X-Outlet-Id", outletId.toString())
                header("X-Network-Iface", networkIfaceProvider())
                authTokenProvider?.invoke()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }

            install(createClientPlugin("DeviceSignature") {
                onRequest { request, _ ->
                    val timestamp = getTimeMillis().toString()
                    val nonceBytes = Random.nextBytes(16)
                    val nonce = nonceBytes.encodeBase64()
                    request.header("X-Timestamp", timestamp)
                    request.header("X-Nonce", nonce)
                    if (signerProvider != null) {
                        val canonical = "$deviceId\n$timestamp\n$nonce"
                        val signature = withContext(Dispatchers.Default) {
                            signerProvider(canonical.encodeToByteArray())
                        }
                        request.header("X-Device-Signature", signature.encodeBase64())
                    }
                }
            })
        }

        return if (engine != null) HttpClient(engine, config) else HttpClient(config)
    }

    fun createRedemptionHttpClient(
        baseUrl: String = "http://10.0.2.2:3001",
        deviceId: String = "DEV-TABLET-001",
        outletId: Long = 2L,
        authTokenProvider: (() -> String?)? = null,
        networkIfaceProvider: () -> String = { "wifi" },
        signerProvider: ((ByteArray) -> ByteArray)? = null,
        engine: HttpClientEngine? = null
    ): HttpClient = createHttpClient(
        baseUrl = baseUrl,
        deviceId = deviceId,
        outletId = outletId,
        authTokenProvider = authTokenProvider,
        networkIfaceProvider = networkIfaceProvider,
        signerProvider = signerProvider,
        engine = engine,
        timeoutMillis = 8_000L
    )
}
