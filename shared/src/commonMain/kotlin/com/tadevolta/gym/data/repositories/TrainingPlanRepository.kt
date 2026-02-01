package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.remote.TrainingPlanService
import com.tadevolta.gym.data.local.TadevoltaDatabase
import com.tadevolta.gym.utils.parseIsoToTimestamp
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
    
    fun getTrainingPlans(studentId: String? = null): Flow<List<TrainingPlan>> = flow {
        // Emitir cache local primeiro se tiver studentId
        if (studentId != null) {
            val cached = database.trainingPlanQueries.selectAll(studentId).executeAsList()
                .map { planRow ->
                    json.decodeFromString<TrainingPlan>(planRow.data_json)
                }
            emit(cached)
        }
        
        // Buscar remoto e atualizar cache
        when (val result = remoteService.getTrainingPlans(studentId, status = "active")) {
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
    
    suspend fun getTrainingPlanById(
        id: String,
        forceRefresh: Boolean = false,
        needsRefresh: Boolean = false
    ): com.tadevolta.gym.data.models.Result<TrainingPlan> {
        // Se não forçar refresh, verificar cache primeiro
        if (!forceRefresh) {
            val cached = database.trainingPlanQueries.selectById(id).executeAsOneOrNull()
            if (cached != null) {
                val cachedPlan = json.decodeFromString<TrainingPlan>(cached.data_json)
                
                // Se não precisa atualizar e cache está válido, retornar cache
                if (!needsRefresh && !shouldRefreshCache(cachedPlan, cached.updated_at)) {
                    return Result.Success(cachedPlan)
                }
            }
        }
        
        // Buscar remoto e atualizar cache
        when (val result = remoteService.getTrainingPlanById(id)) {
            is Result.Success -> {
                val plan = result.data
                // Atualizar cache com dados mais recentes
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
                return result
            }
            else -> {
                // Se falhar remoto e tiver cache, retornar cache
                if (!forceRefresh) {
                    val cached = database.trainingPlanQueries.selectById(id).executeAsOneOrNull()
                    if (cached != null) {
                        val plan = json.decodeFromString<TrainingPlan>(cached.data_json)
                        return Result.Success(plan)
                    }
                }
                return result
            }
        }
    }
    
    /**
     * Força refresh do plano de treino do servidor e atualiza o cache.
     */
    suspend fun refreshTrainingPlan(id: String): com.tadevolta.gym.data.models.Result<TrainingPlan> {
        return getTrainingPlanById(id, forceRefresh = true)
    }
    
    /**
     * Verifica se o cache precisa ser atualizado baseado em updatedAt e idade do cache.
     * 
     * @param cachedPlan Plano de treino do cache
     * @param cacheTimestamp Timestamp de quando o cache foi atualizado (em millis)
     * @return true se precisa atualizar, false caso contrário
     */
    private fun shouldRefreshCache(cachedPlan: TrainingPlan, cacheTimestamp: Long): Boolean {
        // Se não tem updatedAt no plano, sempre atualizar após 5 minutos
        val planUpdatedAt = cachedPlan.updatedAt
        if (planUpdatedAt == null) {
            val cacheAge = System.currentTimeMillis() - cacheTimestamp
            return cacheAge > 5 * 60 * 1000 // 5 minutos
        }
        
        // Converter ISO string para timestamp
        val planTimestamp = parseIsoToTimestamp(planUpdatedAt) ?: return true
        
        // Se cache foi atualizado há mais de 5 minutos, verificar servidor
        val cacheAge = System.currentTimeMillis() - cacheTimestamp
        if (cacheAge > 5 * 60 * 1000) { // 5 minutos
            return true
        }
        
        // Cache ainda é válido (menos de 5 minutos)
        return false
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
