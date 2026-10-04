package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentDto
import com.skyprivilege.domain.model.TicketGuideline
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
import kotlin.test.assertTrue

class GuidelineRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGetGuidelinesSuccess200CachesETag() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/guidelines", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "title": "Cek Keaslian Kertas",
                                "description": "Gunakan kertas thermal resmi",
                                "category": "physical",
                                "active": true
                            }
                        ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    "ETag" to listOf("W/\"guidelines-hash-123\"")
                )
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = GuidelineRepositoryImpl(client)
        val result = repository.getGuidelines()
        assertTrue(result.isSuccess)
        val guidelines = result.getOrNull()
        assertEquals(1, guidelines?.size)
        assertEquals("Cek Keaslian Kertas", guidelines?.first()?.title)
    }

    @Test
    fun testGetGuidelines304ReturnsCachedGuidelines() = runTest {
        var callCount = 0
        val mockEngine = MockEngine { request ->
            callCount++
            if (callCount == 1) {
                respond(
                    content = """
                        {
                            "success": true,
                            "data": [
                                {
                                    "id": 1,
                                    "title": "Cek Stempel",
                                    "description": "Periksa stempel basah",
                                    "category": "stamp",
                                    "active": true
                                }
                            ]
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        "ETag" to listOf("etag-1")
                    )
                )
            } else {
                assertEquals("etag-1", request.headers["If-None-Match"])
                respond(
                    content = "",
                    status = HttpStatusCode.NotModified
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = GuidelineRepositoryImpl(client)
        val firstResult = repository.getGuidelines()
        assertTrue(firstResult.isSuccess)
        assertEquals("Cek Stempel", firstResult.getOrNull()?.first()?.title)

        val secondResult = repository.getGuidelines()
        assertTrue(secondResult.isSuccess)
        assertEquals("Cek Stempel", secondResult.getOrNull()?.first()?.title)
    }

    @Test
    fun testGetGuidelines304WithoutCacheThrowsIllegalStateException() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.NotModified
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = GuidelineRepositoryImpl(client)
        val result = repository.getGuidelines()
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalStateException)
        assertEquals("304 Not Modified received without cached guideline bundle", exception.message)
    }

    @Test
    fun testSubmitAcknowledgmentSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/acknowledgments", request.url.encodedPath)
            respond(
                content = """
                    {
                        "status": "ok",
                        "success": true,
                        "acknowledgment_id": 123
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = GuidelineRepositoryImpl(client)
        val result = repository.submitAcknowledgment(
            AuthenticityAcknowledgmentDto(
                cashierId = 1L,
                outletId = 2L
            )
        )
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull()?.success)
        assertEquals(123L, result.getOrNull()?.acknowledgmentId)
    }
}
