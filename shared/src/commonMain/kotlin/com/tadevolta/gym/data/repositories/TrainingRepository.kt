package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.TrainingService
import com.tadevolta.gym.data.local.TadevoltaDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class TrainingRepository(
    private val remoteService: TrainingService,
    private val database: TadevoltaDatabase
) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    suspend fun createTrainingExecution(planId: String): Result<TrainingExecution> {
        return when (val result = remoteService.createTrainingExecution(planId)) {
            is Result.Success -> {
                // Salvar localmente
                saveTrainingExecutionLocal(result.data)
                result
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
    }
    
    suspend fun updateExerciseExecution(
        trainingId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<TrainingExecution> {
        // Salvar localmente primeiro
        // A tabela exercise_execution está em TrainingPlan.sq, então usa trainingPlanQueries
        executedSets.forEach { set ->
            database.trainingPlanQueries.insertExerciseExecution(
                id = "${trainingId}_${exerciseId}_${set.setNumber}",
                training_execution_id = trainingId,
                exercise_id = exerciseId,
                set_number = set.setNumber.toLong(),
                planned_reps = set.plannedReps,
                executed_reps = set.executedReps?.toLong(),
                planned_weight = set.plannedWeight,
                executed_weight = set.executedWeight,
                completed = if (set.completed) 1L else 0L,
                timestamp = System.currentTimeMillis(),
                duration_seconds = set.durationSeconds?.toLong(),
                rest_duration_seconds = set.restDurationSeconds?.toLong()
            )
        }
        
        // Sincronizar com remoto
        return when (val result = remoteService.updateExerciseExecution(trainingId, exerciseId, executedSets)) {
            is Result.Success -> {
                // Atualizar cache local
                saveTrainingExecutionLocal(result.data)
                result
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
    }
    
    suspend fun completeTrainingExecution(
        trainingId: String,
        totalDurationSeconds: Int? = null
    ): Result<TrainingExecution> {
        return when (val result = remoteService.completeTrainingExecution(trainingId, totalDurationSeconds)) {
            is Result.Success -> {
                // Atualizar cache local
                saveTrainingExecutionLocal(result.data)
                result
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
    }
    
    suspend fun getActiveTrainingExecution(): Result<TrainingExecution?> {
        // Tentar buscar do cache local primeiro
        // Por enquanto, sempre buscar do remoto para garantir dados atualizados
        return remoteService.getActiveTrainingExecution()
    }
    
    suspend fun getTrainingExecutionsByPlan(planId: String): Result<List<TrainingExecution>> {
        return remoteService.getTrainingExecutionsByPlan(planId)
    }
    
    suspend fun getTrainingExecutionById(trainingId: String): Result<TrainingExecution> {
        // Tentar buscar do cache local primeiro
        val cached = database.trainingExecutionQueries.selectById(trainingId).executeAsOneOrNull()
        if (cached != null) {
            try {
                val trainingExecution = json.decodeFromString<TrainingExecution>(cached.data_json)
                return Result.Success(trainingExecution)
            } catch (e: Exception) {
                // Se falhar ao deserializar, buscar do remoto
            }
        }
        
        // Buscar do remoto
        return when (val result = remoteService.getTrainingExecutionById(trainingId)) {
            is Result.Success -> {
                saveTrainingExecutionLocal(result.data)
                result
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Erro desconhecido"))
        }
    }
    
    private fun saveTrainingExecutionLocal(trainingExecution: TrainingExecution) {
        // Converter startedAt e completedAt de ISO string para timestamp
        val startedAtTimestamp = try {
            java.time.Instant.parse(trainingExecution.startedAt).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        val completedAtTimestamp = trainingExecution.completedAt?.let {
            try {
                java.time.Instant.parse(it).toEpochMilli()
            } catch (e: Exception) {
                null
            }
        }
        
        database.trainingExecutionQueries.insertOrReplace(
            id = trainingExecution.id,
            training_plan_id = trainingExecution.trainingPlanId,
            user_id = trainingExecution.userId,
            unit_id = trainingExecution.unitId,
            started_at = startedAtTimestamp,
            completed_at = completedAtTimestamp,
            status = when (trainingExecution.status) {
                TrainingExecutionStatus.IN_PROGRESS -> "in_progress"
                TrainingExecutionStatus.COMPLETED -> "completed"
                TrainingExecutionStatus.ABANDONED -> "abandoned"
            },
            total_duration_seconds = trainingExecution.totalDurationSeconds?.toLong(),
            data_json = json.encodeToString(TrainingExecution.serializer(), trainingExecution)
        )
    }
}
