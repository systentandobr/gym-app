package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.StudentSubscription
import com.tadevolta.gym.data.models.SubscriptionPlan
import com.tadevolta.gym.data.remote.SubscriptionService
import com.tadevolta.gym.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionService: SubscriptionService,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _subscription = MutableStateFlow<StudentSubscription?>(null)
    val subscription: StateFlow<StudentSubscription?> = _subscription.asStateFlow()
    
    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val availablePlans: StateFlow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()
    
    init {
        loadSubscriptionData()
    }
    
    private fun loadSubscriptionData() {
        viewModelScope.launch {
            val user = when (val result = authRepository.getCurrentUser()) {
                is com.tadevolta.gym.data.models.Result.Success -> result.data
                else -> return@launch
            }
            
            // Carregar assinatura atual
            when (val result = subscriptionService.getSubscription(user.id)) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _subscription.value = result.data
                }
                else -> {}
            }
            
            // Carregar planos disponíveis
            when (val result = subscriptionService.getSubscriptionPlans()) {
                is com.tadevolta.gym.data.models.Result.Success -> {
                    _availablePlans.value = result.data
                }
                else -> {}
            }
        }
    }
}
