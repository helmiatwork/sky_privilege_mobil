package com.skyprivilege.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TicketGuideline(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    @SerialName("display_order") val displayOrder: Int = 0,
    val active: Boolean = true
)
