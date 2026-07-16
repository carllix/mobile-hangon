package com.example.hangon.data.repository

import com.example.hangon.data.model.FamilyDetail
import com.example.hangon.data.model.FamilySummary
import com.example.hangon.data.remote.FamilyApi
import com.example.hangon.data.remote.HangOnApi
import com.example.hangon.data.remote.UserApi
import com.example.hangon.data.remote.dto.FamilyCreateRequestDto
import com.example.hangon.data.remote.dto.FamilyJoinRequestDto
import com.example.hangon.data.remote.dto.toDomain
import retrofit2.HttpException
import java.io.IOException

interface FamilyRepository {
    suspend fun listMyFamilies(): ApiResult<List<FamilySummary>>
    suspend fun createFamily(name: String): ApiResult<Unit>
    suspend fun joinFamily(inviteCode: String): ApiResult<Unit>
    suspend fun getFamilyDetail(familyId: String): ApiResult<FamilyDetail>
    suspend fun getSecret(familyId: String): ApiResult<String>
    suspend fun rotateSecret(familyId: String): ApiResult<String>
    suspend fun leaveFamily(familyId: String): ApiResult<Unit>
    suspend fun removeMember(familyId: String, userId: String): ApiResult<Unit>
    suspend fun getCurrentUserId(): ApiResult<String>
}

class RetrofitFamilyRepository(
    private val api: FamilyApi = HangOnApi.familyApi,
    private val userApi: UserApi = HangOnApi.userApi,
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : FamilyRepository {

    private suspend fun bearer(): String {
        val token = authRepository.getIdToken()
            ?: throw IllegalStateException("Sesi tidak valid, silakan masuk kembali.")
        return "Bearer $token"
    }

    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            ApiResult.Failure(e.response()?.errorBody()?.string() ?: "Terjadi kesalahan (${e.code()})")
        } catch (e: IOException) {
            ApiResult.Failure("Tidak dapat terhubung ke server. Periksa koneksi Anda.")
        } catch (e: IllegalStateException) {
            ApiResult.Failure(e.message ?: "Sesi tidak valid, silakan masuk kembali.")
        }
    }

    override suspend fun listMyFamilies(): ApiResult<List<FamilySummary>> = safeCall {
        api.listMyFamilies(bearer()).families.map { it.toDomain() }
    }

    override suspend fun createFamily(name: String): ApiResult<Unit> = safeCall {
        api.createFamily(bearer(), FamilyCreateRequestDto(name))
        Unit
    }

    override suspend fun joinFamily(inviteCode: String): ApiResult<Unit> = safeCall {
        api.joinFamily(bearer(), FamilyJoinRequestDto(inviteCode))
        Unit
    }

    override suspend fun getFamilyDetail(familyId: String): ApiResult<FamilyDetail> = safeCall {
        val auth = bearer()
        val summary = api.listMyFamilies(auth).families.find { it.id == familyId }
            ?: throw IllegalStateException("Family tidak ditemukan")
        val members = api.getMembers(auth, familyId)
        members.toDomain(
            familyId = summary.id,
            familyName = summary.name,
            inviteCode = summary.inviteCode,
            myRole = summary.role
        )
    }

    override suspend fun getSecret(familyId: String): ApiResult<String> = safeCall {
        api.getSecret(bearer(), familyId).sharedSecret
    }

    override suspend fun rotateSecret(familyId: String): ApiResult<String> = safeCall {
        api.rotateSecret(bearer(), familyId).sharedSecret
    }

    override suspend fun leaveFamily(familyId: String): ApiResult<Unit> = safeCall {
        api.leaveFamily(bearer(), familyId)
    }

    override suspend fun removeMember(familyId: String, userId: String): ApiResult<Unit> = safeCall {
        api.removeMember(bearer(), familyId, userId)
    }

    override suspend fun getCurrentUserId(): ApiResult<String> = safeCall {
        userApi.getMe(bearer()).id
    }
}
