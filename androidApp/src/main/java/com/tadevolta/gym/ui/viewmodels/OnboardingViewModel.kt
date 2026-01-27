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
    val currentLongitude: Double? = null,
    val manualAddress: String = "", // Endereço inserido manualmente
    val isGeocoding: Boolean = false, // Flag para indicar que está geocodificando
    val showManualAddressInput: Boolean = false, // Flag para mostrar campo de endereço manual
    val showContinueWithoutUnit: Boolean = false, // Flag para mostrar opção de continuar sem unidade
    val hasTriedLocationSearch: Boolean = false // Flag para indicar que já tentou buscar por localização
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
                        error = null,
                        hasTriedLocationSearch = true // Marcar que tentou buscar
                    )
                    
                    // Se não encontrou unidades após buscar por localização, mostrar campo manual
                    if (unitItems.isEmpty() && latitude != null && longitude != null) {
                        _uiState.value = _uiState.value.copy(
                            showManualAddressInput = true,
                            hasTriedLocationSearch = true
                        )
                    }
                }
                is Result.Error -> {
                    val requiresLocation = result.exception is LocationRequiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao carregar unidades",
                        requiresLocation = requiresLocation,
                        hasTriedLocationSearch = true // Marcar que tentou buscar
                    )
                    
                    // Se não é erro de localização e não encontrou unidades, mostrar campo manual
                    if (!requiresLocation && latitude != null && longitude != null) {
                        _uiState.value = _uiState.value.copy(showManualAddressInput = true)
                    }
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
            requiresLocation = false, // Limpar flag de requer localização
            hasTriedLocationSearch = false // Resetar flag antes de tentar nova busca
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
    
    fun updateManualAddress(address: String) {
        _uiState.value = _uiState.value.copy(manualAddress = address)
    }
    
    fun showManualAddressInput(show: Boolean) {
        _uiState.value = _uiState.value.copy(showManualAddressInput = show)
    }
    
    fun showContinueWithoutUnit(show: Boolean) {
        _uiState.value = _uiState.value.copy(showContinueWithoutUnit = show)
    }
    
    /**
     * Obtém os dados selecionados para sincronização com SignUpViewModel
     */
    fun getSelectedUnitId(): String? = _uiState.value.selectedUnit?.unitId
    
    fun getSelectedUnitName(): String? = _uiState.value.selectedUnit?.name
    
    fun getSelectedGoal(): String? = _uiState.value.selectedGoal
    
    /**
     * Limpa o estado do onboarding após conclusão do fluxo
     */
    fun clearOnboardingState() {
        _uiState.value = OnboardingUiState()
    }
    
    suspend fun geocodeAndSearchUnits(
        address: String,
        locationHelper: com.tadevolta.gym.utils.LocationHelper
    ) {
        _uiState.value = _uiState.value.copy(
            isGeocoding = true,
            error = null,
            manualAddress = address
        )
        
        val geocodeResult = locationHelper.geocodeAddress(address)
        
        if (geocodeResult != null) {
            // Atualizar localização e buscar unidades
            updateLocation(geocodeResult.latitude, geocodeResult.longitude)
            _uiState.value = _uiState.value.copy(isGeocoding = false)
            
            // Se ainda não encontrar unidades, mostrar opção de continuar sem unidade
            if (_uiState.value.filteredUnits.isEmpty() && !_uiState.value.isLoading) {
                _uiState.value = _uiState.value.copy(showContinueWithoutUnit = true)
            }
        } else {
            // Geocodificação falhou, mostrar opção de continuar sem unidade
            _uiState.value = _uiState.value.copy(
                isGeocoding = false,
                error = "Não foi possível localizar o endereço. Você pode continuar sem selecionar uma unidade.",
                showContinueWithoutUnit = true
            )
        }
    }
}
