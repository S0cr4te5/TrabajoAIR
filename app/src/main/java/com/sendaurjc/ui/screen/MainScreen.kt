package com.sendaurjc.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.sendaurjc.data.local.IncidentEntity
import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.service.AlertForegroundService
import com.sendaurjc.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val secureSegments by viewModel.secureSegments.collectAsState()
    val unsafeSegments by viewModel.unsafeSegments.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val origin by viewModel.origin.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var reportDialogOpen by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val needed = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) needed += Manifest.permission.POST_NOTIFICATIONS
        launcher.launch(needed.toTypedArray())
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { reportDialogOpen = true }) { Text("Reportar") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { showCompanionDialog(context) }) { Text("Solicitar Acompañante") }
                Button(onClick = {
                    startAlertService(context)
                    scope.launch { snackbarHostState.showSnackbar("Alerta activa") }
                }) { Text("Activar Alerta") }
            }
            Text(
                text = "Contacto de confianza: Contacto URJC (666555444)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            SendaMap(
                origin = origin,
                secureSegments = secureSegments,
                unsafeSegments = unsafeSegments,
                incidents = incidents,
                onMapLongPress = { viewModel.onDestinationSelected(it) }
            )
        }
    }

    if (reportDialogOpen) {
        IncidentDialog(
            onDismiss = { reportDialogOpen = false },
            onReport = { type ->
                viewModel.reportIncident(type, origin)
                scope.launch { snackbarHostState.showSnackbar("Incidencia guardada") }
                reportDialogOpen = false
            }
        )
    }
}

private fun startAlertService(context: Context) {
    val intent = Intent(context, AlertForegroundService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

private fun showCompanionDialog(context: Context) {
    val dialog = AlertDialog.Builder(context)
        .setTitle("Buscando voluntarios URJC...")
        .setView(android.widget.ProgressBar(context))
        .setCancelable(false)
        .create()
    dialog.show()
    Handler(Looper.getMainLooper()).postDelayed({
        dialog.dismiss()
        Toast.makeText(context, "✅ Voluntario asignado (Simulación). Buen camino.", Toast.LENGTH_LONG).show()
    }, 3000)
}

@Composable
private fun SendaMap(
    origin: GeoPoint,
    secureSegments: List<List<GeoPoint>>,
    unsafeSegments: List<List<GeoPoint>>,
    incidents: List<IncidentEntity>,
    onMapLongPress: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            controller.setCenter(GeoPoint(MockLumenSmartDataSource.CAMPUS_CENTER_LAT, MockLumenSmartDataSource.CAMPUS_CENTER_LON))
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView },
        update = { map ->
            map.overlays.clear()

            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p?.let(onMapLongPress)
                    return true
                }
            })
            map.overlays.add(eventsOverlay)

            MockLumenSmartDataSource.darkZones.forEach { zone ->
                map.overlays.add(Polygon().apply {
                    fillColor = 0x44FF0000
                    strokeColor = 0xAAFF0000.toInt()
                    strokeWidth = 2f
                    points = Polygon.pointsAsCircle(zone.center, zone.radiusMeters)
                })
            }

            map.overlays.add(Marker(map).apply {
                position = origin
                title = "Tu posición (simulada)"
            })

            secureSegments.forEach { seg ->
                map.overlays.add(Polyline().apply {
                    setPoints(seg)
                    color = android.graphics.Color.GREEN
                    width = 8f
                })
            }
            unsafeSegments.forEach { seg ->
                map.overlays.add(Polyline().apply {
                    setPoints(seg)
                    color = android.graphics.Color.RED
                    width = 8f
                })
            }

            incidents.forEach { incident ->
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(incident.lat, incident.lon)
                    title = incident.type
                    icon = ContextCompat.getDrawable(context, android.R.drawable.star_big_on)
                })
            }
            map.invalidate()
        }
    )

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }
}
