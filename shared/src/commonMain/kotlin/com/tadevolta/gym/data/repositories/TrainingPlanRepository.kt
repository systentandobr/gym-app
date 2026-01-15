package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.remote.TrainingPlanService
import com.tadevolta.gym.data.local.TadevoltaDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class TrainingPlanRepository(
    private val remoteService: TrainingPlanService,
    private val database: TadevoltaDatabase
) {
    
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    fun getTrainingPlans(studentId: String): Flow<List<TrainingPlan>> = flow {
        // Emitir cache local primeiro
        val cached = database.trainingPlanQueries.selectAll(studentId).executeAsList()
            .map { planRow ->
                json.decodeFromString<TrainingPlan>(planRow.data_json)
            }
        emit(cached)
        
        // Buscar remoto e atualizar cache
        when (val result = remoteService.getTrainingPlans(studentId)) {
            is Result.Success -> {
                result.data.forEach { plan ->
                    database.trainingPlanQueries.insertOrReplace(
                        id = plan.id,
                        unit_id = plan.unitId,
                        student_id = plan.studentId,
                        name = plan.name,
                        description = plan.description,
                        status = plan.status.name,
                        start_date = plan.startDate.toLongOrNull() ?: 0L,
                        end_date = plan.endDate?.toLongOrNull(),
                        updated_at = System.currentTimeMillis(),
                        data_json = json.encodeToString(TrainingPlan.serializer(), plan)
                    )
                }
                emit(result.data)
            }
            else -> {
                // Manter cache em caso de erro
            }
        }
    }
    
    suspend fun getTrainingPlanById(id: String): com.tadevolta.gym.data.models.Result<TrainingPlan> {
        // Tentar cache primeiro
        val cached = database.trainingPlanQueries.selectById(id).executeAsOneOrNull()
        if (cached != null) {
            val plan = json.decodeFromString<TrainingPlan>(cached.data_json)
            return Result.Success(plan)
        }
        
        // Buscar remoto
        return remoteService.getTrainingPlanById(id)
    }
    
    suspend fun updateExerciseExecution(
        planId: String,
        exerciseId: String,
        executedSets: List<ExecutedSet>
    ): Result<TrainingPlan> {
        // Salvar localmente primeiro
        executedSets.forEach { set ->
            database.trainingPlanQueries.insertExerciseExecution(
                id = "${planId}_${exerciseId}_${set.setNumber}",
                training_plan_id = planId,
                exercise_id = exerciseId,
                set_number = set.setNumber.toLong(),
                planned_reps = set.plannedReps,
                executed_reps = set.executedReps?.toLong(),
                planned_weight = set.plannedWeight,
                executed_weight = set.executedWeight,
                completed = if (set.completed) 1L else 0L,
                timestamp = System.currentTimeMillis()
            )
        }
        
        // Sincronizar com remoto
        return remoteService.updateExerciseExecution(planId, exerciseId, executedSets)
    }
}

// Extension para converter String para Long (timestamp)
// Nota: startDate e endDate são Strings no modelo, não Instant
// Isso precisa ser ajustado conforme a API real
