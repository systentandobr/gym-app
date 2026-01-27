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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseExecutionUiState(
    val workoutTitle: String = "TREINO",
    val exercise: Exercise? = null,
    val exercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = -1,
    val executedSets: List<ExecutedSet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExerciseExecutionViewModel @Inject constructor(
    private val executeExerciseUseCase: ExecuteExerciseUseCase,
    private val trainingPlanRepository: TrainingPlanRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val planId: String = savedStateHandle.get<String>("planId") ?: ""
    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: ""
    
    private val _uiState = MutableStateFlow(ExerciseExecutionUiState())
    val uiState: StateFlow<ExerciseExecutionUiState> = _uiState.asStateFlow()
    
    private val _isFinishing = MutableStateFlow(false)
    val isFinishing: StateFlow<Boolean> = _isFinishing.asStateFlow()
    
    init {
        loadExerciseData()
    }
    
    private fun loadExerciseData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Carregar plano de treino
            when (val result = trainingPlanRepository.getTrainingPlanById(planId)) {
                is Result.Success -> {
                    val plan = result.data
                    val exercise = plan.exercises.find { 
                        it.exerciseId == exerciseId || it.name == exerciseId 
                    }
                    val currentIndex = plan.exercises.indexOfFirst { 
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
                    
                    _uiState.value = _uiState.value.copy(
                        workoutTitle = plan.name,
                        exercise = exercise,
                        exercises = plan.exercises,
                        currentExerciseIndex = currentIndex,
                        executedSets = executedSets,
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
}
