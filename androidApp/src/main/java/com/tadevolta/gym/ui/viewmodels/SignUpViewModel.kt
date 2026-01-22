package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.models.User
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val unitId: String? = null,
    val unitName: String? = null,
    val goal: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpSuccessful: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }
    
    fun setSelectedUnit(unitId: String, unitName: String) {
        _uiState.value = _uiState.value.copy(
            unitId = unitId,
            unitName = unitName
        )
    }
    
    fun setGoal(goal: String?) {
        _uiState.value = _uiState.value.copy(goal = goal)
    }
    
    fun signUp() {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val unitId = _uiState.value.unitId
        val unitName = _uiState.value.unitName
        
        // Validações
        when {
            name.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Nome é obrigatório")
                return
            }
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "E-mail é obrigatório")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "E-mail inválido")
                return
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Senha é obrigatória")
                return
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "Senha deve ter pelo menos 6 caracteres")
                return
            }
            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(error = "As senhas não coincidem")
                return
            }
            unitId == null || unitId.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Selecione uma unidade antes de continuar")
                return
            }
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.signUp(
                name = name,
                email = email,
                password = password,
                unitName = unitName,
                unitId = unitId
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpSuccessful = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao criar conta"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro desconhecido"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
