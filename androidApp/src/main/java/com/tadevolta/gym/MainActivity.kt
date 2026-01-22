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
import com.tadevolta.gym.ui.navigation.AppNavigation
import com.tadevolta.gym.ui.theme.TadevoltaGymTheme
import com.tadevolta.gym.ui.viewmodels.AuthStateManager
import dagger.hilt.android.AndroidEntryPoint

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
    authStateManager: AuthStateManager = hiltViewModel()
) {
    val isAuthenticated by authStateManager.isAuthenticated.collectAsState()
    
    when (isAuthenticated) {
        null -> {
            // Verificando autenticação - mostrar loading
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        true -> {
            // Usuário autenticado - ir para Dashboard
            AppNavigation(isAuthenticated = true)
        }
        false -> {
            // Usuário não autenticado - ir para Onboarding
            AppNavigation(isAuthenticated = false)
        }
    }
}
