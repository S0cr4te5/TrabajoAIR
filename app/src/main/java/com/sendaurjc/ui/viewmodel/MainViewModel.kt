package com.sendaurjc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendaurjc.SendaApplication
import com.sendaurjc.data.local.CampusFeature
import com.sendaurjc.data.local.IncidentEntity
import com.sendaurjc.data.local.Sitio
import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.data.network.OverpassService
import com.sendaurjc.data.repository.CampusFeatureRepository
import com.sendaurjc.data.repository.RutaPredefinidaRepository
import com.sendaurjc.data.sync.MapDataSyncManager
import com.sendaurjc.domain.RouteRepository
import com.sendaurjc.util.GeoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

class MainViewModel(
    private val app: SendaApplication,
    private val routeRepository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val dao = app.database.incidentDao()
    private val gson = Gson()
    private val featureRepository = CampusFeatureRepository(app)
    private val rutaManualRepository = RutaPredefinidaRepository(app)
    private val settingsRepository = com.sendaurjc.data.repository.SettingsRepository(app)
    private val syncManager = MapDataSyncManager(OverpassService())

    private val _isDarkMode = MutableStateFlow(settingsRepository.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _origin = MutableStateFlow(GeoPoint(40.334583, -3.876450))
    val origin: StateFlow<GeoPoint> = _origin.asStateFlow()

    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination: StateFlow<GeoPoint?> = _destination.asStateFlow()

    private val _routeOptions = MutableStateFlow<List<RouteOption>>(emptyList())
    val routeOptions: StateFlow<List<RouteOption>> = _routeOptions.asStateFlow()

    private val _selectedRoute = MutableStateFlow<RouteOption?>(null)
    val selectedRoute: StateFlow<RouteOption?> = _selectedRoute.asStateFlow()

    private val _secureSegments = MutableStateFlow<List<List<GeoPoint>>>(emptyList())
    val secureSegments: StateFlow<List<List<GeoPoint>>> = _secureSegments.asStateFlow()

    private val _unsafeSegments = MutableStateFlow<List<List<GeoPoint>>>(emptyList())
    val unsafeSegments: StateFlow<List<List<GeoPoint>>> = _unsafeSegments.asStateFlow()

    private val _sitios = MutableStateFlow<List<Sitio>>(emptyList())
    val sitios: StateFlow<List<Sitio>> = _sitios.asStateFlow()

    private val _campusFeatures = MutableStateFlow<List<CampusFeature>>(emptyList())
    val campusFeatures: StateFlow<List<CampusFeature>> = _campusFeatures.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _destinosConRuta = MutableStateFlow<Set<String>>(emptySet())
    val destinosConRuta: StateFlow<Set<String>> = _destinosConRuta.asStateFlow()

    val filteredSitios: StateFlow<List<Sitio>> = combine(_sitios, _searchQuery) { sitios, query ->
        if (query.isEmpty()) {
            sitios.sortedBy { it.nombre }
        } else {
            sitios.filter { it.nombre.contains(query, ignoreCase = true) }
                .sortedWith(compareBy<Sitio> {
                    // Primero los que empiezan por la consulta (mayor relevancia)
                    !it.nombre.startsWith(query, ignoreCase = true)
                }.thenBy { it.nombre }) // Luego por orden alfabético
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidents = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSitios()
        syncAndLoadFeatures()
        _destinosConRuta.value = rutaManualRepository.getDestinosConRuta()
    }

    private fun syncAndLoadFeatures() {
        viewModelScope.launch {
            syncManager.syncMapData(
                MockLumenSmartDataSource.CAMPUS_CENTER_LAT,
                MockLumenSmartDataSource.CAMPUS_CENTER_LON
            )
            val features = withContext(Dispatchers.IO) {
                featureRepository.loadCampusFeatures()
            }
            _campusFeatures.value = features
            MockLumenSmartDataSource.setFeatures(features)
        }
    }

    private fun loadSitios() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                try {
                    val jsonFromAssets = app.assets.open("sitios.json").bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<Sitio>>() {}.type
                    gson.fromJson<List<Sitio>>(jsonFromAssets, type) ?: emptyList<Sitio>()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            _sitios.value = list
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDestinationSelected(point: GeoPoint) {
        _destination.value = point
        _selectedRoute.value = null
        val sitioCercano = _sitios.value.find { 
            GeoUtils.distanceMeters(it.coordenadas, point) < 20.0 
        }
        calculateRoutes(point, sitioCercano?.nombre)
    }

    private fun calculateRoutes(point: GeoPoint, nombreSitio: String?) {
        viewModelScope.launch {
            try {
                val rutasManuales = nombreSitio?.let { rutaManualRepository.getRutasParaDestino(it) }
                
                val rawRoutes: List<Pair<List<GeoPoint>, Double>> = if (!rutasManuales.isNullOrEmpty()) {
                    rutasManuales
                } else {
                    emptyList()
                }

                val colors = listOf(
                    androidx.compose.ui.graphics.Color(0xFF4CAF50), // Verde
                    androidx.compose.ui.graphics.Color(0xFF2196F3), // Azul
                    androidx.compose.ui.graphics.Color(0xFFFF9800)  // Naranja
                )

                val options = rawRoutes.mapIndexed { index, pair ->
                    val points = pair.first
                    val modificador = pair.second
                    val randomSafety = 7.4 + Math.random() * (9.9 - 7.4)
                    val finalSafety = Math.max(0.0, Math.round((randomSafety - modificador) * 10.0) / 10.0)
                    
                    RouteOption(
                        id = index,
                        name = "Ruta ${index + 1}",
                        points = points,
                        safetyIndex = finalSafety,
                        color = if (index < colors.size) colors[index] else androidx.compose.ui.graphics.Color.Gray
                    )
                }.sortedByDescending { it.safetyIndex }
                .mapIndexed { index, option ->
                    option.copy(name = "Ruta ${index + 1}")
                }

                _routeOptions.value = options
                
                if (options.isNotEmpty()) {
                    segmentAndClassify(options.first().points)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _routeOptions.value = emptyList()
                _secureSegments.value = emptyList()
                _unsafeSegments.value = emptyList()
            }
        }
    }

    fun selectRoute(option: RouteOption) {
        _selectedRoute.value = option.copy(color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
        _routeOptions.value = emptyList()
        segmentAndClassify(option.points)
    }

    fun clearRoutes() {
        _routeOptions.value = emptyList()
        _selectedRoute.value = null
        _destination.value = null
        _secureSegments.value = emptyList()
        _unsafeSegments.value = emptyList()
    }

    fun reportIncident(type: String, description: String, location: GeoPoint) {
        viewModelScope.launch {
            dao.insert(IncidentEntity(type = type, description = description, lat = location.latitude, lon = location.longitude))
        }
    }

    fun deleteIncident(incident: IncidentEntity) {
        viewModelScope.launch {
            dao.deleteById(incident.id)
        }
    }

    fun updateIncident(incident: IncidentEntity, newType: String, newDescription: String) {
        viewModelScope.launch {
            dao.update(incident.copy(type = newType, description = newDescription))
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        settingsRepository.setDarkMode(enabled)
        _isDarkMode.value = enabled
    }

    private fun segmentAndClassify(route: List<GeoPoint>) {
        if (route.size < 2) {
            _secureSegments.value = emptyList()
            _unsafeSegments.value = emptyList()
            return
        }
        val secure = mutableListOf<List<GeoPoint>>()
        val unsafe = mutableListOf<List<GeoPoint>>()

        for (i in 0 until route.lastIndex) {
            val a = route[i]
            val b = route[i + 1]
            val sampleMid = GeoUtils.interpolate(a, b, 0.5)
            
            if (MockLumenSmartDataSource.isUnsafe(sampleMid)) {
                unsafe += listOf(a, b)
            } else {
                secure += listOf(a, b)
            }
        }
        _secureSegments.value = secure
        _unsafeSegments.value = unsafe
    }

    class Factory(private val app: SendaApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(app) as T
        }
    }
}

data class RouteOption(
    val id: Int,
    val name: String,
    val points: List<GeoPoint>,
    val safetyIndex: Double,
    val color: androidx.compose.ui.graphics.Color
)
