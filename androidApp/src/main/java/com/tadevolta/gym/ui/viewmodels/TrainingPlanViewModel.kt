package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainingPlanViewModel(
    private val repository: TrainingPlanRepository
) : ViewModel() {
    
    private val _trainingPlan = MutableStateFlow<Result<TrainingPlan>>(Result.Loading)
    val trainingPlan: StateFlow<Result<TrainingPlan>> = _trainingPlan.asStateFlow()
    
    fun loadPlan(planId: String) {
        viewModelScope.launch {
            _trainingPlan.value = Result.Loading
            _trainingPlan.value = repository.getTrainingPlanById(planId)
        }
    }
}
