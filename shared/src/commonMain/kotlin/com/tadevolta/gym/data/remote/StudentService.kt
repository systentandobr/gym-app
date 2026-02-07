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
import kotlinx.serialization.json.*

interface StudentService {
    suspend fun createStudent(
        userId: String,
        unitId: String,
        studentData: CreateStudentRequest
    ): Result<Student>
    
    suspend fun getStudentByUserId(userId: String): Result<Student>
    
    suspend fun updateStudent(
        studentId: String,
        birthDate: String? = null,
        gender: Gender? = null,
        phone: String? = null
    ): Result<Student>
}

class StudentServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : StudentService {
    
    override suspend fun createStudent(
        userId: String,
        unitId: String,
        studentData: CreateStudentRequest
    ): Result<Student> {
        return try {
            val token = tokenProvider()
            if (token == null) {
                return Result.Error(Exception("Token de autenticação não disponível"))
            }
            
            // Construir JSON manualmente para não incluir campos null
            val jsonBody = buildJsonObject {
                put("name", studentData.name)
                put("email", studentData.email)
                put("unitId", unitId)
                put("userId", userId)
                studentData.phone?.let { put("phone", it) }
                studentData.cpf?.let { put("cpf", it) }
                studentData.birthDate?.let { put("birthDate", it) }
                studentData.gender?.let { put("gender", it.name.lowercase()) }
                studentData.address?.let { address ->
                    putJsonObject("address") {
                        address.street?.let { put("street", it) }
                        address.number?.let { put("number", it) }
                        address.complement?.let { put("complement", it) }
                        address.neighborhood?.let { put("neighborhood", it) }
                        put("city", address.city)  // obrigatório
                        put("state", address.state)  // obrigatório
                        address.zipCode?.let { put("zipCode", it) }
                    }
                }
                studentData.emergencyContact?.let { ec ->
                    putJsonObject("emergencyContact") {
                        put("name", ec.name)
                        put("phone", ec.phone)
                        put("relationship", ec.relationship)
                    }
                }
                studentData.healthInfo?.let { health ->
                    putJsonObject("healthInfo") {
                        health.medicalConditions?.let { 
                            putJsonArray("medicalConditions") { 
                                it.forEach { condition -> add(condition) } 
                            } 
                        }
                        health.medications?.let { 
                            putJsonArray("medications") { 
                                it.forEach { medication -> add(medication) } 
                            } 
                        }
                        health.injuries?.let { 
                            putJsonArray("injuries") { 
                                it.forEach { injury -> add(injury) } 
                            } 
                        }
                        health.fitnessLevel?.let { put("fitnessLevel", it.name.lowercase()) }
                    }
                }
            }
            
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/students")
                        }
                        method = HttpMethod.Post
                        headers {
                            append("Content-Type", "application/json")
                        }
                        contentType(ContentType.Application.Json)
                        setBody(jsonBody)
                    },
                    responseHandler = { it }
                )
            } else {
                client.post("${EnvironmentConfig.API_BASE_URL}/students") {
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Content-Type", "application/json")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(jsonBody)
                }
            }
            
            // Verificar status HTTP
            if (response.status.value >= 400) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.error 
                        ?: "Erro ao criar aluno"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Erro ao criar aluno: ${response.status.description}"))
                }
            }
            
            // Deserializar resposta
            val apiResponse: ApiResponse<Student> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao criar aluno"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getStudentByUserId(userId: String): Result<Student> {
        return try {
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url("${EnvironmentConfig.API_BASE_URL}/students/by-user/$userId")
                        method = HttpMethod.Get
                        headers {
                            append("Content-Type", "application/json")
                        }
                    },
                    responseHandler = { it }
                )
            } else {
                val token = tokenProvider()
                if (token == null) {
                    return Result.Error(Exception("Token de autenticação não disponível"))
                }
                client.get("${EnvironmentConfig.API_BASE_URL}/students/by-user/$userId") {
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Content-Type", "application/json")
                    }
                }
            }
            
            // Verificar status HTTP
            if (response.status.value >= 400) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.error 
                        ?: "Aluno não encontrado"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Erro ao buscar aluno: ${response.status.description}"))
                }
            }
            
            // Deserializar resposta
            val apiResponse: ApiResponse<Student> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Aluno não encontrado"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateStudent(
        studentId: String,
        birthDate: String?,
        gender: Gender?,
        phone: String?
    ): Result<Student> {
        return try {
            
            // Construir JSON apenas com campos fornecidos
            val jsonBody = buildJsonObject {
                birthDate?.let { put("birthDate", it) }
                gender?.let { put("gender", it.name.lowercase()) }
                phone?.let { put("phone", it) }
            }
            
            val response = if (authRepository != null || tokenManager != null) {
                executeWithRetry(
                    client = client,
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    tokenProvider = tokenProvider,
                    maxRetries = 3,
                    requestBuilder = {
                        url {
                            takeFrom("${EnvironmentConfig.API_BASE_URL}/students/$studentId")
                        }
                        method = HttpMethod.Patch
                        headers {
                            append("Content-Type", "application/json")
                        }
                        contentType(ContentType.Application.Json)
                        setBody(jsonBody)
                    },
                    responseHandler = { it }
                )
            } else {
                val token = tokenProvider()
                if (token == null) {
                    return Result.Error(Exception("Token de autenticação não disponível"))
                }
                client.patch("${EnvironmentConfig.API_BASE_URL}/students/$studentId") {
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Content-Type", "application/json")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(jsonBody)
                }
            }
            
            // Verificar status HTTP
            if (response.status.value >= 400) {
                try {
                    val errorResponse: ErrorResponse = response.body()
                    val errorMessage = errorResponse.formattedMessage 
                        ?: errorResponse.error 
                        ?: "Erro ao atualizar aluno"
                    return Result.Error(Exception(errorMessage))
                } catch (e: Exception) {
                    return Result.Error(Exception("Erro ao atualizar aluno: ${response.status.description}"))
                }
            }
            
            // Deserializar resposta
            val apiResponse: ApiResponse<Student> = response.body()
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao atualizar aluno"))
            }
        } catch (e: UnauthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
