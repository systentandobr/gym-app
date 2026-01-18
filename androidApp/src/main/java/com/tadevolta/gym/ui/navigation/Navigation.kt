package com.tadevolta.gym.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tadevolta.gym.ui.screens.*

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object TrainingPlan : Screen("training_plan/{planId}", "Plano de Treino")
    object ExerciseExecution : Screen("exercise/{planId}/{exerciseId}", "Executar Exercício")
    object CheckIn : Screen("checkin", "Check-in")
    object Ranking : Screen("ranking", "Ranking")
    object Subscription : Screen("subscription", "Assinatura")
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { onNavigate(Screen.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Treino") },
            label = { Text("Treino") },
            selected = currentRoute.startsWith("training_plan"),
            onClick = { onNavigate(Screen.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Check-in") },
            label = { Text("Check-in") },
            selected = currentRoute == Screen.CheckIn.route,
            onClick = { onNavigate(Screen.CheckIn.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Ranking") },
            label = { Text("Ranking") },
            selected = currentRoute == Screen.Ranking.route,
            onClick = { onNavigate(Screen.Ranking.route) }
        )
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTrainingPlan = { planId ->
                    navController.navigate("training_plan/$planId")
                },
                onNavigateToCheckIn = {
                    navController.navigate(Screen.CheckIn.route)
                },
                onNavigateToRanking = {
                    navController.navigate(Screen.Ranking.route)
                }
            )
        }
        
        composable("training_plan/{planId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            TrainingPlanScreen(
                planId = planId,
                onExerciseClick = { exercise ->
                    navController.navigate("exercise/$planId/${exercise.exerciseId ?: exercise.name}")
                }
            )
        }
        
        composable("exercise/{planId}/{exerciseId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            // TODO: Implementar ExerciseExecutionScreen com parâmetros planId e exerciseId
            // Por enquanto, navegação básica - voltar para o plano de treino
            navController.popBackStack("training_plan/$planId", inclusive = false)
        }
        
        composable(Screen.CheckIn.route) {
            CheckInScreen()
        }
        
        composable(Screen.Ranking.route) {
            RankingScreen()
        }
        
        composable(Screen.Subscription.route) {
            SubscriptionScreen()
        }
    }
}
