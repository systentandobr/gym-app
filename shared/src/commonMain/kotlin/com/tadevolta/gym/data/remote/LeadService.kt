package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.tadevolta.gym.utils.config.EnvironmentConfig
import kotlinx.serialization.json.Json

interface LeadService {
    suspend fun postLead(lead: LeadRequest): Result<LeadResponse>
}

class LeadServiceImpl(
    private val client: HttpClient
) : LeadService {
    
    override suspend fun postLead(lead: LeadRequest): Result<LeadResponse> {
        return try {
            val response = client.post("${EnvironmentConfig.API_BASE_URL}/leads/public") {
                contentType(ContentType.Application.Json)
                setBody(lead)
                // Endpoint público, não requer autenticação
            }
            
            // A API retorna LeadResponse diretamente, não envolto em ApiResponse
            val leadResponse: LeadResponse = response.body()
            Result.Success(leadResponse)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
