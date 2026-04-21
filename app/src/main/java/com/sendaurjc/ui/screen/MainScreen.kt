package com.sendaurjc.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import com.sendaurjc.data.local.IncidentEntity
import com.sendaurjc.data.local.Sitio
import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onManageIncidents: () -> Unit) {

    val secureSegments by viewModel.secureSegments.collectAsState()
    val unsafeSegments by viewModel.unsafeSegments.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val origin by viewModel.origin.collectAsState()
    val destination by viewModel.destination.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var reportDialogOpen by remember { mutableStateOf(false) }
    var emergencyScreenOpen by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSitios by viewModel.filteredSitios.collectAsState()
    var searchActive by remember { mutableStateOf(false) }

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    LaunchedEffect(Unit) {
        val needed = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        launcher.launch(needed.toTypedArray())
    }

    if (emergencyScreenOpen) {
        EmergencyScreen(
            onClose = { emergencyScreenOpen = false },
            onReportClick = { reportDialogOpen = true },
            onManageIncidentsClick = {
                emergencyScreenOpen = false
                onManageIncidents()
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(padding)
            ) {

                SendaMap(
                    origin = origin,
                    destination = destination,
                    secureSegments = secureSegments,
                    unsafeSegments = unsafeSegments,
                    incidents = incidents,
                    onMapLongPress = { viewModel.onDestinationSelected(it) },
                    mapViewRef = mapViewRef
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            reportDialogOpen = true
                        },
                        containerColor = Color(0xFFD32F2F),

                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = "Incidencia")
                    }
                    FloatingActionButton(
                        onClick = {
                            mapViewRef.value?.controller?.setCenter(origin)
                        },
                        containerColor = Color(0xFF757575),

                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Mi ubicación")
                    }


                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        onSearch = { searchActive = false },
                        active = searchActive,
                        onActiveChange = { searchActive = it },
                        placeholder = { Text("Buscar destino...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            filteredSitios.forEach { sitio ->
                                ListItem(
                                    headlineContent = { Text(sitio.nombre) },
                                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        viewModel.onDestinationSelected(sitio.coordenadas)
                                        searchActive = false
                                        viewModel.onSearchQueryChange("")
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        FloatingActionButton(
                            onClick = { emergencyScreenOpen = true },
                            containerColor = Color(0xFFD32F2F),
                            shape = CircleShape,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Emergencia")
                        }
                    }
                }
            }
        }

        if (reportDialogOpen) {
            IncidentDialog(
                onDismiss = { reportDialogOpen = false },
                onReport = { type ->
                    viewModel.reportIncident(type, origin)
                    scope.launch {
                        snackbarHostState.showSnackbar("Incidencia guardada")
                    }
                    reportDialogOpen = false
                }
            )
        }
    }
}

@Composable
private fun SendaMap(
    origin: GeoPoint,
    destination: GeoPoint?,
    secureSegments: List<List<GeoPoint>>,
    unsafeSegments: List<List<GeoPoint>>,
    incidents: List<IncidentEntity>,
    onMapLongPress: (GeoPoint) -> Unit,
    mapViewRef: MutableState<MapView?> = remember { mutableStateOf(null) }
) {

    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            controller.setZoom(17.0)
            controller.setCenter(
                GeoPoint(
                    MockLumenSmartDataSource.CAMPUS_CENTER_LAT,
                    MockLumenSmartDataSource.CAMPUS_CENTER_LON
                )
            )
        }
    }

    LaunchedEffect(mapView) {
        mapViewRef.value = mapView
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView },
        update = { map ->

            map.overlays.clear()

            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?) = false

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p?.let(onMapLongPress)
                    return true
                }
            })
            map.overlays.add(eventsOverlay)

            MockLumenSmartDataSource.darkZones.forEach { zone ->
                map.overlays.add(
                    Polygon().apply {
                        fillColor = 0x44FF0000
                        strokeColor = 0xAAFF0000.toInt()
                        strokeWidth = 2f
                        points = Polygon.pointsAsCircle(zone.center, zone.radiusMeters)
                    }
                )
            }

            map.overlays.add(
                Marker(map).apply {
                    icon = ContextCompat.getDrawable(
                        context,
                        com.sendaurjc.R.drawable.ic_location
                    )
                    position = origin
                    title = "Tu posición (simulada)"
                }
            )

            destination?.let { dest ->
                map.overlays.add(
                    Marker(map).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            com.sendaurjc.R.drawable.ic_destination
                        )
                        position = dest
                        title = "Destino seleccionado"
                    }
                )
            }

            secureSegments.forEach { seg ->
                map.overlays.add(
                    Polyline().apply {
                        setPoints(seg)
                        color = android.graphics.Color.GREEN
                        width = 8f
                    }
                )
            }

            unsafeSegments.forEach { seg ->
                map.overlays.add(
                    Polyline().apply {
                        setPoints(seg)
                        color = android.graphics.Color.RED
                        width = 8f
                    }
                )
            }

            incidents.forEach { incident ->
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(incident.lat, incident.lon)
                        title = incident.type
                        icon = ContextCompat.getDrawable(
                            context,
                            android.R.drawable.star_big_on
                        )
                    }
                )
            }

            map.invalidate()
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
}
