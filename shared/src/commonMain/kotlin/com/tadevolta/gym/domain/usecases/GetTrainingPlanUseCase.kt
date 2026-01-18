package com.tadevolta.gym.domain.usecases

import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTrainingPlanUseCase(
    private val repository: TrainingPlanRepository
) {
    suspend operator fun invoke(studentId: String): Flow<Result<TrainingPlan>> {
        return repository.getTrainingPlans(studentId)
            .map { plans ->
                plans.firstOrNull()?.let { Result.Success(it) }
                    ?: Result.Error(Exception("Nenhum plano encontrado"))
            }
    }
}
