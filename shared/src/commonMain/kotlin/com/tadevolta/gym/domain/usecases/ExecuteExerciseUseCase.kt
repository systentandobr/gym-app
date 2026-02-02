package com.tadevolta.gym.domain.usecases

import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.repositories.TrainingRepository

class ExecuteExerciseUseCase(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(
        trainingId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<Unit> {
        return when (val result = repository.updateExerciseExecution(trainingId, exerciseId, executedSets)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
    }
}
