package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.ExecutedSet
import com.tadevolta.gym.data.models.Exercise
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.models.TrainingPlan
import com.tadevolta.gym.data.models.TrainingExecution
import com.tadevolta.gym.data.remote.ExerciseService
import com.tadevolta.gym.data.repositories.TrainingPlanRepository
import com.tadevolta.gym.data.repositories.TrainingRepository
import com.tadevolta.gym.utils.ImageUrlBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseExecutionUiState(
    val workoutTitle: String = "TREINO",
    val workoutSubtitle: String = "",
    val exercise: Exercise? = null,
    val exercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = -1,
    val executedSets: List<ExecutedSet> = emptyList(),
    val nextExercises: List<Exercise> = emptyList(),
    val totalExercises: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExecutionRunning: Boolean = false,
    val totalExecutionTimeSeconds: Long = 0,
    val restTimeRemaining: Int? = null
)

@HiltViewModel
class ExerciseExecutionViewModel @Inject constructor(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val trainingRepository: TrainingRepository,
    private val exerciseService: ExerciseService,
    private val appStateManager: AppStateManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val planId: String = savedStateHandle.get<String>("planId") ?: ""
    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: ""
    private val dayOfWeek: Int? = savedStateHandle.get<Int>("dayOfWeek")
    
    private var currentTrainingExecutionId: String? = null
    
    private val _uiState = MutableStateFlow(ExerciseExecutionUiState())
    val uiState: StateFlow<ExerciseExecutionUiState> = _uiState.asStateFlow()
    
    private val _exerciseDetails = MutableStateFlow<Exercise?>(null)
    val exerciseDetails: StateFlow<Exercise?> = _exerciseDetails.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _isFinishing = MutableStateFlow(false)
    val isFinishing: StateFlow<Boolean> = _isFinishing.asStateFlow()
    
    private var executionTimerJob: Job? = null
    private var restTimerJob: Job? = null
    
    init {
        loadExerciseData()
        
        // Observar flag global de refresh
        viewModelScope.launch {
            appStateManager.needsDataRefresh.collect { needsRefresh ->
                if (needsRefresh) {
                    loadExerciseData(forceRefresh = true)
                    appStateManager.clearRefreshFlag()
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        executionTimerJob?.cancel()
        restTimerJob?.cancel()
    }
    
    private fun loadExerciseData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Verificar se precisa atualizar baseado em estado global
            val needsRefresh = appStateManager.needsDataRefresh.value || forceRefresh
            
            // Buscar ou criar TrainingExecution ativa
            val trainingExecutionResult = trainingRepository.getActiveTrainingExecution()
            val trainingExecution = when (trainingExecutionResult) {
                is Result.Success -> trainingExecutionResult.data
                else -> null
            }
            
            // Se não há TrainingExecution ativa para este plano, criar uma
            if (trainingExecution == null || trainingExecution.trainingPlanId != planId) {
                when (val createResult = trainingRepository.createTrainingExecution(planId)) {
                    is Result.Success -> {
                        currentTrainingExecutionId = createResult.data.id
                        // Carregar executedSets da nova TrainingExecution
                        loadExecutedSetsFromTraining(createResult.data)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Erro ao iniciar execução de treino: ${createResult.exception.message}"
                        )
                        return@launch
                    }
                    else -> {}
                }
            } else {
                currentTrainingExecutionId = trainingExecution.id
                // Carregar executedSets da TrainingExecution existente
                loadExecutedSetsFromTraining(trainingExecution)
            }
            
            // Carregar plano de treino
            when (val result = trainingPlanRepository.getTrainingPlanById(
                planId,
                forceRefresh = forceRefresh,
                needsRefresh = needsRefresh
            )) {
                is Result.Success -> {
                    val plan = result.data
                    
                    // Buscar exercícios de weeklySchedule se dayOfWeek foi fornecido
                    val exercises = if (dayOfWeek != null) {
                        plan.weeklySchedule.find { it.dayOfWeek == dayOfWeek }?.exercises 
                            ?: emptyList()
                    } else {
                        // Fallback: usar plan.exercises se weeklySchedule estiver vazio
                        plan.exercises.ifEmpty {
                            plan.weeklySchedule.flatMap { it.exercises }
                        }
                    }
                    
                    val exercise = exercises.find { 
                        it.exerciseId == exerciseId || it.name == exerciseId 
                    }
                    val currentIndex = exercises.indexOfFirst { 
                        it.exerciseId == exerciseId || it.name == exerciseId 
                    }
                    
                    // Carregar detalhes do exercício do catálogo se tiver exerciseId
                    // Isso garante que as imagens sejam sempre buscadas do catálogo atualizado
                    exercise?.exerciseId?.let { exId ->
                        loadExerciseDetails(exId)
                    } ?: exercise?.name?.let { exerciseName ->
                        // Fallback temporário: tentar buscar por nome quando exerciseId não está disponível
                        // Nota: Isso só funcionará se o backend implementar busca por nome
                        // A solução ideal é o backend preservar exerciseId ao carregar templates
                        android.util.Log.w("ExerciseExecutionViewModel", 
                            "exerciseId não disponível para exercício '$exerciseName'. Tentando buscar por nome...")
                        loadExerciseDetailsByName(exerciseName)
                    }
                    
                    // Carregar executedSets da TrainingExecution se existir
                    val executedSets = if (currentTrainingExecutionId != null) {
                        val trainingExec = trainingRepository.getTrainingExecutionById(currentTrainingExecutionId!!)
                        when (trainingExec) {
                            is Result.Success -> {
                                val exerciseExec = trainingExec.data.exercises.find { 
                                    it.exerciseId == exerciseId || it.name == exerciseId 
                                }
                                exerciseExec?.executedSets ?: initializeEmptySets(exercise)
                            }
                            else -> initializeEmptySets(exercise)
                        }
                    } else {
                        initializeEmptySets(exercise)
                    }
                    
                    // Determinar subtítulo do treino baseado no dayOfWeek
                    val workoutSubtitle = if (dayOfWeek != null) {
                        val dayNames = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
                        val dayName = dayNames.getOrElse(dayOfWeek) { "Dia $dayOfWeek" }
                        val timeSlot = plan.weeklySchedule.find { it.dayOfWeek == dayOfWeek }
                            ?.timeSlots?.firstOrNull()?.activity
                        if (timeSlot != null) {
                            "$dayName - $timeSlot"
                        } else {
                            "$dayName - ${plan.name}"
                        }
                    } else {
                        plan.name
                    }
                    
                    // Obter próximos exercícios (após o atual)
                    val nextExercises = if (currentIndex >= 0 && currentIndex < exercises.size - 1) {
                        exercises.subList(currentIndex + 1, exercises.size)
                    } else {
                        emptyList()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        workoutTitle = "Treino Ativo",
                        workoutSubtitle = workoutSubtitle,
                        exercise = exercise,
                        exercises = exercises,
                        currentExerciseIndex = currentIndex,
                        executedSets = executedSets,
                        nextExercises = nextExercises,
                        totalExercises = exercises.size,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao carregar exercício"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    /**
     * Força refresh dos dados do exercício.
     * Chamado quando usuário faz pull-to-refresh.
     */
    fun refreshExerciseData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadExerciseData(forceRefresh = true)
            _isRefreshing.value = false
        }
    }
    
    fun navigateToExercise(newExerciseId: String) {
        savedStateHandle["exerciseId"] = newExerciseId
        loadExerciseData()
    }
    
    fun navigateToPrevious(): String? {
        val currentIndex = _uiState.value.currentExerciseIndex
        val exercises = _uiState.value.exercises
        
        if (currentIndex > 0) {
            val previousExercise = exercises[currentIndex - 1]
            return previousExercise.exerciseId ?: previousExercise.name
        }
        return null
    }
    
    fun navigateToNext(): String? {
        val currentIndex = _uiState.value.currentExerciseIndex
        val exercises = _uiState.value.exercises
        
        if (currentIndex >= 0 && currentIndex < exercises.size - 1) {
            val nextExercise = exercises[currentIndex + 1]
            return nextExercise.exerciseId ?: nextExercise.name
        }
        return null
    }
    
    fun completeSet(set: ExecutedSet) {
        val updatedSets = _uiState.value.executedSets.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(completed = true)
            _uiState.value = _uiState.value.copy(executedSets = updatedSets)
        }
    }
    
    fun updateWeight(set: ExecutedSet, weight: Double) {
        val updatedSets = _uiState.value.executedSets.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(executedWeight = weight)
            _uiState.value = _uiState.value.copy(executedSets = updatedSets)
        }
    }
    
    fun updateReps(set: ExecutedSet, reps: Int) {
        val updatedSets = _uiState.value.executedSets.toMutableList()
        val index = updatedSets.indexOfFirst { it.setNumber == set.setNumber }
        if (index >= 0) {
            updatedSets[index] = set.copy(executedReps = reps)
            _uiState.value = _uiState.value.copy(executedSets = updatedSets)
        }
    }
    
    private fun initializeEmptySets(exercise: Exercise?): List<ExecutedSet> {
        val initialSets = exercise?.sets ?: 3
        return (1..initialSets).map { setNumber ->
            ExecutedSet(
                setNumber = setNumber,
                plannedReps = exercise?.reps ?: "",
                executedReps = null,
                plannedWeight = exercise?.weight,
                executedWeight = null,
                completed = false,
                timestamp = null
            )
        }
    }
    
    private suspend fun loadExecutedSetsFromTraining(trainingExecution: TrainingExecution) {
        val exerciseExec = trainingExecution.exercises.find { 
            it.exerciseId == exerciseId || it.name == exerciseId 
        }
        if (exerciseExec != null) {
            _uiState.value = _uiState.value.copy(executedSets = exerciseExec.executedSets)
        }
    }
    
    fun finishExercise() {
        viewModelScope.launch {
            _isFinishing.value = true
            
            val trainingId = currentTrainingExecutionId
            if (trainingId == null) {
                _uiState.value = _uiState.value.copy(error = "Erro: Execução de treino não encontrada")
                _isFinishing.value = false
                return@launch
            }
            
            when (val result = trainingRepository.updateExerciseExecution(
                trainingId = trainingId,
                exerciseId = exerciseId,
                executedSets = _uiState.value.executedSets
            )) {
                is Result.Success -> {
                    // Verificar se todos os exercícios do treino estão completos
                    val updatedTraining = result.data
                    val allExercisesCompleted = checkAllExercisesCompleted(updatedTraining)
                    
                    if (allExercisesCompleted) {
                        // Completar TrainingExecution
                        trainingRepository.completeTrainingExecution(
                            trainingId = trainingId,
                            totalDurationSeconds = _uiState.value.totalExecutionTimeSeconds.toInt()
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Erro ao salvar execução: ${result.exception.message}")
                }
                else -> {
                    _uiState.value = _uiState.value.copy(error = "Erro desconhecido ao salvar execução")
                }
            }
            
            _isFinishing.value = false
        }
    }
    
    private fun checkAllExercisesCompleted(trainingExecution: TrainingExecution): Boolean {
        // Buscar o plano para saber quantos exercícios são esperados
        // Por enquanto, assumir que se todos os exercícios na TrainingExecution têm executedSets completos, está completo
        return trainingExecution.exercises.isNotEmpty() &&
               trainingExecution.exercises.all { exerciseExec ->
                   exerciseExec.executedSets.isNotEmpty() &&
                   exerciseExec.executedSets.all { it.completed }
               }
    }
    
    fun startExecutionTimer() {
        if (_uiState.value.isExecutionRunning) return
        
        _uiState.value = _uiState.value.copy(isExecutionRunning = true)
        
        executionTimerJob = viewModelScope.launch {
            while (_uiState.value.isExecutionRunning) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    totalExecutionTimeSeconds = _uiState.value.totalExecutionTimeSeconds + 1
                )
            }
        }
    }
    
    fun pauseExecutionTimer() {
        _uiState.value = _uiState.value.copy(isExecutionRunning = false)
        executionTimerJob?.cancel()
        executionTimerJob = null
    }
    
    fun startRestTimer() {
        val exercise = _uiState.value.exercise ?: return
        val executedSets = _uiState.value.executedSets
        
        // Verificar se há pelo menos uma série completa (permite descanso após cada série)
        val hasCompletedSet = executedSets.any { it.completed == true }
        
        if (!hasCompletedSet) return
        
        // Verificar se todas as séries planejadas estão completas
        val allSetsCompleted = (0 until exercise.sets).all { index ->
            executedSets.getOrNull(index)?.completed == true
        }
        
        val restTime = exercise.restTime ?: 60
        _uiState.value = _uiState.value.copy(restTimeRemaining = restTime)
        
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            var remaining = restTime
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(restTimeRemaining = remaining)
            }
            
            // Timer finalizado
            _uiState.value = _uiState.value.copy(restTimeRemaining = null)
            
            // Se todas as séries estiverem completas, finalizar exercício
            // Caso contrário, apenas continuar para próxima série
            if (allSetsCompleted) {
                finishExercise()
            }
        }
    }
    
    fun cancelRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        _uiState.value = _uiState.value.copy(restTimeRemaining = null)
    }
    
    fun addExtraSet() {
        val exercise = _uiState.value.exercise ?: return
        val currentSets = _uiState.value.executedSets
        val nextSetNumber = currentSets.size + 1
        
        val extraSet = ExecutedSet(
            setNumber = nextSetNumber,
            plannedReps = exercise.reps,
            executedReps = null,
            plannedWeight = exercise.weight,
            executedWeight = null,
            completed = false,
            timestamp = null
        )
        
        _uiState.value = _uiState.value.copy(
            executedSets = currentSets + extraSet
        )
    }
    
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    /**
     * Carrega detalhes do exercício por nome (fallback temporário).
     * Nota: Este método pode não funcionar se o backend não suportar busca por nome.
     * A solução ideal é o backend retornar exerciseId no plano de treino.
     * 
     * @param exerciseName Nome do exercício a ser buscado
     */
    private fun loadExerciseDetailsByName(exerciseName: String) {
        if (exerciseName.isBlank()) {
            android.util.Log.w("ExerciseExecutionViewModel", "Nome do exercício é vazio")
            return
        }
        
        android.util.Log.d("ExerciseExecutionViewModel", "Tentando buscar exercício por nome: $exerciseName")
        
        viewModelScope.launch {
            when (val result = exerciseService.searchExerciseByName(exerciseName)) {
                is Result.Success -> {
                    val exercise = result.data
                    
                    // Construir URLs completas das imagens
                    val exerciseWithFullUrls = exercise.copy(
                        images = exercise.images?.let { ImageUrlBuilder.buildImageUrls(it) },
                        imageUrl = ImageUrlBuilder.buildImageUrl(exercise.imageUrl),
                        videoUrl = ImageUrlBuilder.buildImageUrl(exercise.videoUrl)
                    )
                    
                    _exerciseDetails.value = exerciseWithFullUrls
                    
                    // Atualizar também o exercício no UI state se for o mesmo
                    val currentExercise = _uiState.value.exercise
                    if (currentExercise?.name == exerciseName) {
                        _uiState.value = _uiState.value.copy(exercise = exerciseWithFullUrls)
                    }
                }
                is Result.Error -> {
                    // Em caso de erro, manter exercício inicial sem imagens (fallback)
                    android.util.Log.w("ExerciseExecutionViewModel", 
                        "Não foi possível buscar exercício '$exerciseName' por nome. " +
                        "O backend deve retornar exerciseId no plano de treino. " +
                        "Erro: ${result.exception.message}")
                    // Não atualizar _exerciseDetails para manter null e usar fallback (ícone padrão)
                }
                is Result.Loading -> {
                    // Estado de carregamento - não fazer nada
                }
            }
        }
    }
    
    /**
     * Carrega detalhes completos do exercício do catálogo de forma lazy.
     * Busca o exercício via ExerciseService e constrói URLs completas das imagens.
     * 
     * @param exerciseId ID do exercício a ser buscado
     */
    fun loadExerciseDetails(exerciseId: String?) {
        if (exerciseId == null || exerciseId.isBlank()) {
            android.util.Log.w("ExerciseExecutionViewModel", "exerciseId é null ou vazio")
            return
        }
        
        android.util.Log.d("ExerciseExecutionViewModel", "Carregando detalhes do exercício: $exerciseId")
        
        viewModelScope.launch {
            when (val result = exerciseService.getExercise(exerciseId)) {
                is Result.Success -> {
                    val exercise = result.data
                    
                    // Construir URLs completas das imagens
                    val exerciseWithFullUrls = exercise.copy(
                        images = exercise.images?.let { ImageUrlBuilder.buildImageUrls(it) },
                        imageUrl = ImageUrlBuilder.buildImageUrl(exercise.imageUrl),
                        videoUrl = ImageUrlBuilder.buildImageUrl(exercise.videoUrl)
                    )
                    
                    _exerciseDetails.value = exerciseWithFullUrls
                    
                    // Atualizar também o exercício no UI state se for o mesmo
                    val currentExercise = _uiState.value.exercise
                    if (currentExercise?.exerciseId == exerciseId || currentExercise?.name == exerciseId) {
                        _uiState.value = _uiState.value.copy(exercise = exerciseWithFullUrls)
                    }
                }
                is Result.Error -> {
                    // Em caso de erro, manter exercício inicial (fallback)
                    // Não atualizar _exerciseDetails para manter null e usar fallback
                    android.util.Log.w("ExerciseExecutionViewModel", 
                        "Erro ao carregar detalhes do exercício $exerciseId: ${result.exception.message}")
                }
                is Result.Loading -> {
                    // Estado de carregamento - não fazer nada, manter exercício inicial
                }
            }
        }
    }
}
