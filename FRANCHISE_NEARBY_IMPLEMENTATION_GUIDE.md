# Guia de Implementação: Busca de Unidades Próximas no Frontend Android

Este guia descreve como implementar a consulta de unidades mais próximas usando o endpoint `/franchises/nearby` na tela de onboarding do aplicativo Android.

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Estrutura de Dados](#estrutura-de-dados)
3. [Criar FranchiseService](#criar-franchiseservice)
4. [Criar Modelos de Dados](#criar-modelos-de-dados)
5. [Atualizar ViewModel](#atualizar-viewmodel)
6. [Implementar Permissão de Localização](#implementar-permissão-de-localização)
7. [Atualizar OnboardingUnitScreen](#atualizar-onboardingunitscreen)
8. [Configurar Dependency Injection](#configurar-dependency-injection)
9. [Exemplo de Uso Completo](#exemplo-de-uso-completo)

---

## Visão Geral

O endpoint `/franchises/nearby` permite buscar unidades (franquias) mais próximas filtradas por segmentação de mercado (ex: `gym`). A implementação seguirá o padrão já estabelecido no projeto usando:

- **Ktor Client** para requisições HTTP
- **Kotlin Coroutines** para operações assíncronas
- **Jetpack Compose** para a UI
- **Hilt** para Dependency Injection

---

## Estrutura de Dados

### Endpoint

```
GET /franchises/nearby?lat={latitude}&lng={longitude}&marketSegment={segmento}&radius={raio}&limit={limite}
```

### Parâmetros

- `lat` (opcional): Latitude do ponto de referência
- `lng` (opcional): Longitude do ponto de referência
- `marketSegment` (obrigatório): Tipo de segmentação (`gym`, `restaurant`, etc.)
- `radius` (opcional): Raio máximo em km (padrão: 50)
- `limit` (opcional): Número máximo de resultados (padrão: 20)

### Resposta

```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "unitId": "string",
      "name": "string",
      "owner": {
        "id": "string",
        "name": "string",
        "email": "string",
        "phone": "string"
      },
      "location": {
        "lat": -5.7793,
        "lng": -35.2009,
        "address": "string",
        "city": "string",
        "state": "string",
        "zipCode": "string",
        "type": "physical"
      },
      "status": "active",
      "type": "standard",
      "marketSegments": ["gym"],
      "distance": 5.2,
      "metrics": { ... }
    }
  ],
  "error": null
}
```

---

## Criar FranchiseService

### 1. Criar arquivo `FranchiseService.kt`

**Localização:** `shared/src/commonMain/kotlin/com/tadevolta/gym/data/remote/FranchiseService.kt`

```kotlin
package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import com.tadevolta.gym.utils.config.EnvironmentConfig

interface FranchiseService {
    suspend fun findNearby(
        lat: Double,
        lng: Double,
        marketSegment: String,
        radius: Int = 50,
        limit: Int = 20
    ): Result<List<NearbyFranchise>>
}

class FranchiseServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : FranchiseService {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override suspend fun findNearby(
        lat: Double,
        lng: Double,
        marketSegment: String,
        radius: Int,
        limit: Int
    ): Result<List<NearbyFranchise>> {
        return try {
            val response = client.get("${EnvironmentConfig.API_BASE_URL}/franchises/nearby") {
                parameter("lat", lat)
                parameter("lng", lng)
                parameter("marketSegment", marketSegment)
                parameter("radius", radius)
                parameter("limit", limit)
                headers {
                    tokenProvider()?.let { 
                        append("Authorization", "Bearer $it") 
                    }
                }
            }
            
            val apiResponse: ApiResponse<List<NearbyFranchise>> = 
                json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar unidades próximas"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

---

## Criar Modelos de Dados

### 2. Criar arquivo `FranchiseModels.kt`

**Localização:** `shared/src/commonMain/kotlin/com/tadevolta/gym/data/models/FranchiseModels.kt`

```kotlin
package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class NearbyFranchise(
    val id: String,
    val unitId: String,
    val name: String,
    val owner: FranchiseOwner,
    val location: FranchiseLocation,
    val status: String,
    val type: String,
    val marketSegments: List<String>,
    val distance: Double, // em km
    val metrics: FranchiseMetrics? = null,
    val territory: FranchiseTerritory? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class FranchiseOwner(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null
)

@Serializable
data class FranchiseLocation(
    val lat: Double,
    val lng: Double,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val type: String // "physical" ou "digital"
)

@Serializable
data class FranchiseTerritory(
    val city: String,
    val state: String,
    val exclusive: Boolean,
    val radius: Double? = null
)

@Serializable
data class FranchiseMetrics(
    val totalOrders: Int,
    val totalSales: Double,
    val totalLeads: Int,
    val conversionRate: Double,
    val averageTicket: Double,
    val customerCount: Int,
    val growthRate: Double,
    val lastMonthSales: Double,
    val lastMonthOrders: Int,
    val lastMonthLeads: Int
)

// Modelo simplificado para uso na UI
data class UnitItem(
    val id: String,
    val unitId: String,
    val name: String,
    val distance: String, // Formatado como "5.2 km"
    val address: String,
    val city: String,
    val state: String,
    val tags: List<String> = emptyList()
) {
    companion object {
        fun fromNearbyFranchise(franchise: NearbyFranchise): UnitItem {
            return UnitItem(
                id = franchise.id,
                unitId = franchise.unitId,
                name = franchise.name,
                distance = formatDistance(franchise.distance),
                address = franchise.location.address,
                city = franchise.location.city,
                state = franchise.location.state,
                tags = franchise.marketSegments
            )
        }
        
        private fun formatDistance(km: Double): String {
            return when {
                km < 1 -> "${(km * 1000).toInt()} m"
                else -> String.format("%.1f km", km)
            }
        }
    }
}
```

---

## Atualizar ViewModel

### 3. Atualizar `OnboardingViewModel.kt`

**Localização:** `androidApp/src/main/java/com/tadevolta/gym/ui/viewmodels/OnboardingViewModel.kt`

```kotlin
package com.tadevolta.gym.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tadevolta.gym.data.models.UnitItem
import com.tadevolta.gym.data.remote.FranchiseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val units: List<UnitItem> = emptyList(),
    val filteredUnits: List<UnitItem> = emptyList(),
    val searchQuery: String = "",
    val selectedUnit: UnitItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val locationPermissionGranted: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val franchiseService: FranchiseService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    init {
        // Carregar unidades próximas se tiver localização
        loadNearbyUnits()
    }
    
    fun loadNearbyUnits(
        lat: Double? = null,
        lng: Double? = null,
        marketSegment: String = "gym",
        radius: Int = 50,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val latitude = lat ?: _uiState.value.currentLatitude
            val longitude = lng ?: _uiState.value.currentLongitude
            
            if (latitude == null || longitude == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Localização não disponível"
                )
                return@launch
            }
            
            when (val result = franchiseService.findNearby(
                lat = latitude,
                lng = longitude,
                marketSegment = marketSegment,
                radius = radius,
                limit = limit
            )) {
                is Result.Success -> {
                    val unitItems = result.data.map { 
                        UnitItem.fromNearbyFranchise(it) 
                    }
                    _uiState.value = _uiState.value.copy(
                        units = unitItems,
                        filteredUnits = unitItems,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Erro ao carregar unidades"
                    )
                }
            }
        }
    }
    
    fun updateLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            currentLatitude = lat,
            currentLongitude = lng,
            locationPermissionGranted = true
        )
        // Recarregar unidades com nova localização
        loadNearbyUnits(lat, lng)
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterUnits(query)
    }
    
    private fun filterUnits(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.units
        } else {
            _uiState.value.units.filter { unit ->
                unit.name.contains(query, ignoreCase = true) ||
                unit.address.contains(query, ignoreCase = true) ||
                unit.city.contains(query, ignoreCase = true) ||
                unit.state.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(filteredUnits = filtered)
    }
    
    fun selectUnit(unit: UnitItem) {
        _uiState.value = _uiState.value.copy(selectedUnit = unit)
    }
    
    fun getFilteredUnits(): List<UnitItem> {
        return _uiState.value.filteredUnits
    }
}
```

---

## Implementar Permissão de Localização

### 4. Criar `LocationHelper.kt`

**Localização:** `androidApp/src/main/java/com/tadevolta/gym/utils/LocationHelper.kt`

```kotlin
package com.tadevolta.gym.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun requestLocationUpdates(
        onLocationUpdate: (Location) -> Unit
    ) {
        if (!hasLocationPermission()) {
            return
        }
        
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 segundos
            fastestInterval = 5000 // 5 segundos
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(onLocationUpdate)
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            ).await()
        } catch (e: Exception) {
            // Tratar erro
        }
    }
}
```

### 5. Atualizar `OnboardingUnitScreen.kt` com Permissão

```kotlin
// Adicionar no início do arquivo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.tadevolta.gym.utils.LocationHelper

@Composable
fun OnboardingUnitScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: () -> Unit = {},
    context: Context = LocalContext.current
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredUnits = viewModel.getFilteredUnits()
    
    val locationHelper = remember { LocationHelper(context) }
    
    // Launcher para permissão de localização
    val locationPermissionLauncher = rememberLaunchedForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Buscar localização atual
            LaunchedEffect(Unit) {
                val location = locationHelper.getCurrentLocation()
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                }
            }
        }
    }
    
    // Solicitar localização ao iniciar
    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            val location = locationHelper.getCurrentLocation()
            location?.let {
                viewModel.updateLocation(it.latitude, it.longitude)
            }
        }
    }
    
    // ... resto do código da UI
}
```

---

## Atualizar OnboardingUnitScreen

### 6. Atualizar o card de sugestão de localização

```kotlin
// Substituir o item do card de sugestão
item {
    if (!uiState.locationPermissionGranted) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "SUGESTÕES PRÓXIMAS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Ative a localização para melhores resultados",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MutedForegroundDark
                            )
                        )
                    }
                }
                GradientButton(
                    text = "Permitir Acesso",
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}
```

### 7. Adicionar indicador de loading e erro

```kotlin
// Adicionar após o campo de busca
item {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PurplePrimary)
        }
    }
    
    if (uiState.error != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Erro ao carregar unidades",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedForegroundDark
                        )
                    )
                }
                TextButton(onClick = { 
                    viewModel.loadNearbyUnits() 
                }) {
                    Text("Tentar novamente", color = PurplePrimary)
                }
            }
        }
    }
}
```

---

## Configurar Dependency Injection

### 8. Atualizar `AppModule.kt`

**Localização:** `androidApp/src/main/java/com/tadevolta/gym/di/AppModule.kt`

```kotlin
// Adicionar no AppModule
@Provides
@Singleton
fun provideFranchiseService(
    client: HttpClient,
    tokenStorage: SecureTokenStorage
): FranchiseService {
    return FranchiseServiceImpl(client) { 
        kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
    }
}
```

### 9. Atualizar `OnboardingViewModel` para injetar o serviço

```kotlin
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val franchiseService: FranchiseService
) : ViewModel() {
    // ... código existente
}
```

---

## Exemplo de Uso Completo

### 10. Exemplo completo da integração

```kotlin
@Composable
fun OnboardingUnitScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: () -> Unit = {},
    context: Context = LocalContext.current
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredUnits = viewModel.getFilteredUnits()
    
    val locationHelper = remember { LocationHelper(context) }
    
    // Launcher para permissão
    val locationPermissionLauncher = rememberLaunchedForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (granted) {
            LaunchedEffect(Unit) {
                locationHelper.getCurrentLocation()?.let { location ->
                    viewModel.updateLocation(location.latitude, location.longitude)
                }
            }
        }
    }
    
    // Solicitar permissão ao iniciar
    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            locationHelper.getCurrentLocation()?.let { location ->
                viewModel.updateLocation(location.latitude, location.longitude)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ... componentes existentes
            
            // Loading
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                }
            }
            
            // Erro
            if (uiState.error != null && !uiState.isLoading) {
                item {
                    // Card de erro (código acima)
                }
            }
            
            // Lista de unidades
            if (filteredUnits.isEmpty() && !uiState.isLoading) {
                item {
                    Text(
                        text = "Nenhuma unidade encontrada",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MutedForegroundDark
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredUnits) { unit ->
                    SelectableCard(
                        selected = uiState.selectedUnit?.id == unit.id,
                        onClick = { viewModel.selectUnit(unit) }
                    ) {
                        // ... código do card existente
                    }
                }
            }
        }
    }
}
```

---

## Checklist de Implementação

- [ ] Criar `FranchiseService.kt` no módulo shared
- [ ] Criar `FranchiseModels.kt` com todos os modelos de dados
- [ ] Atualizar `OnboardingViewModel.kt` com lógica de busca
- [ ] Criar `LocationHelper.kt` para gerenciar localização
- [ ] Atualizar `OnboardingUnitScreen.kt` com permissões e UI
- [ ] Adicionar `FranchiseService` no `AppModule.kt`
- [ ] Adicionar permissões no `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  ```
- [ ] Adicionar dependência do Google Play Services Location (se necessário):
  ```kotlin
  implementation("com.google.android.gms:play-services-location:21.0.1")
  ```

---

## Notas Importantes

1. **Segmentação de Mercado**: O valor padrão é `"gym"`, mas pode ser alterado conforme necessário
2. **Raio de Busca**: O padrão é 50km, mas pode ser ajustado na chamada
3. **Tratamento de Erros**: Sempre tratar casos onde a localização não está disponível
4. **Performance**: A busca é feita apenas quando a localização está disponível
5. **Cache**: Considere implementar cache local para melhorar a experiência offline

---

## Testes

Para testar a implementação:

1. **Com Localização Real**: Execute em um dispositivo físico com GPS ativado
2. **Com Localização Mock**: Use coordenadas fixas para testes:
   ```kotlin
   viewModel.loadNearbyUnits(
       lat = -5.7793,
       lng = -35.2009,
       marketSegment = "gym"
   )
   ```
3. **Sem Permissão**: Teste o fluxo quando o usuário nega a permissão

---

## Suporte

Para dúvidas ou problemas, consulte:
- Documentação da API: `/docs` (Swagger)
- Endpoint: `GET /franchises/nearby`
- Código do backend: `backend-monorepo/nodejs/apis/apps/apis-monorepo/src/modules/franchises/`
