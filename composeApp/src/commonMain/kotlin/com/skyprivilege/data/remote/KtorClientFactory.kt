package com.skyprivilege.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientFactory {
    fun createHttpClient(
        baseUrl: String = "http://10.0.2.2:3001",
        deviceId: String = "DEV-TABLET-001",
        outletId: Long = 1L,
        authTokenProvider: (() -> String?)? = null
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                header("X-Device-Id", deviceId)
                header("X-Device-Token", deviceId)
                header("X-Outlet-Id", outletId.toString())
                authTokenProvider?.invoke()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }
}
