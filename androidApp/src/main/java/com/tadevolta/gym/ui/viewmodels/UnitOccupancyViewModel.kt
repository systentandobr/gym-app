package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.*
import com.tadevolta.gym.data.remote.UnitOccupancyService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnitOccupancyUiState(
    val occupancyData: UnitOccupancyResponse? = null,
    val selectedDayOfWeek: Int? = null, // 0=Domingo, 1=Segunda, etc.
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UnitOccupancyViewModel @Inject constructor(
    private val unitOccupancyService: UnitOccupancyService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UnitOccupancyUiState())
    val uiState: StateFlow<UnitOccupancyUiState> = _uiState.asStateFlow()
    
    private var refreshJob: Job? = null
    
    fun loadOccupancyData(unitId: String, dayOfWeek: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = unitOccupancyService.getUnitOccupancy(unitId, dayOfWeek)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        occupancyData = result.data,
                        selectedDayOfWeek = dayOfWeek ?: result.data.dayOfWeek,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Erro ao carregar dados de ocupação",
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
    
    fun selectDayOfWeek(unitId: String, dayOfWeek: Int) {
        _uiState.value = _uiState.value.copy(selectedDayOfWeek = dayOfWeek)
        loadOccupancyData(unitId, dayOfWeek)
    }
    
    fun startPeriodicRefresh(unitId: String, intervalMinutes: Long = 5) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(intervalMinutes * 60 * 1000) // Converter minutos para milissegundos
                loadOccupancyData(unitId, _uiState.value.selectedDayOfWeek)
            }
        }
    }
    
    fun stopPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPeriodicRefresh()
    }
}
