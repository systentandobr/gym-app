package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.utils.auth.UnauthenticatedException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface SubscriptionService {
    suspend fun getSubscription(studentId: String): Result<StudentSubscription>
    suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>>
}

class SubscriptionServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?,
    private val authRepository: AuthRepository? = null
) : SubscriptionService {
    
    override suspend fun getSubscription(studentId: String): Result<StudentSubscription> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/students/$studentId")
                        }
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/students/$studentId") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                }
            }
            val studentResponse: ApiResponse<Student> = response.body()
            
            if (studentResponse.success && studentResponse.data?.subscription != null) {
                Result.Success(studentResponse.data.subscription)
            } else {
                Result.Error(Exception("Assinatura não encontrada"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/subscriptions/plans")
                        }
                        method = HttpMethod.Get
                        headers {
                            tokenProvider()?.let { append("Authorization", "Bearer $it") }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.get("${EnvironmentConfig.API_BASE_URL}/subscriptions/plans") {
                    headers {
                        tokenProvider()?.let { append("Authorization", "Bearer $it") }
                    }
                }
            }
            val apiResponse: ApiResponse<List<SubscriptionPlan>> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar planos de assinatura"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
