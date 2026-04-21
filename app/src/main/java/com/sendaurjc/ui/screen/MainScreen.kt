package com.sendaurjc.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.toArgb
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

    val routeOptions by viewModel.routeOptions.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val secureSegments by viewModel.secureSegments.collectAsState()
    val unsafeSegments by viewModel.unsafeSegments.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val origin by viewModel.origin.collectAsState()
    val destination by viewModel.destination.collectAsState()
    val destinosConRuta by viewModel.destinosConRuta.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    var reportDialogOpen by remember { mutableStateOf(false) }
    var emergencyScreenOpen by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSitios by viewModel.filteredSitios.collectAsState()
    var searchActive by remember { mutableStateOf(false) }

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    var destinationWithoutRoute by remember { mutableStateOf(false) }

    LaunchedEffect(routeOptions) {
        if (routeOptions.isNotEmpty()) {
            showBottomSheet = true
            destinationWithoutRoute = false
        }
    }

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
                    routeOptions = if (selectedRoute != null) listOf(selectedRoute!!) else routeOptions,
                    incidents = incidents,
                    onMapLongPress = { viewModel.onDestinationSelected(it) },
                    mapViewRef = mapViewRef
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(bottom = if (showBottomSheet && (routeOptions.isNotEmpty() || destinationWithoutRoute)) 200.dp else 0.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            reportDialogOpen = true
                            showBottomSheet = false
                            if (selectedRoute == null) viewModel.clearRoutes()
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
                            mapViewRef.value?.controller?.setZoom(19.0)
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
                        onActiveChange = { 
                            searchActive = it 
                            if (it) {
                                showBottomSheet = false
                                destinationWithoutRoute = false
                                if (selectedRoute == null) viewModel.clearRoutes()
                            }
                        },
                        placeholder = { Text("Buscar destino...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val filteredDestinosConRuta = filteredSitios.filter { destinosConRuta.contains(it.nombre.lowercase()) }
                            
                            if (searchQuery.isNotEmpty() && filteredDestinosConRuta.isEmpty() && filteredSitios.isNotEmpty()) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "No existen rutas o no son seguras",
                                            color = Color.Red,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingContent = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) }
                                )
                            }

                            filteredSitios.forEach { sitio ->
                                val hasPredefinedRoute = destinosConRuta.contains(sitio.nombre.lowercase())
                                ListItem(
                                    headlineContent = { Text(text = sitio.nombre) },
                                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        viewModel.onDestinationSelected(sitio.coordenadas)
                                        searchActive = false
                                        viewModel.onSearchQueryChange("")
                                        if (!hasPredefinedRoute) {
                                            destinationWithoutRoute = true
                                            showBottomSheet = true
                                        }
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
                            onClick = { 
                                emergencyScreenOpen = true 
                                showBottomSheet = false
                                if (selectedRoute == null) viewModel.clearRoutes()
                            },
                            containerColor = Color(0xFFD32F2F),
                            shape = CircleShape,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Emergencia")
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showBottomSheet && (routeOptions.isNotEmpty() || destinationWithoutRoute),
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (destinationWithoutRoute) "Información" else "Rutas disponibles",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(onClick = {
                                    showBottomSheet = false
                                    destinationWithoutRoute = false
                                    if (selectedRoute == null) {
                                        viewModel.clearRoutes()
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (destinationWithoutRoute) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "No existen rutas o no son seguras",
                                        color = Color.Red,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            } else {
                                routeOptions.forEach { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                viewModel.selectRoute(option)
                                                showBottomSheet = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(24.dp),
                                                color = option.color,
                                                shape = CircleShape
                                            ) {}
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = option.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        val safetyColor = when {
                                            option.safetyIndex > 8.5 -> Color(0xFF4CAF50)
                                            option.safetyIndex >= 7.5 -> Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                                        Text(
                                            text = "Seguridad: ${option.safetyIndex}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = safetyColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (reportDialogOpen) {
            IncidentDialog(
                onDismiss = { reportDialogOpen = false },
                onReport = { type, description ->
                    viewModel.reportIncident(type, description, origin)
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
    routeOptions: List<com.sendaurjc.ui.viewmodel.RouteOption>,
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

            routeOptions.forEach { option ->
                map.overlays.add(
                    Polyline().apply {
                        setPoints(option.points)
                        color = option.color.toArgb()
                        width = 12f
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
