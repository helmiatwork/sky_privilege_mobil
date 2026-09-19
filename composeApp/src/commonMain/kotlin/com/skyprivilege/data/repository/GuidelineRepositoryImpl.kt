package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentDto
import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentResponse
import com.skyprivilege.data.remote.dto.GuidelinesResponse
import com.skyprivilege.domain.model.TicketGuideline
import com.skyprivilege.domain.repository.GuidelineRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class GuidelineRepositoryImpl(
    private val httpClient: HttpClient
) : GuidelineRepository {

    override suspend fun getGuidelines(): Result<List<TicketGuideline>> {
        return runCatching {
            val response = httpClient.get("/api/v1/guidelines").body<GuidelinesResponse>()
            response.data.ifEmpty { response.guidelines }
        }
    }

    override suspend fun submitAcknowledgment(
        request: AuthenticityAcknowledgmentDto
    ): Result<AuthenticityAcknowledgmentResponse> {
        return runCatching {
            httpClient.post("/api/v1/acknowledgments") {
                setBody(request)
            }.body<AuthenticityAcknowledgmentResponse>()
        }
    }
}
