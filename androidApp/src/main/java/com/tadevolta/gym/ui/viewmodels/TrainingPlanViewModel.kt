package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.models.TrainingExecution
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.data.repositories.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingPlanViewModel @Inject constructor(
    private val repository: TrainingPlanRepository,
    private val trainingRepository: TrainingRepository
) : ViewModel() {
    
    private val _trainingPlan = MutableStateFlow<Result<TrainingPlan>>(Result.Loading)
    val trainingPlan: StateFlow<Result<TrainingPlan>> = _trainingPlan.asStateFlow()
    
    private val _activeExecution = MutableStateFlow<Result<TrainingExecution?>>(Result.Success(null))
    val activeExecution: StateFlow<Result<TrainingExecution?>> = _activeExecution.asStateFlow()
    
    private val _trainingPlans = MutableStateFlow<Result<List<TrainingPlan>>>(Result.Loading)
    val trainingPlans: StateFlow<Result<List<TrainingPlan>>> = _trainingPlans.asStateFlow()
    
    fun loadPlan(planId: String) {
        viewModelScope.launch {
            _trainingPlan.value = Result.Loading
            _trainingPlan.value = repository.getTrainingPlanById(planId)
            
            // Carregar execução ativa em paralelo
            loadActiveExecution()
        }
    }
    
    fun loadActiveExecution() {
        viewModelScope.launch {
            _activeExecution.value = trainingRepository.getActiveTrainingExecution()
        }
    }
    
    fun loadPlansByStudentId(studentId: String) {
        viewModelScope.launch {
            _trainingPlans.value = Result.Loading
            try {
                val plansFlow = repository.getTrainingPlans(studentId)
                val plans = plansFlow.first()
                _trainingPlans.value = Result.Success(plans)
            } catch (e: Exception) {
                _trainingPlans.value = Result.Error(e)
            }
        }
    }
}
