package com.tadevolta.gym.utils

import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.data.remote.StudentService
import kotlinx.coroutines.flow.first

/**
 * Helper para obter o studentId do usuário.
 * Tenta obter do primeiro TrainingPlan ativo, caso contrário busca no backend através do userId.
 */
suspend fun getStudentId(
    userId: String,
    trainingPlanRepository: TrainingPlanRepository,
    studentService: StudentService? = null
): String {
    return try {
        // Tentar obter studentId do primeiro plano de treino
        val plans = trainingPlanRepository.getTrainingPlans(null).first()
        val studentId = plans.firstOrNull()?.studentId
        
        if (studentId != null) {
            return studentId
        }
        
        // Buscar no backend através do userId
        if (studentService != null) {
            try {
                when (val result = studentService.getStudentByUserId(userId)) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        return result.data.id
                    }
                    else -> {
                        // Se não encontrou aluno no backend, usar userId como fallback
                        return userId
                    }
                }
            } catch (e: Exception) {
                // Em caso de erro ao buscar no backend, usar userId como fallback
                return userId
            }
        }
        
        // Se não tem studentService disponível, usar userId como fallback
        userId
    } catch (e: Exception) {
        // Em caso de erro, usar userId como fallback
        userId
    }
}
