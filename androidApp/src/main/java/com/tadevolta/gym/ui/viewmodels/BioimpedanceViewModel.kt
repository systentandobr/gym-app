package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.BioimpedanceMeasurement
import com.tadevolta.gym.data.models.BioimpedanceProgress
import com.tadevolta.gym.data.models.DataPoint
import com.tadevolta.gym.data.remote.BioimpedanceService
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BioimpedanceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bioimpedanceService: BioimpedanceService
) : ViewModel() {
    
    private val _measurements = MutableStateFlow<List<BioimpedanceMeasurement>>(emptyList())
    val measurements: StateFlow<List<BioimpedanceMeasurement>> = _measurements.asStateFlow()
    
    private val _progressData = MutableStateFlow<BioimpedanceProgress?>(null)
    val progressData: StateFlow<BioimpedanceProgress?> = _progressData.asStateFlow()
    
    init {
        loadBioimpedanceData()
    }
    
    private fun loadBioimpedanceData() {
        viewModelScope.launch {
            val user = when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> result.data
                else -> return@launch
            }
            
            // Carregar histórico
            when (val result = bioimpedanceService.getHistory(user.id)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _measurements.value = result.data.measurements
                }
                else -> {}
            }
            
            // Carregar progresso
            when (val result = bioimpedanceService.getProgress(user.id, "6 meses")) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _progressData.value = result.data
                }
                else -> {}
            }
        }
    }
}
