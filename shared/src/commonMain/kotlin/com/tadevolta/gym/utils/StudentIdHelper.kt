package com.tadevolta.gym.utils

import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import kotlinx.coroutines.flow.first

/**
 * Helper para obter o studentId do usuário.
 * Tenta obter do primeiro TrainingPlan ativo, caso contrário usa userId como fallback.
 */
suspend fun getStudentId(
    userId: String,
    trainingPlanRepository: TrainingPlanRepository
): String {
    return try {
        // Tentar obter studentId do primeiro plano de treino
        val plans = trainingPlanRepository.getTrainingPlans(null).first()
        val studentId = plans.firstOrNull()?.studentId
        // Se não encontrou, usar userId como fallback
        // (assumindo que em alguns casos podem ser iguais, ou que o backend aceita)
        studentId ?: userId
    } catch (e: Exception) {
        // Em caso de erro, usar userId como fallback
        userId
    }
}
