## Obtener Datos de Edificios y Zonas Verdes del Mapa

### Opciones Disponibles

#### 1. **Usar Overpass API (Recomendado)** ⭐
Descarga datos dinámicamente de OpenStreetMap en tiempo real.

**Ventajas:**
- ✅ Datos actualizados en tiempo real
- ✅ Cubre cualquier ubicación del mundo
- ✅ Gratuito
- ✅ Basado en OpenStreetMap

**Desventajas:**
- ❌ Requiere conexión a internet
- ❌ Puede ser lento en conexiones lentas (10-30s)

**Uso:**
```kotlin
// En tu MainViewModel o Activity
val syncManager = MapDataSyncManager(OverpassService())

// Descargar datos para la ubicación actual
viewModelScope.launch {
    syncManager.syncMapData(
        centerLat = 40.335,
        centerLon = -3.875,
        radiusKm = 0.5  // Radio de 500 metros
    )
}
```

#### 2. **Usar Datos Hardcodeados (Actual)**
Los datos vienen precargados en MockLumenSmartDataSource.

**Ventajas:**
- ✅ Instantáneo
- ✅ No requiere internet
- ✅ Perfecto para testing/simulación

**Desventajas:**
- ❌ No es dinámico
- ❌ Fijo a una ubicación

#### 3. **Usar una Base de Datos Local**
Guardar datos en SQLite y sincronizar periódicamente.

**Ventajas:**
- ✅ Rápido después del primer download
- ✅ Funciona offline
- ✅ Actualización periódica opcional

**Desventajas:**
- ❌ Requiere implementación adicional
- ❌ Más almacenamiento

### Implementación Recomendada (Combinada)

1. **Inicio**: Usar datos hardcodeados
2. **En background**: Descargar datos de Overpass
3. **Cachear**: Guardar en SQLite
4. **Fallback**: Usar cache si Overpass falla

### Datos Extraídos de Overpass

**Edificios (building=*)**
- Hospitales
- Escuelas
- Oficinas
- Residencias
- Cualquier edificio etiquetado en OSM

**Zonas Verdes**
- `leisure=park` - Parques públicos
- `leisure=garden` - Jardines
- `leisure=playground` - Áreas de juego
- `landuse=forest` - Bosques
- `landuse=grass` - Prados
- `natural=wood` - Áreas arboladas

### Ejemplo Completo

```kotlin
// En SendaApplication onCreate
val syncManager = MapDataSyncManager(OverpassService())

// En MainViewModel
fun loadMapData() {
    viewModelScope.launch {
        try {
            syncManager.syncMapData(
                centerLat = origin.value.latitude,
                centerLon = origin.value.longitude,
                radiusKm = 1.0  // 1 kilómetro de radio
            )
            // Ahora MockLumenSmartDataSource.buildings y greenZones estarán actualizados
        } catch (e: Exception) {
            Log.e("MapDataSync", "Error sincronizando datos", e)
            // Los datos hardcodeados seguirán siendo usados
        }
    }
}
```

### Limitaciones de Overpass API

- **Rate limit**: ~4 requests por segundo
- **Timeout**: 30 segundos máximo por query
- **Precisión**: Radio máximo recomendado: 2km

### Alternativas Futuras

Si necesitas más información:
1. **Mapbox API** - Datos vectoriales de alto nivel
2. **Google Maps API** - Places API (búsqueda de lugares)
3. **Tu propio servidor** - Con datos personalizados
4. **GeoJSON estático** - Archivo local actualizable

