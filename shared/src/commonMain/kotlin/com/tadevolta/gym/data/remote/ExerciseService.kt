package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
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
    private val tokenProvider: () -> String?
) : ExerciseService {
    
    override suspend fun getExercise(id: String): Result<Exercise> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/exercises/$id") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
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
            
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<Exercise> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar exercício"))
            }
        } catch (e: io.ktor.serialization.JsonConvertException) {
            Result.Error(Exception("Erro ao processar resposta do servidor: ${e.message}"))
        } catch (e: Exception) {
            Result.Error(e)
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
