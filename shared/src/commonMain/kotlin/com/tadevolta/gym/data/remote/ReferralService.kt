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

interface ReferralService {
    suspend fun createReferral(referral: ReferralRequest): Result<ReferralResponse>
}

class ReferralServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?,
    private val authRepository: AuthRepository? = null
) : ReferralService {
    
    override suspend fun createReferral(referral: ReferralRequest): Result<ReferralResponse> {
        return try {
            val response = if (authRepository != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/referrals")
                        }
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        setBody(referral)
                        headers {
                            tokenProvider()?.let { 
                                append("Authorization", "Bearer $it") 
                            }
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                client.post("${EnvironmentConfig.API_BASE_URL}/referrals") {
                    contentType(ContentType.Application.Json)
                    setBody(referral)
                    headers {
                        tokenProvider()?.let { 
                            append("Authorization", "Bearer $it") 
                        }
                    }
                }
            }
            
            val referralResponse: ReferralResponse = response.body()
            Result.Success(referralResponse)
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
