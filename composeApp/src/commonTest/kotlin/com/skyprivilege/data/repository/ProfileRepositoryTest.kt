package com.skyprivilege.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGetProfileSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/profile", request.url.encodedPath)
            assertEquals("2", request.url.parameters["cashier_id"])
            respond(
                content = """
                    {
                        "success": true,
                        "cashier": {
                            "id": 2,
                            "name": "Andhika Putra",
                            "employee_id": "CSH-002",
                            "role": "cashier",
                            "outlet_id": 2,
                            "outlet_name": "Sky Lounge Terminal 3 CGK",
                            "active": true
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = ProfileRepositoryImpl(client)
        val result = repository.getProfile(cashierId = 2L)

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()
        assertNotNull(profile)
        assertEquals(2L, profile.id)
        assertEquals("Andhika Putra", profile.name)
        assertEquals("CSH-002", profile.employeeId)
        assertEquals("Sky Lounge Terminal 3 CGK", profile.outletName)
    }

    @Test
    fun testUpdateProfileSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/profile", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "message": "Profil berhasil diperbarui",
                        "cashier": {
                            "id": 2,
                            "name": "Andhika Putra Pratama",
                            "employee_id": "CSH-002",
                            "role": "cashier",
                            "outlet_id": 2,
                            "outlet_name": "Sky Lounge Terminal 3 CGK",
                            "active": true
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = ProfileRepositoryImpl(client)
        val result = repository.updateProfile(cashierId = 2L, name = "Andhika Putra Pratama")

        assertTrue(result.isSuccess)
        val msg = result.getOrNull()
        assertEquals("Profil berhasil diperbarui", msg)
    }

    @Test
    fun testChangePasswordSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/profile/change_password", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "message": "Password/PIN kasir berhasil diubah"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = ProfileRepositoryImpl(client)
        val result = repository.changePassword(
            cashierId = 2L,
            oldPin = "123456",
            newPin = "654321",
            confirmation = "654321"
        )

        assertTrue(result.isSuccess)
        val msg = result.getOrNull()
        assertEquals("Password/PIN kasir berhasil diubah", msg)
    }
}
