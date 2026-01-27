package com.tadevolta.gym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.repositories.UserSessionStorage
import com.tadevolta.gym.ui.navigation.AppNavigation
import com.tadevolta.gym.ui.theme.TadevoltaGymTheme
import com.tadevolta.gym.ui.viewmodels.AuthStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TadevoltaGymTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent(
    authStateManager: AuthStateManager = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel = hiltViewModel()
) {
    val isAuthenticated by authStateManager.isAuthenticated.collectAsState()
    val onboardingCompleted by mainActivityViewModel.onboardingCompleted.collectAsState()
    
    when {
        isAuthenticated == null || onboardingCompleted == null -> {
            // Verificando autenticação ou onboarding - mostrar loading
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        isAuthenticated == true -> {
            // Usuário autenticado - ir para Dashboard
            AppNavigation(isAuthenticated = true, onboardingCompleted = onboardingCompleted ?: false)
        }
        else -> {
            // Usuário não autenticado - verificar onboarding
            AppNavigation(
                isAuthenticated = false,
                onboardingCompleted = onboardingCompleted ?: false
            )
        }
    }
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val userSessionStorage: UserSessionStorage
) : ViewModel() {
    
    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()
    
    init {
        viewModelScope.launch {
            _onboardingCompleted.value = userSessionStorage.isOnboardingCompleted()
        }
    }
}
