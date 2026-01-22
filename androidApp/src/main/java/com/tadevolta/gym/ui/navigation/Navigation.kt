package com.tadevolta.gym.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tadevolta.gym.ui.screens.*
import com.tadevolta.gym.ui.theme.*

sealed class Screen(val route: String, val title: String) {
    object OnboardingUnit : Screen("onboarding/unit", "Selecionar Unidade")
    object OnboardingGoal : Screen("onboarding/goal", "Definir Objetivo")
    object OnboardingLeadDetails : Screen("onboarding/lead_details", "Informações de Lead")
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "Criar Conta")
    object ForgotPassword : Screen("forgot_password", "Recuperar Senha")
    object Dashboard : Screen("dashboard", "Dashboard")
    object TrainingPlan : Screen("training_plan/{planId}", "Plano de Treino")
    object ExerciseExecution : Screen("exercise/{planId}/{exerciseId}", "Executar Exercício")
    object CheckIn : Screen("checkin", "Check-in")
    object Ranking : Screen("ranking", "Ranking")
    object Bioimpedance : Screen("bioimpedance", "Bioimpedância")
    object Profile : Screen("profile", "Perfil")
    object Subscription : Screen("subscription", "Assinatura")
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = CardDark,
        modifier = Modifier.background(CardDark)
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.Home, 
                    contentDescription = "Início",
                    tint = if (currentRoute == Screen.Dashboard.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            label = { 
                Text(
                    "Início",
                    color = if (currentRoute == Screen.Dashboard.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { onNavigate(Screen.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.FitnessCenter, 
                    contentDescription = "Treinos",
                    tint = if (currentRoute.startsWith("training_plan") || currentRoute.startsWith("exercise")) PurplePrimary else MutedForegroundDark
                ) 
            },
            label = { 
                Text(
                    "Treinos",
                    color = if (currentRoute.startsWith("training_plan") || currentRoute.startsWith("exercise")) PurplePrimary else MutedForegroundDark
                ) 
            },
            selected = currentRoute.startsWith("training_plan") || currentRoute.startsWith("exercise"),
            onClick = { 
                // Navegar para o primeiro plano de treino ou dashboard
                onNavigate(Screen.Dashboard.route) 
            }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.BarChart, 
                    contentDescription = "Bio",
                    tint = if (currentRoute == Screen.Bioimpedance.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            label = { 
                Text(
                    "Bio",
                    color = if (currentRoute == Screen.Bioimpedance.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            selected = currentRoute == Screen.Bioimpedance.route,
            onClick = { onNavigate(Screen.Bioimpedance.route) }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.EmojiEvents, 
                    contentDescription = "Ranking",
                    tint = if (currentRoute == Screen.Ranking.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            label = { 
                Text(
                    "Ranking",
                    color = if (currentRoute == Screen.Ranking.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            selected = currentRoute == Screen.Ranking.route,
            onClick = { onNavigate(Screen.Ranking.route) }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.Person, 
                    contentDescription = "Perfil",
                    tint = if (currentRoute == Screen.Profile.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            label = { 
                Text(
                    "Perfil",
                    color = if (currentRoute == Screen.Profile.route) PurplePrimary else MutedForegroundDark
                ) 
            },
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) }
        )
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.OnboardingUnit.route,
    isAuthenticated: Boolean = false
) {
    val actualStartDestination = if (isAuthenticated) {
        Screen.Dashboard.route
    } else {
        startDestination
    }
    
    NavHost(
        navController = navController,
        startDestination = actualStartDestination
    ) {
        // Onboarding
        composable(Screen.OnboardingUnit.route) {
            OnboardingUnitScreen(
                onNext = { unitId, unitName ->
                    val encodedUnitName = java.net.URLEncoder.encode(unitName, "UTF-8")
                    navController.navigate("${Screen.OnboardingGoal.route}?unitId=${unitId}&unitName=${encodedUnitName}")
                },
                onNavigateToSignUp = { unitId, unitName ->
                    // Codificar unitName para URL
                    val encodedUnitName = java.net.URLEncoder.encode(unitName, "UTF-8")
                    navController.navigate("${Screen.SignUp.route}?unitId=${unitId}&unitName=${encodedUnitName}") {
                        popUpTo(Screen.OnboardingUnit.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = "${Screen.OnboardingGoal.route}?unitId={unitId}&unitName={unitName}",
            arguments = listOf(
                navArgument("unitId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("unitName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId")?.takeIf { it.isNotBlank() }
            val unitName = backStackEntry.arguments?.getString("unitName")?.takeIf { it.isNotBlank() }
            
            OnboardingGoalScreen(
                unitId = unitId,
                unitName = unitName,
                onNext = { finalUnitId, finalUnitName, goal ->
                    val encodedUnitName = finalUnitName?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""
                    val encodedGoal = goal?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""
                    navController.navigate("${Screen.OnboardingLeadDetails.route}?unitId=${finalUnitId ?: ""}&unitName=${encodedUnitName}&goal=${encodedGoal}")
                }
            )
        }
        
        composable(
            route = "${Screen.OnboardingLeadDetails.route}?unitId={unitId}&unitName={unitName}&goal={goal}",
            arguments = listOf(
                navArgument("unitId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("unitName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("goal") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId")?.takeIf { it.isNotBlank() }
            val unitName = backStackEntry.arguments?.getString("unitName")?.takeIf { it.isNotBlank() }
            val goal = backStackEntry.arguments?.getString("goal")?.takeIf { it.isNotBlank() }
            
            OnboardingLeadDetailsScreen(
                unitId = unitId,
                unitName = unitName,
                goal = goal,
                onStudentSelected = { finalUnitId, finalUnitName, finalGoal ->
                    val encodedUnitName = finalUnitName?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""
                    val encodedGoal = finalGoal?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""
                    navController.navigate("${Screen.SignUp.route}?unitId=${finalUnitId ?: ""}&unitName=${encodedUnitName}&goal=${encodedGoal}") {
                        popUpTo(Screen.OnboardingUnit.route) { inclusive = true }
                    }
                },
                onGymLeadSubmitted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OnboardingUnit.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Autenticação
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        
        composable(
            route = "${Screen.SignUp.route}?unitId={unitId}&unitName={unitName}&goal={goal}",
            arguments = listOf(
                navArgument("unitId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("unitName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("goal") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId")?.takeIf { it.isNotBlank() }
            val unitName = backStackEntry.arguments?.getString("unitName")?.takeIf { it.isNotBlank() }
            val goal = backStackEntry.arguments?.getString("goal")?.takeIf { it.isNotBlank() }
            SignUpScreen(
                unitId = unitId,
                unitName = unitName,
                goal = goal,
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                navToLogin = { preservedUnitId, preservedUnitName ->
                    // Voltar para Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                navToLogin = { preservedUnitId, preservedUnitName ->
                    // Voltar para Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSendLink = { email ->
                    // TODO: Implementar envio de link
                    navController.popBackStack()
                }
            )
        }
        
        // Telas principais
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
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            // TODO: Carregar exercício real baseado no ID
            // Por enquanto, criar um exercício mock
            val mockExercise = com.tadevolta.gym.data.models.Exercise(
                exerciseId = exerciseId,
                name = "Agachamento Livre",
                sets = 3,
                reps = "10-12",
                restTime = 60
            )
            ExerciseExecutionScreen(
                workoutTitle = "TREINO A - INFERIORES",
                exercise = mockExercise,
                onPrevious = {
                    navController.popBackStack()
                },
                onNext = {
                    // TODO: Navegar para próximo exercício
                    navController.popBackStack()
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CheckIn.route) {
            CheckInScreen()
        }
        
        composable(Screen.Ranking.route) {
            RankingScreen()
        }
        
        composable(Screen.Bioimpedance.route) {
            BioimpedanceScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNewMeasurementClick = {
                    // TODO: Navegar para tela de nova avaliação
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onPersonalDataClick = {
                    // TODO: Navegar para dados pessoais
                },
                onTrainingPlanClick = {
                    // TODO: Navegar para plano de treino
                },
                onPrivacyClick = {
                    // TODO: Navegar para privacidade
                },
                onLogoutClick = {
                    // TODO: Implementar logout
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Subscription.route) {
            SubscriptionScreen()
        }
    }
}
