package com.tadevolta.gym.di

import android.content.Context
import com.tadevolta.gym.data.local.DatabaseDriverFactory
import com.tadevolta.gym.data.local.TadevoltaDatabase
import com.tadevolta.gym.data.manager.TokenManager
import com.tadevolta.gym.data.remote.*
import com.tadevolta.gym.data.repositories.*
import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.domain.usecases.ExecuteExerciseUseCase
import com.tadevolta.gym.domain.usecases.GetTrainingPlanUseCase
import com.tadevolta.gym.utils.LocationHelper
import com.tadevolta.gym.utils.config.EnvironmentConfig
import com.tadevolta.gym.data.remote.createHttpClient
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return createHttpClient()
    }
    
    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context
    ): SecureTokenStorage {
        return SecureTokenStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideUserSessionStorage(
        @ApplicationContext context: Context
    ): UserSessionStorage {
        return SecureUserSessionStorage(context)
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        tokenStorage: SecureTokenStorage
    ): TokenManager {
        // TokenManager NÃO depende de AuthRepository para evitar dependência circular
        // O refresh é feito pelo executeWithRetry no HttpRequestHelper
        return TokenManager(tokenStorage)
    }
    
    @Provides
    @Singleton
    fun provideLocationHelper(
        @ApplicationContext context: Context
    ): LocationHelper {
        return LocationHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TadevoltaDatabase {
        val driverFactory = DatabaseDriverFactory(context)
        return TadevoltaDatabase(driverFactory.createDriver())
    }
    
    @Provides
    @Singleton
    fun provideAuthService(
        client: HttpClient
    ): AuthService {
        return AuthServiceImpl(client)
    }
    
    @Provides
    @Singleton
    fun provideUserService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): UserService {
        // UserService é usado pelo AuthRepository, então não injetamos authRepository aqui
        // Mas injetamos tokenManager para ter prevenção pró-ativa de expiração
        return UserServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = null, // Sem retry para evitar dependência circular
            tokenManager = tokenManager // Mas com prevenção pró-ativa de expiração
        )
    }
    
    @Provides
    @Singleton
    fun provideTrainingPlanService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): TrainingPlanService {
        return TrainingPlanServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideTrainingService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): TrainingService {
        return TrainingServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideSubscriptionService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): SubscriptionService {
        return SubscriptionServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideCheckInService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): CheckInService {
        return CheckInServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideGamificationService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): GamificationService {
        return GamificationServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideTeamService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): TeamService {
        return TeamServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideExerciseService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): ExerciseService {
        return ExerciseServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideBioimpedanceService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): BioimpedanceService {
        return BioimpedanceServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideFranchiseService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): FranchiseService {
        // Token opcional - endpoint pode funcionar sem autenticação no onboarding
        return FranchiseServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: AuthService,
        userService: Lazy<UserService>,
        tokenStorage: SecureTokenStorage,
        userSessionStorage: UserSessionStorage
    ): AuthRepository {
        // Usar Lazy para quebrar dependência circular com UserService
        return AuthRepository(authService, userService.get(), tokenStorage, userSessionStorage)
    }
    
    @Provides
    @Singleton
    fun provideTrainingPlanRepository(
        trainingPlanService: TrainingPlanService,
        database: TadevoltaDatabase
    ): TrainingPlanRepository {
        return TrainingPlanRepository(trainingPlanService, database)
    }
    
    @Provides
    @Singleton
    fun provideTrainingRepository(
        trainingService: TrainingService,
        database: TadevoltaDatabase
    ): TrainingRepository {
        return TrainingRepository(trainingService, database)
    }
    
    @Provides
    @Singleton
    fun provideGetTrainingPlanUseCase(
        repository: TrainingPlanRepository
    ): GetTrainingPlanUseCase {
        return GetTrainingPlanUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideExecuteExerciseUseCase(
        repository: TrainingRepository
    ): ExecuteExerciseUseCase {
        return ExecuteExerciseUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideLeadService(
        client: HttpClient
    ): LeadService {
        // Não precisa de tokenProvider pois é endpoint público
        return LeadServiceImpl(client)
    }
    
    @Provides
    @Singleton
    fun provideReferralService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): ReferralService {
        return ReferralServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideStudentService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): StudentService {
        return StudentServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
    
    @Provides
    @Singleton
    fun provideUnitOccupancyService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage,
        authRepository: Lazy<AuthRepository>,
        tokenManager: TokenManager
    ): UnitOccupancyService {
        return UnitOccupancyServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
            },
            authRepository = authRepository.get(),
            tokenManager = tokenManager
        )
    }
}
