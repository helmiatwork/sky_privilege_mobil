package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentDto
import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentResponse
import com.skyprivilege.data.remote.dto.GuidelinesResponse
import com.skyprivilege.domain.model.TicketGuideline
import com.skyprivilege.domain.repository.GuidelineRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class GuidelineRepositoryImpl(
    private val httpClient: HttpClient
) : GuidelineRepository {

    private var cachedETag: String? = null
    private var cachedGuidelines: List<TicketGuideline>? = null

    override suspend fun getGuidelines(): Result<List<TicketGuideline>> {
        return runCatching {
            val response: HttpResponse = httpClient.get("/api/v1/guidelines") {
                cachedETag?.let { header("If-None-Match", it) }
            }
            if (response.status == HttpStatusCode.NotModified) {
                return Result.success(
                    cachedGuidelines ?: throw IllegalStateException("304 Not Modified received without cached guideline bundle")
                )
            }
            val body = response.body<GuidelinesResponse>()
            cachedETag = response.headers["ETag"]
            val guidelines = body.data.ifEmpty { body.guidelines }
            cachedGuidelines = guidelines
            guidelines
        }
    }

    override suspend fun submitAcknowledgment(
        request: AuthenticityAcknowledgmentDto
    ): Result<AuthenticityAcknowledgmentResponse> {
        return runCatching {
            httpClient.post("/api/v1/acknowledgments") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AuthenticityAcknowledgmentResponse>()
        }
    }
}
