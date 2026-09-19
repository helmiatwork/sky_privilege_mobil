package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.CashierProfileDto
import com.skyprivilege.data.remote.dto.CashierProfileResponse
import com.skyprivilege.data.remote.dto.ChangePasswordRequest
import com.skyprivilege.data.remote.dto.ProfileActionResponse
import com.skyprivilege.data.remote.dto.UpdateProfileRequest
import com.skyprivilege.domain.repository.ProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProfileRepositoryImpl(
    private val httpClient: HttpClient
) : ProfileRepository {

    override suspend fun getProfile(cashierId: Long): Result<CashierProfileDto> {
        return runCatching {
            val response = httpClient.get("/api/v1/profile") {
                parameter("cashier_id", cashierId)
            }.body<CashierProfileResponse>()

            if (response.success && response.cashier != null) {
                response.cashier
            } else {
                throw IllegalStateException(response.error ?: "Gagal memuat profil kasir")
            }
        }
    }

    override suspend fun updateProfile(cashierId: Long, name: String): Result<String> {
        return runCatching {
            val response = httpClient.patch("/api/v1/profile") {
                contentType(ContentType.Application.Json)
                setBody(UpdateProfileRequest(cashierId = cashierId, name = name))
            }.body<ProfileActionResponse>()

            if (response.success) {
                response.message ?: "Profil berhasil diperbarui"
            } else {
                throw IllegalStateException(response.error ?: "Gagal memperbarui profil")
            }
        }
    }

    override suspend fun changePassword(
        cashierId: Long,
        oldPin: String,
        newPin: String,
        confirmation: String
    ): Result<String> {
        return runCatching {
            val response = httpClient.post("/api/v1/profile/change_password") {
                contentType(ContentType.Application.Json)
                setBody(
                    ChangePasswordRequest(
                        cashierId = cashierId,
                        currentPin = oldPin,
                        newPin = newPin,
                        newPinConfirmation = confirmation
                    )
                )
            }.body<ProfileActionResponse>()

            if (response.success) {
                response.message ?: "Password/PIN kasir berhasil diubah"
            } else {
                throw IllegalStateException(response.error ?: "Gagal mengubah password")
            }
        }
    }
}
