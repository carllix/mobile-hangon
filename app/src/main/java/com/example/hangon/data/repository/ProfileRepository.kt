package com.example.hangon.data.repository

import com.example.hangon.data.model.UserProfile
import com.example.hangon.data.remote.HangOnApi
import com.example.hangon.data.remote.UserApi
import com.example.hangon.data.remote.dto.MeResponseDto
import com.example.hangon.data.remote.dto.UpdateDisplayNameRequestDto
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

interface ProfileRepository {
    suspend fun getProfile(): ApiResult<UserProfile>
    suspend fun updateDisplayName(displayName: String): ApiResult<UserProfile>
}

class RetrofitProfileRepository(
    private val api: UserApi = HangOnApi.userApi,
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ProfileRepository {

    private suspend fun bearer(): String {
        val token = authRepository.getIdToken()
            ?: throw IllegalStateException("Invalid session, please sign in again.")
        return "Bearer $token"
    }

    private fun MeResponseDto.toDomain() = UserProfile(
        id = id,
        displayName = displayName ?: "",
        email = authRepository.email
    )

    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val fallback = "An error occurred (${e.code()})"
            ApiResult.Failure(parseApiErrorMessage(e.response()?.errorBody()?.string(), fallback))
        } catch (e: IOException) {
            ApiResult.Failure("Unable to connect to the server. Please check your connection.")
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "An unexpected error occurred.")
        }
    }

    override suspend fun getProfile(): ApiResult<UserProfile> = safeCall {
        api.getMe(bearer()).toDomain()
    }

    override suspend fun updateDisplayName(displayName: String): ApiResult<UserProfile> = safeCall {
        val updated = api.updateMe(bearer(), UpdateDisplayNameRequestDto(displayName)).toDomain()
        authRepository.updateDisplayName(displayName)
        updated
    }
}
