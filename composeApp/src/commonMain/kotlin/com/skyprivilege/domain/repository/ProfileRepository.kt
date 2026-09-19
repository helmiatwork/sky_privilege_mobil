package com.skyprivilege.domain.repository

import com.skyprivilege.data.remote.dto.CashierProfileDto

interface ProfileRepository {
    suspend fun getProfile(cashierId: Long): Result<CashierProfileDto>
    suspend fun updateProfile(cashierId: Long, name: String): Result<String>
    suspend fun changePassword(cashierId: Long, oldPin: String, newPin: String, confirmation: String): Result<String>
}
