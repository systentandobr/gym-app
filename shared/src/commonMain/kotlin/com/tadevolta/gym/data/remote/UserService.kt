package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

interface UserService {
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateProfile(data: UpdateUserData): Result<User>
}

class UserServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : UserService {
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = client.get("/users/profile") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<User> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar usuário"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateProfile(data: UpdateUserData): Result<User> {
        return try {
            val response = client.patch("/users/profile") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
                setBody(data)
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<User> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao atualizar perfil"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
