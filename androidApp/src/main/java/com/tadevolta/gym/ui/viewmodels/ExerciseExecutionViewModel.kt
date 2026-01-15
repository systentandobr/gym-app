package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.domain.usecases.ExecuteExerciseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseExecutionViewModel(
    private val executeExerciseUseCase: ExecuteExerciseUseCase,
    private val planId: String,
    private val exerciseId: String,
    private val initialSets: Int
) : ViewModel() {
    
    private val _executedSets = MutableStateFlow<List<ExecutedSet>>(
        (1..initialSets).map { setNumber ->
            ExecutedSet(
                setNumber = setNumber,
                plannedReps = "",
                executedReps = null,
                plannedWeight = null,
                executedWeight = null,
                completed = false,
                timestamp = null
            )
        }
    )
    val executedSets: StateFlow<List<ExecutedSet>> = _executedSets.asStateFlow()
    
    private val _isFinishing = MutableStateFlow(false)
    val isFinishing: StateFlow<Boolean> = _isFinishing.asStateFlow()
    
    fun completeSet(set: ExecutedSet) {
        val updatedSets = _executedSets.value.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(completed = true)
            _executedSets.value = updatedSets
        }
    }
    
    fun updateWeight(set: ExecutedSet, weight: Double) {
        val updatedSets = _executedSets.value.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(executedWeight = weight)
            _executedSets.value = updatedSets
        }
    }
    
    fun updateReps(set: ExecutedSet, reps: Int) {
        val updatedSets = _executedSets.value.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(executedReps = reps)
            _executedSets.value = updatedSets
        }
    }
    
    fun finishExercise() {
        viewModelScope.launch {
            _isFinishing.value = true
            executeExerciseUseCase(
                planId = planId,
                exerciseId = exerciseId,
                executedSets = _executedSets.value
            )
            _isFinishing.value = false
        }
    }
}
