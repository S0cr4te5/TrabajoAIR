package com.sendaurjc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sendaurjc.SendaApplication
import com.sendaurjc.data.local.IncidentEntity
import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.domain.RouteRepository
import com.sendaurjc.util.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MainViewModel(
    app: SendaApplication,
    private val routeRepository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val dao = app.database.incidentDao()

    private val _origin = MutableStateFlow(GeoPoint(40.335, -3.875))
    val origin: StateFlow<GeoPoint> = _origin.asStateFlow()

    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination: StateFlow<GeoPoint?> = _destination.asStateFlow()

    private val _secureSegments = MutableStateFlow<List<List<GeoPoint>>>(emptyList())
    val secureSegments: StateFlow<List<List<GeoPoint>>> = _secureSegments.asStateFlow()

    private val _unsafeSegments = MutableStateFlow<List<List<GeoPoint>>>(emptyList())
    val unsafeSegments: StateFlow<List<List<GeoPoint>>> = _unsafeSegments.asStateFlow()

    val incidents = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private fun segmentAndClassify(route: List<GeoPoint>) {
        if (route.size < 2) {
            _secureSegments.value = emptyList()
            _unsafeSegments.value = emptyList()
            return
        }
        val secure = mutableListOf<List<GeoPoint>>()
        val unsafe = mutableListOf<List<GeoPoint>>()

        for (i in 0 until route.lastIndex) {
            val start = route[i]
            val end = route[i + 1]
            val distance = GeoUtils.distanceMeters(start, end)
            val pieces = maxOf(1, (distance / 20.0).toInt())
            for (p in 0 until pieces) {
                val a = GeoUtils.interpolate(start, end, p.toDouble() / pieces)
                val b = GeoUtils.interpolate(start, end, (p + 1).toDouble() / pieces)
                val sampleMid = GeoUtils.interpolate(a, b, 0.5)
                val segment = listOf(a, b)
                if (MockLumenSmartDataSource.isUnsafe(sampleMid)) {
                    unsafe += segment
                } else {
                    secure += segment
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
