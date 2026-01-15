package com.tadevolta.gym.domain.usecases

import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.repositories.TrainingPlanRepository

class ExecuteExerciseUseCase(
    private val repository: TrainingPlanRepository
) {
    suspend operator fun invoke(
        planId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<Unit> {
        return when (val result = repository.updateExerciseExecution(planId, exerciseId, executedSets)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
    }
}
