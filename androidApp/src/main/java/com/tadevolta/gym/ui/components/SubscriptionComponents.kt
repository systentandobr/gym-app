package com.tadevolta.gym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadevolta.gym.data.models.StudentSubscription
import com.tadevolta.gym.data.models.SubscriptionPlan

@Composable
fun CurrentSubscriptionCard(subscription: StudentSubscription) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assinatura Atual",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            subscription.plan?.let { plan ->
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "R$ ${plan.price / 100.0}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: ${subscription.status.name}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SubscriptionPlanCard(plan: SubscriptionPlan) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium
            )
            plan.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "R$ ${plan.price / 100.0}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${plan.duration} dias",
                style = MaterialTheme.typography.bodySmall
            )
            if (plan.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                plan.features.forEach { feature ->
                    Text(
                        text = "• $feature",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
