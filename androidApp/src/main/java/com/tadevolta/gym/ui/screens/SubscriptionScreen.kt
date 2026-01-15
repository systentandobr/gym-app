package com.tadevolta.gym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tadevolta.gym.ui.components.*
import com.tadevolta.gym.ui.viewmodels.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val subscription by viewModel.subscription.collectAsState()
    val plans by viewModel.availablePlans.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Assinatura") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            subscription?.let { sub ->
                item {
                    CurrentSubscriptionCard(subscription = sub)
                }
            }
            
            item {
                Text(
                    text = "Planos Disponíveis",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            plans.forEach { plan ->
                item {
                    SubscriptionPlanCard(plan = plan)
                }
            }
        }
    }
}
