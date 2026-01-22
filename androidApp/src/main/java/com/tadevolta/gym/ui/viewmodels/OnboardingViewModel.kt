package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.UnitItem
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.remote.FranchiseService
import com.tadevolta.gym.data.remote.LocationRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val units: List<UnitItem> = emptyList(),
    val filteredUnits: List<UnitItem> = emptyList(),
    val searchQuery: String = "",
    val selectedUnit: UnitItem? = null,
    val selectedGoal: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val requiresLocation: Boolean = false, // Flag para indicar que precisa de localização
    val locationPermissionGranted: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val franchiseService: FranchiseService,
    private val userSessionStorage: com.tadevolta.gym.data.repositories.UserSessionStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    fun loadNearbyUnits(
        lat: Double? = null,
        lng: Double? = null,
        marketSegment: String = "gym",
        radius: Int = 50,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val latitude = lat ?: _uiState.value.currentLatitude
            val longitude = lng ?: _uiState.value.currentLongitude
            
            when (val result = franchiseService.findNearby(
                lat = latitude,
                lng = longitude,
                marketSegment = marketSegment,
                radius = radius,
                limit = limit
            )) {
                is Result.Success -> {
                    val unitItems = result.data.map { 
                        UnitItem.fromNearbyFranchise(it) 
                    }
                    _uiState.value = _uiState.value.copy(
                        units = unitItems,
                        filteredUnits = unitItems,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    val requiresLocation = result.exception is LocationRequiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao carregar unidades",
                        requiresLocation = requiresLocation
                    )
                }
                is Result.Loading -> {
                    // Já está em loading, não precisa fazer nada
                }
            }
        }
    }
    
    fun updateLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            currentLatitude = lat,
            currentLongitude = lng,
            locationPermissionGranted = true,
            error = null, // Limpar erro ao obter localização
            requiresLocation = false // Limpar flag de requer localização
        )
        // Recarregar unidades com nova localização
        loadNearbyUnits(lat, lng)
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterUnits(query)
    }
    
    private fun filterUnits(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.units
        } else {
            _uiState.value.units.filter { unit ->
                unit.name.contains(query, ignoreCase = true) ||
                unit.address.contains(query, ignoreCase = true) ||
                unit.city.contains(query, ignoreCase = true) ||
                unit.state.contains(query, ignoreCase = true) ||
                unit.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
        _uiState.value = _uiState.value.copy(filteredUnits = filtered)
    }
    
    fun selectUnit(unit: UnitItem) {
        _uiState.value = _uiState.value.copy(selectedUnit = unit)
        // Salvar no UserSessionStorage
        viewModelScope.launch {
            userSessionStorage.saveSelectedUnit(unit.unitId, unit.name)
        }
    }
    
    fun selectGoal(goal: String) {
        _uiState.value = _uiState.value.copy(selectedGoal = goal)
    }
    
    fun getFilteredUnits(): List<UnitItem> {
        return _uiState.value.filteredUnits
    }
}
