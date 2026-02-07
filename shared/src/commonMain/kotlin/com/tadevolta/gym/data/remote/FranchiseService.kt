package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.repositories.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import com.tadevolta.gym.utils.config.EnvironmentConfig

// Exceção customizada para indicar que localização é necessária
class LocationRequiredException(message: String) : Exception(message)

interface FranchiseService {
    suspend fun findNearby(
        lat: Double? = null,
        lng: Double? = null,
        marketSegment: String = "gym",
        radius: Int = 50,
        limit: Int = 20
    ): Result<List<NearbyFranchise>>
}

class FranchiseServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String? = { null },
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : FranchiseService {
    
    override suspend fun findNearby(
        lat: Double?,
        lng: Double?,
        marketSegment: String,
        radius: Int,
        limit: Int
    ): Result<List<NearbyFranchise>> {
        return try {
            val url = "${EnvironmentConfig.API_BASE_URL}/franchises/nearby"
            val response = executeWithRetry(
                client = client,
                authRepository = authRepository,
                tokenManager = tokenManager,
                tokenProvider = tokenProvider,
                maxRetries = 3,
                requestBuilder = {
                    url(url)
                    lat?.let { parameter("lat", it) }
                    lng?.let { parameter("lng", it) }
                    parameter("marketSegment", marketSegment)
                    parameter("radius", radius)
                    parameter("limit", limit)
                },
                responseHandler = { it }
            )
            
            // Verificar status code antes de parsear
            if (response.status.value == 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                
                // Verificar se o erro é sobre coordenadas necessárias
                if (errorBody?.contains("coordenadas", ignoreCase = true) == true || 
                    errorBody?.contains("lat/lng", ignoreCase = true) == true ||
                    errorBody?.contains("É necessário fornecer coordenadas", ignoreCase = true) == true ||
                    errorBody?.contains("É necessário fornecer", ignoreCase = true) == true) {
                    return Result.Error(LocationRequiredException("É necessário fornecer sua localização para encontrar unidades próximas. Clique no ícone de localização ou no campo de busca para permitir o acesso."))
                } else {
                    return Result.Error(Exception("Erro ao buscar unidades: ${errorBody ?: "Requisição inválida"}"))
                }
            }
            
            val apiResponse: ApiResponse<List<NearbyFranchise>> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar unidades próximas"))
            }
        } catch (e: LocationRequiredException) {
            // Erro específico: precisa de localização
            Result.Error(e)
        } catch (e: java.net.UnknownHostException) {
            // Erro de DNS/conectividade
            Result.Error(Exception("Erro de conexão: Verifique sua conexão com a internet. ${e.message}"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.Error(Exception("Tempo de conexão esgotado. Verifique sua conexão com a internet."))
        } catch (e: java.net.ConnectException) {
            Result.Error(Exception("Não foi possível conectar ao servidor. Verifique sua conexão com a internet."))
        } catch (e: Exception) {
            Result.Error(Exception("Erro ao buscar unidades próximas: ${e.message ?: e.javaClass.simpleName}"))
        }
    }
}
