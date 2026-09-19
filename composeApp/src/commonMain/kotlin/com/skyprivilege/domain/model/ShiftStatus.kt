package com.skyprivilege.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ShiftStatus {
    OPEN,
    CLOSED,
    FORCE_CLOSED
}
