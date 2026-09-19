package com.skyprivilege.domain.repository

import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentDto
import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentResponse
import com.skyprivilege.domain.model.TicketGuideline

interface GuidelineRepository {
    suspend fun getGuidelines(): Result<List<TicketGuideline>>
    suspend fun submitAcknowledgment(request: AuthenticityAcknowledgmentDto): Result<AuthenticityAcknowledgmentResponse>
}
