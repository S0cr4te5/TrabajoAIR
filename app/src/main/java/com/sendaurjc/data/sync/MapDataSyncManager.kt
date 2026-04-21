package com.sendaurjc.data.sync

import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.data.network.OverpassService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapDataSyncManager(private val overpassService: OverpassService) {

    suspend fun syncMapData(
        centerLat: Double,
        centerLon: Double,
        radiusKm: Double = 0.5
    ) {
        withContext(Dispatchers.Default) {
            try {
                // Descargar edificios y zonas verdes en paralelo
                val buildings = overpassService.fetchBuildings(centerLat, centerLon, radiusKm)
                val greenZones = overpassService.fetchGreenZones(centerLat, centerLon, radiusKm)

                // Actualizar el data source
                if (buildings.isNotEmpty()) {
                    MockLumenSmartDataSource.updateBuildings(buildings)
                }
                if (greenZones.isNotEmpty()) {
                    MockLumenSmartDataSource.updateGreenZones(greenZones)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, mantiene los valores por defecto
            }
        }
    }
}

