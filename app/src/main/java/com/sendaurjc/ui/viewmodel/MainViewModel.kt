package com.sendaurjc.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendaurjc.SendaApplication
import com.sendaurjc.data.local.IncidentEntity
import com.sendaurjc.data.local.Sitio
import com.sendaurjc.data.mock.MockLumenSmartDataSource
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
import java.io.File

class MainViewModel(
    private val app: SendaApplication,
    private val routeRepository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val dao = app.database.incidentDao()
    private val gson = Gson()
    private val sitiosFile = File(app.filesDir, "sitios.json")

    //private val _origin = MutableStateFlow(GeoPoint(40.334583, -3.876450))
    private val _origin = MutableStateFlow(GeoPoint(40.334583, -3.876450))
    val origin: StateFlow<GeoPoint> = _origin.asStateFlow()

    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination: StateFlow<GeoPoint?> = _destination.asStateFlow()

    private val _secureSegments = MutableStateFlow<List<List<GeoPoint>>>(emptyList())
    val secureSegments: StateFlow<List<List<GeoPoint>>> = _secureSegments.asStateFlow()

    private val _unsafeSegments = MutableStateFlow<List<List<GeoPoint>>>(emptyList())
    val unsafeSegments: StateFlow<List<List<GeoPoint>>> = _unsafeSegments.asStateFlow()

    private val _sitios = MutableStateFlow<List<Sitio>>(emptyList())
    val sitios: StateFlow<List<Sitio>> = _sitios.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSitios: StateFlow<List<Sitio>> = combine(_sitios, _searchQuery) { sitios, query ->
        if (query.isEmpty()) {
            sitios
        } else {
            sitios.filter { it.nombre.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidents = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSitios()
    }

    private fun loadSitios() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                try {
                    // Para que los cambios en assets/sitios.json se vean inmediatamente:
                    val jsonFromAssets = app.assets.open("sitios.json").bufferedReader().use { it.readText() }
                    
                    // Si el archivo local no existe, lo creamos. Si existe pero es distinto, lo actualizamos.
                    if (!sitiosFile.exists() || sitiosFile.readText() != jsonFromAssets) {
                        sitiosFile.writeText(jsonFromAssets)
                    }
                    
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

    fun addSitio(sitio: Sitio) {
        viewModelScope.launch {
            val updatedList = _sitios.value + sitio
            _sitios.value = updatedList
            saveSitios(updatedList)
        }
    }

    private suspend fun saveSitios(list: List<Sitio>) {
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(list)
                sitiosFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrigin(point: GeoPoint) {
        _origin.value = point
    }

    fun onDestinationSelected(point: GeoPoint) {
        _destination.value = point
        viewModelScope.launch {
            val route = routeRepository.requestWalkingRoute(_origin.value, point)
            segmentAndClassify(route)
        }
    }

    fun reportIncident(type: String, location: GeoPoint) {
        viewModelScope.launch {
            dao.insert(IncidentEntity(type = type, lat = location.latitude, lon = location.longitude))
        }
    }

    fun deleteIncident(incident: IncidentEntity) {
        viewModelScope.launch {
            dao.deleteById(incident.id)
        }
    }

    fun updateIncident(incident: IncidentEntity, newType: String) {
        viewModelScope.launch {
            dao.updateType(incident.id, newType)
        }
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
            val sStart = route[i]
            val sEnd = route[i + 1]
            
            val distance = GeoUtils.distanceMeters(sStart, sEnd)
            val pieces = if (distance > 30) (distance / 20.0).toInt().coerceAtLeast(1) else 1
            
            for (p in 0 until pieces) {
                // Forzamos el inicio y fin exactos de cada segmento para evitar derivas
                val a = if (p == 0) sStart else GeoUtils.interpolate(sStart, sEnd, p.toDouble() / pieces)
                val b = if (p == pieces - 1) sEnd else GeoUtils.interpolate(sStart, sEnd, (p + 1).toDouble() / pieces)
                
                val sampleMid = GeoUtils.interpolate(a, b, 0.5)
                if (MockLumenSmartDataSource.isUnsafe(sampleMid)) {
                    unsafe += listOf(a, b)
                } else {
                    secure += listOf(a, b)
                }
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
