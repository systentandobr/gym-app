package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
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

interface ExerciseService {
    suspend fun getExercise(id: String): Result<Exercise>
    /**
     * Busca exercício por nome (fallback temporário quando exerciseId não está disponível).
     * Nota: Este método tenta buscar usando o nome como ID, mas pode não funcionar.
     * A solução ideal é o backend retornar exerciseId no plano de treino.
     */
    suspend fun searchExerciseByName(name: String): Result<Exercise>
}

class ExerciseServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : ExerciseService {
    
    override suspend fun getExercise(id: String): Result<Exercise> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/exercises/$id")
                        }
                        method = HttpMethod.Get
                    },
                    responseHandler = { it }
                )
            } else {
                val token = tokenProvider()
                client.get("${EnvironmentConfig.API_BASE_URL}/exercises/$id") {
                    headers {
                        token?.let { append("Authorization", "Bearer $it") }
                    }
                }
            }
            
            // Tratar erro 404
            if (response.status.value == 404) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.message 
                        ?: errorResponse.error 
                        ?: "Exercício não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Exercício não encontrado (404)"))
                }
            }
            
            // Tratar outros erros HTTP
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    null
                }
                return Result.Error(Exception("Erro ao buscar exercício: ${errorBody ?: "Erro do servidor"}"))
            }
            
            // A API retorna CatalogExercise (formato do catálogo) diretamente, não envolto em ApiResponse
            // Tentar deserializar como CatalogExercise primeiro
            try {
                val json = Json { ignoreUnknownKeys = true }
                val responseText = response.bodyAsText()
                
                // Tentar deserializar como CatalogExercise primeiro (formato do catálogo)
                try {
                    val catalogExercise: CatalogExercise = json.decodeFromString(CatalogExercise.serializer(), responseText)
                    // Converter CatalogExercise para Exercise
                    return Result.Success(catalogExercise.toExercise())
                } catch (e2: kotlinx.serialization.SerializationException) {
                    // Se falhar, tentar como Exercise diretamente (formato do plano de treino)
                    try {
                        val exercise: Exercise = json.decodeFromString(Exercise.serializer(), responseText)
                        return Result.Success(exercise)
                    } catch (e3: kotlinx.serialization.SerializationException) {
                        // Se falhar, tentar como ApiResponse<Exercise> (fallback)
                        try {
                            val apiResponse: ApiResponse<Exercise> = json.decodeFromString(
                                ApiResponse.serializer(Exercise.serializer()), 
                                responseText
                            )
                            if (apiResponse.success && apiResponse.data != null) {
                                return Result.Success(apiResponse.data)
                            } else {
                                return Result.Error(Exception(apiResponse.error ?: "Erro ao buscar exercício"))
                            }
                        } catch (e4: kotlinx.serialization.SerializationException) {
                            return Result.Error(Exception("Erro ao processar resposta do servidor. Formato não reconhecido: ${e2.message}"))
                        }
                    }
                }
            } catch (e: Exception) {
                return Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
            }
        } catch (e: UnauthenticatedException) {
            return Result.Error(e)
        } catch (e: Exception) {
            return Result.Error(Exception("Erro ao buscar exercício: ${e.message}"))
        }
    }
    
    override suspend fun searchExerciseByName(name: String): Result<Exercise> {
        // Nota: Não há endpoint de busca por nome no backend atualmente.
        // Este método retorna erro informando que exerciseId é necessário.
        // A solução ideal é o backend preservar exerciseId ao carregar templates.
        return Result.Error(
            Exception("Não é possível buscar exercício por nome. O backend deve retornar exerciseId no plano de treino.")
        )
    }
}
