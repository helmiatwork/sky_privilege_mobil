package com.skyprivilege.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CashierProfileDto(
    val id: Long,
    val name: String,
    @SerialName("employee_id") val employeeId: String,
    val role: String,
    @SerialName("outlet_id") val outletId: Long? = null,
    @SerialName("outlet_name") val outletName: String? = null,
    val active: Boolean = true
)

@Serializable
data class CashierProfileResponse(
    val success: Boolean,
    val cashier: CashierProfileDto? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("cashier_id") val cashierId: Long,
    val name: String
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("current_pin") val currentPin: String,
    @SerialName("new_pin") val newPin: String,
    @SerialName("new_pin_confirmation") val newPinConfirmation: String
)

@Serializable
data class ProfileActionResponse(
    val success: Boolean,
    val message: String? = null,
    val cashier: CashierProfileDto? = null,
    val error: String? = null
)
