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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.ui.screens.*
import com.tadevolta.gym.ui.theme.*
import com.tadevolta.gym.ui.viewmodels.ProfileViewModel
import com.tadevolta.gym.ui.viewmodels.TrainingPlanViewModel
import com.tadevolta.gym.ui.viewmodels.OnboardingSharedViewModel
import com.tadevolta.gym.ui.viewmodels.ExerciseExecutionViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder

sealed class Screen(val route: String, val title: String) {
    object OnboardingUnit : Screen("onboarding/unit", "Selecionar Academia")
    object OnboardingGoal : Screen("onboarding/goal", "Definir Objetivo")
    object OnboardingGender : Screen("onboarding/gender", "Escolher Perfil")
    object OnboardingLeadDetails : Screen("onboarding/lead_details", "Informações de Lead")
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "Criar Conta")
    object ForgotPassword : Screen("forgot_password", "Recuperar Senha")
    object Dashboard : Screen("dashboard", "Dashboard")
    object TrainingPlan : Screen("training_plan/{planId}", "Plano de Treino")
    object TrainingPlans : Screen("training_plans/{studentId}", "Meus Treinos")
    object ExerciseExecution : Screen("exercise/{planId}/{exerciseId}", "Executar Exercício")
    object CheckIn : Screen("checkin", "Check-in")
    object Ranking : Screen("ranking", "Ranking")
    object Bioimpedance : Screen("bioimpedance", "Bioimpedância")
    object Profile : Screen("profile", "Perfil")
    object PersonalData : Screen("profile/personal_data", "Dados Pessoais")
    object Privacy : Screen("profile/privacy", "Privacidade")
    object Achievements : Screen("profile/achievements", "Conquistas")
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
    isAuthenticated: Boolean = false,
    onboardingCompleted: Boolean = false
) {
    val actualStartDestination = when {
        isAuthenticated -> {
            Screen.Dashboard.route
        }
        onboardingCompleted -> {
            // Se onboarding foi completado mas usuário não está autenticado, ir para Login
            Screen.Login.route
        }
        else -> {
            // Se onboarding não foi completado, ir para OnboardingUnit
            Screen.OnboardingUnit.route
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = actualStartDestination
    ) {
        composable(Screen.OnboardingUnit.route) {
            val sharedViewModel: OnboardingSharedViewModel = hiltViewModel()
            OnboardingUnitScreen(
                viewModel = sharedViewModel,
                onNext = {
                    val uiState = sharedViewModel.uiState.value
                    // Verificar se veio de "Indique sua Academia"
                    if (uiState.showContinueWithoutUnit) {
                        navController.navigate(Screen.OnboardingLeadDetails.route)                    
                    } else {
                        navController.navigate(Screen.OnboardingGoal.route)
                    } 
                },
                onNavigateToSignUp = {
                    
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OnboardingUnit.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.OnboardingGoal.route) {
            val parentEntry = remember {
                navController.getBackStackEntry(Screen.OnboardingUnit.route)
            }
            OnboardingGoalScreen(
                viewModel = hiltViewModel<OnboardingSharedViewModel>(parentEntry),
                onNext = {
                    navController.navigate(Screen.OnboardingGender.route)
                }
            )
        }
        
        composable(Screen.OnboardingGender.route) {
            val parentEntry = remember {
                navController.getBackStackEntry(Screen.OnboardingUnit.route)
            }
            OnboardingGenderScreen(
                viewModel = hiltViewModel<OnboardingSharedViewModel>(parentEntry),
                onNext = {
                    navController.navigate(Screen.OnboardingLeadDetails.route)
                }
            )
        }
        
        composable(Screen.OnboardingLeadDetails.route) {
            val parentEntry = remember {
                navController.getBackStackEntry(Screen.OnboardingUnit.route)
            }
            OnboardingLeadDetailsScreen(
                viewModel = hiltViewModel<OnboardingSharedViewModel>(parentEntry),
                onStudentSelected = {
                    navController.navigate(Screen.SignUp.route)
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
        
        composable(Screen.SignUp.route) {
            val parentEntry = remember {
                try {
                    navController.getBackStackEntry(Screen.OnboardingUnit.route)
                } catch (e: Exception) {
                    null
                }
            }
            SignUpScreen(
                viewModel = parentEntry?.let { hiltViewModel<OnboardingSharedViewModel>(it) } ?: hiltViewModel<OnboardingSharedViewModel>(),
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                navToLogin = {
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
                onSendLinkSuccess = {
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
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToTrainingPlans = { studentId ->
                    navController.navigate("training_plans/$studentId")
                }
            )
        }
        
        composable("training_plan/{planId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            // Extrair dayOfWeek de savedStateHandle se disponível
            val dayOfWeek = backStackEntry.savedStateHandle.get<Int>("dayOfWeek")
            TrainingScreen(
                planId = planId,
                dayOfWeek = dayOfWeek,
                onBackClick = { navController.popBackStack() },
                onExerciseClick = { exercise ->
                    val exerciseRoute = "exercise/$planId/${exercise.exerciseId ?: exercise.name}"
                    navController.navigate(exerciseRoute) {
                        launchSingleTop = true
                    }
                    // Passar dayOfWeek via savedStateHandle após navegação
                    if (dayOfWeek != null) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(100)
                            try {
                                val entry = navController.getBackStackEntry(exerciseRoute)
                                entry.savedStateHandle["dayOfWeek"] = dayOfWeek
                            } catch (e: Exception) {
                                // Se não conseguir acessar, tentar novamente após mais tempo
                                kotlinx.coroutines.delay(200)
                                try {
                                    val entry = navController.getBackStackEntry(exerciseRoute)
                                    entry.savedStateHandle["dayOfWeek"] = dayOfWeek
                                } catch (e2: Exception) {
                                    // Ignorar erro se ainda não conseguir
                                }
                            }
                        }
                    }
                }
            )
        }
        
        composable("training_plans/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            TrainingPlanScreen(
                studentId = studentId,
                onDayClick = { planId, dayOfWeek ->
                    // Navegar para o plano com dayOfWeek
                    navController.navigate("training_plan/$planId") {
                        launchSingleTop = true
                    }
                    // Passar dayOfWeek via savedStateHandle
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(100)
                        try {
                            val entry = navController.getBackStackEntry("training_plan/$planId")
                            entry.savedStateHandle["dayOfWeek"] = dayOfWeek
                        } catch (e: Exception) {
                            // Ignorar erro se a entrada não existir ainda
                        }
                    }
                }
            )
        }
        
        composable("exercise/{planId}/{exerciseId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            // Obter dayOfWeek do savedStateHandle se disponível
            val dayOfWeek = backStackEntry.savedStateHandle.get<Int>("dayOfWeek")
            
            // Passar dayOfWeek para o ViewModel via savedStateHandle
            if (dayOfWeek != null) {
                backStackEntry.savedStateHandle["dayOfWeek"] = dayOfWeek
            }
            
            val exerciseViewModel: ExerciseExecutionViewModel = hiltViewModel()
            
            ExerciseExecutionScreen(
                viewModel = exerciseViewModel,
                onPrevious = {
                    val previousExerciseId = exerciseViewModel.navigateToPrevious()
                    if (previousExerciseId != null) {
                        navController.navigate("exercise/$planId/$previousExerciseId") {
                            popUpTo("exercise/$planId/$exerciseId") { inclusive = true }
                            // Manter dayOfWeek na navegação
                            if (dayOfWeek != null) {
                                launchSingleTop = true
                            }
                        }
                        // Passar dayOfWeek para o próximo exercício
                        if (dayOfWeek != null) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(50)
                                try {
                                    val entry = navController.getBackStackEntry("exercise/$planId/$previousExerciseId")
                                    entry.savedStateHandle["dayOfWeek"] = dayOfWeek
                                } catch (e: Exception) {
                                    // Ignorar erro
                                }
                            }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onNext = {
                    val nextExerciseId = exerciseViewModel.navigateToNext()
                    if (nextExerciseId != null) {
                        navController.navigate("exercise/$planId/$nextExerciseId") {
                            popUpTo("exercise/$planId/$exerciseId") { inclusive = true }
                            // Manter dayOfWeek na navegação
                            if (dayOfWeek != null) {
                                launchSingleTop = true
                            }
                        }
                        // Passar dayOfWeek para o próximo exercício
                        if (dayOfWeek != null) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(50)
                                try {
                                    val entry = navController.getBackStackEntry("exercise/$planId/$nextExerciseId")
                                    entry.savedStateHandle["dayOfWeek"] = dayOfWeek
                                } catch (e: Exception) {
                                    // Ignorar erro
                                }
                            }
                        }
                    } else {
                        // Último exercício, voltar para o plano
                        navController.navigate("training_plan/$planId") {
                            popUpTo("exercise/$planId/$exerciseId") { inclusive = true }
                            // Manter dayOfWeek ao voltar
                            if (dayOfWeek != null) {
                                launchSingleTop = true
                            }
                        }
                        // Passar dayOfWeek de volta para o plano
                        if (dayOfWeek != null) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(50)
                                try {
                                    val entry = navController.getBackStackEntry("training_plan/$planId")
                                    entry.savedStateHandle["dayOfWeek"] = dayOfWeek
                                } catch (e: Exception) {
                                    // Ignorar erro
                                }
                            }
                        }
                    }
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CheckIn.route) {
            CheckInScreen(
                onNavigateToTrainingPlan = { studentId ->
                    navController.navigate("training_plans/$studentId")
                }
            )
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
                    // Por enquanto, pode abrir um modal ou navegar para uma tela de nova avaliação
                    // TODO: Criar tela de nova avaliação de bioimpedância se necessário
                }
            )
        }
        
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            
            ProfileScreen(
                viewModel = profileViewModel,
                onPersonalDataClick = {
                    navController.navigate(Screen.PersonalData.route)
                },
                onTrainingPlanClick = { studentId ->
                    if (studentId != null) {
                        navController.navigate("training_plans/$studentId")
                    }
                },
                onPrivacyClick = {
                    navController.navigate(Screen.Privacy.route)
                },
                onAchievementsClick = {
                    navController.navigate(Screen.Achievements.route)
                },
                onLogoutClick = {
                    // Implementar logout
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        profileViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Screen.PersonalData.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            
            PersonalDataScreen(
                viewModel = profileViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Achievements.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            
            AchievementsScreen(
                viewModel = profileViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Subscription.route) {
            SubscriptionScreen()
        }
    }
}
