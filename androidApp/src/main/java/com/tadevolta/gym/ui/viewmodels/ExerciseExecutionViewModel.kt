package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.data.models.Exercise
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.domain.usecases.ExecuteExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseExecutionUiState(
    val workoutTitle: String = "TREINO",
    val workoutSubtitle: String = "",
    val exercise: Exercise? = null,
    val exercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = -1,
    val executedSets: List<ExecutedSet> = emptyList(),
    val nextExercises: List<Exercise> = emptyList(),
    val totalExercises: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExecutionRunning: Boolean = false,
    val totalExecutionTimeSeconds: Long = 0,
    val restTimeRemaining: Int? = null
)

@HiltViewModel
class ExerciseExecutionViewModel @Inject constructor(
    private val executeExerciseUseCase: ExecuteExerciseUseCase,
    private val trainingPlanRepository: TrainingPlanRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val planId: String = savedStateHandle.get<String>("planId") ?: ""
    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: ""
    private val dayOfWeek: Int? = savedStateHandle.get<Int>("dayOfWeek")
    
    private val _uiState = MutableStateFlow(ExerciseExecutionUiState())
    val uiState: StateFlow<ExerciseExecutionUiState> = _uiState.asStateFlow()
    
    private val _isFinishing = MutableStateFlow(false)
    val isFinishing: StateFlow<Boolean> = _isFinishing.asStateFlow()
    
    private var executionTimerJob: Job? = null
    private var restTimerJob: Job? = null
    
    init {
        loadExerciseData()
    }
    
    override fun onCleared() {
        super.onCleared()
        executionTimerJob?.cancel()
        restTimerJob?.cancel()
    }
    
    private fun loadExerciseData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Carregar plano de treino
            when (val result = trainingPlanRepository.getTrainingPlanById(planId)) {
                is Result.Success -> {
                    val plan = result.data
                    
                    // Buscar exercícios de weeklySchedule se dayOfWeek foi fornecido
                    val exercises = if (dayOfWeek != null) {
                        plan.weeklySchedule.find { it.dayOfWeek == dayOfWeek }?.exercises 
                            ?: emptyList()
                    } else {
                        // Fallback: usar plan.exercises se weeklySchedule estiver vazio
                        plan.exercises.ifEmpty {
                            plan.weeklySchedule.flatMap { it.exercises }
                        }
                    }
                    
                    val exercise = exercises.find { 
                        it.exerciseId == exerciseId || it.name == exerciseId 
                    }
                    val currentIndex = exercises.indexOfFirst { 
                        it.exerciseId == exerciseId || it.name == exerciseId 
                    }
                    
                    // Inicializar sets executados
                    val initialSets = exercise?.sets ?: 3
                    val executedSets = (1..initialSets).map { setNumber ->
                        ExecutedSet(
                            setNumber = setNumber,
                            plannedReps = exercise?.reps ?: "",
                            executedReps = null,
                            plannedWeight = exercise?.weight,
                            executedWeight = null,
                            completed = false,
                            timestamp = null
                        )
                    }
                    
                    // Determinar subtítulo do treino baseado no dayOfWeek
                    val workoutSubtitle = if (dayOfWeek != null) {
                        val dayNames = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
                        val dayName = dayNames.getOrElse(dayOfWeek) { "Dia $dayOfWeek" }
                        val timeSlot = plan.weeklySchedule.find { it.dayOfWeek == dayOfWeek }
                            ?.timeSlots?.firstOrNull()?.activity
                        if (timeSlot != null) {
                            "$dayName - $timeSlot"
                        } else {
                            "$dayName - ${plan.name}"
                        }
                    } else {
                        plan.name
                    }
                    
                    // Obter próximos exercícios (após o atual)
                    val nextExercises = if (currentIndex >= 0 && currentIndex < exercises.size - 1) {
                        exercises.subList(currentIndex + 1, exercises.size)
                    } else {
                        emptyList()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        workoutTitle = "Treino Ativo",
                        workoutSubtitle = workoutSubtitle,
                        exercise = exercise,
                        exercises = exercises,
                        currentExerciseIndex = currentIndex,
                        executedSets = executedSets,
                        nextExercises = nextExercises,
                        totalExercises = exercises.size,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao carregar exercício"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun navigateToExercise(newExerciseId: String) {
        savedStateHandle["exerciseId"] = newExerciseId
        loadExerciseData()
    }
    
    fun navigateToPrevious(): String? {
        val currentIndex = _uiState.value.currentExerciseIndex
        val exercises = _uiState.value.exercises
        
        if (currentIndex > 0) {
            val previousExercise = exercises[currentIndex - 1]
            return previousExercise.exerciseId ?: previousExercise.name
        }
        return null
    }
    
    fun navigateToNext(): String? {
        val currentIndex = _uiState.value.currentExerciseIndex
        val exercises = _uiState.value.exercises
        
        if (currentIndex >= 0 && currentIndex < exercises.size - 1) {
            val nextExercise = exercises[currentIndex + 1]
            return nextExercise.exerciseId ?: nextExercise.name
        }
        return null
    }
    
    fun completeSet(set: ExecutedSet) {
        val updatedSets = _uiState.value.executedSets.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(completed = true)
            _uiState.value = _uiState.value.copy(executedSets = updatedSets)
        }
    }
    
    fun updateWeight(set: ExecutedSet, weight: Double) {
        val updatedSets = _uiState.value.executedSets.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(executedWeight = weight)
            _uiState.value = _uiState.value.copy(executedSets = updatedSets)
        }
    }
    
    fun updateReps(set: ExecutedSet, reps: Int) {
        val updatedSets = _uiState.value.executedSets.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(executedReps = reps)
            _uiState.value = _uiState.value.copy(executedSets = updatedSets)
        }
    }
    
    fun finishExercise() {
        viewModelScope.launch {
            _isFinishing.value = true
            executeExerciseUseCase(
                planId = planId,
                exerciseId = exerciseId,
                executedSets = _uiState.value.executedSets
            )
            _isFinishing.value = false
        }
    }
    
    fun startExecutionTimer() {
        if (_uiState.value.isExecutionRunning) return
        
        _uiState.value = _uiState.value.copy(isExecutionRunning = true)
        
        executionTimerJob = viewModelScope.launch {
            while (_uiState.value.isExecutionRunning) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    totalExecutionTimeSeconds = _uiState.value.totalExecutionTimeSeconds + 1
                )
            }
        }
    }
    
    fun pauseExecutionTimer() {
        _uiState.value = _uiState.value.copy(isExecutionRunning = false)
        executionTimerJob?.cancel()
        executionTimerJob = null
    }
    
    fun startRestTimer() {
        val exercise = _uiState.value.exercise ?: return
        val executedSets = _uiState.value.executedSets
        
        // Verificar se há pelo menos uma série completa (permite descanso após cada série)
        val hasCompletedSet = executedSets.any { it.completed == true }
        
        if (!hasCompletedSet) return
        
        // Verificar se todas as séries planejadas estão completas
        val allSetsCompleted = (0 until exercise.sets).all { index ->
            executedSets.getOrNull(index)?.completed == true
        }
        
        val restTime = exercise.restTime ?: 60
        _uiState.value = _uiState.value.copy(restTimeRemaining = restTime)
        
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            var remaining = restTime
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(restTimeRemaining = remaining)
            }
            
            // Timer finalizado
            _uiState.value = _uiState.value.copy(restTimeRemaining = null)
            
            // Se todas as séries estiverem completas, finalizar exercício
            // Caso contrário, apenas continuar para próxima série
            if (allSetsCompleted) {
                finishExercise()
            }
        }
    }
    
    fun cancelRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        _uiState.value = _uiState.value.copy(restTimeRemaining = null)
    }
    
    fun addExtraSet() {
        val exercise = _uiState.value.exercise ?: return
        val currentSets = _uiState.value.executedSets
        val nextSetNumber = currentSets.size + 1
        
        val extraSet = ExecutedSet(
            setNumber = nextSetNumber,
            plannedReps = exercise.reps,
            executedReps = null,
            plannedWeight = exercise.weight,
            executedWeight = null,
            completed = false,
            timestamp = null
        )
        
        _uiState.value = _uiState.value.copy(
            executedSets = currentSets + extraSet
        )
    }
    
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}
