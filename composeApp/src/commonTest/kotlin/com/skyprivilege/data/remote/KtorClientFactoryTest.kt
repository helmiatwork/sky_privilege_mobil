package com.skyprivilege.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KtorClientFactoryTest {

    @Test
    fun testHeadersIncludeDeviceAndOmitDeviceToken() = runTest {
        var iface = "wifi"
        val mockEngine = MockEngine { request ->
            assertEquals("DEV-TEST-007", request.headers["X-Device-Id"])
            assertEquals("99", request.headers["X-Outlet-Id"])
            assertEquals("wifi", request.headers["X-Network-Iface"])
            assertNull(request.headers["X-Device-Token"], "X-Device-Token must be purged")
            assertNotNull(request.headers["X-Timestamp"], "X-Timestamp must be attached")
            assertNotNull(request.headers["X-Nonce"], "X-Nonce must be attached")
            assertNull(request.headers["X-Device-Signature"])

            respond("OK", HttpStatusCode.OK)
        }

        val client = KtorClientFactory.createHttpClient(
            baseUrl = "http://localhost:3000",
            deviceId = "DEV-TEST-007",
            outletId = 99L,
            networkIfaceProvider = { iface },
            engine = mockEngine
        )

        client.get("/test")
    }

    @Test
    fun testHeadersDynamicNetworkIfaceAndSignatureWhenProvided() = runTest {
        var ifaceState = "cellular"
        val mockEngine = MockEngine { request ->
            assertEquals("cellular", request.headers["X-Network-Iface"])
            assertNotNull(request.headers["X-Device-Signature"])
            assertTrue(request.headers["X-Device-Signature"]!!.isNotEmpty())
            respond("OK", HttpStatusCode.OK)
        }

        val client = KtorClientFactory.createHttpClient(
            baseUrl = "http://localhost:3000",
            deviceId = "DEV-TEST-007",
            outletId = 99L,
            networkIfaceProvider = { ifaceState },
            signerProvider = { data -> "mock_signature_bytes".encodeToByteArray() },
            engine = mockEngine
        )

        client.get("/test")
    }
}
