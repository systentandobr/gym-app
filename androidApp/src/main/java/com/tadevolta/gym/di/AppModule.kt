package com.tadevolta.gym.di

import android.content.Context
import com.tadevolta.gym.data.local.DatabaseDriverFactory
import com.tadevolta.gym.data.local.TadevoltaDatabase
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
        tokenStorage: SecureTokenStorage
    ): UserService {
        // Não injetar AuthRepository aqui para evitar dependência circular
        // O UserServiceImpl aceita authRepository como opcional
        // Se necessário, pode ser injetado posteriormente ou usado via Lazy em outro lugar
        return UserServiceImpl(
            client = client,
            tokenProvider = { 
                kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
            },
            authRepository = null // Será usado sem retry automático, mas evita ciclo de dependência
        )
    }
    
    @Provides
    @Singleton
    fun provideTrainingPlanService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): TrainingPlanService {
        return TrainingPlanServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
    }
    
    @Provides
    @Singleton
    fun provideSubscriptionService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): SubscriptionService {
        return SubscriptionServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
    }
    
    @Provides
    @Singleton
    fun provideCheckInService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): CheckInService {
        return CheckInServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
    }
    
    @Provides
    @Singleton
    fun provideGamificationService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): GamificationService {
        return GamificationServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
    }
    
    @Provides
    @Singleton
    fun provideBioimpedanceService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): BioimpedanceService {
        return BioimpedanceServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
    }
    
    @Provides
    @Singleton
    fun provideFranchiseService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): FranchiseService {
        // Token opcional - endpoint pode funcionar sem autenticação no onboarding
        return FranchiseServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
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
    fun provideGetTrainingPlanUseCase(
        repository: TrainingPlanRepository
    ): GetTrainingPlanUseCase {
        return GetTrainingPlanUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideExecuteExerciseUseCase(
        repository: TrainingPlanRepository
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
    fun provideStudentService(
        client: HttpClient,
        tokenStorage: SecureTokenStorage
    ): StudentService {
        return StudentServiceImpl(client) { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        }
    }
}
