package com.sendaurjc.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sendaurjc.service.AlertForegroundService
import kotlinx.coroutines.launch

data class EmergencyContact(
    val id: String,
    val name: String,
    val email: String,
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onClose: () -> Unit, onReportClick: () -> Unit, onManageIncidentsClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedEmergencyContact by remember { mutableStateOf<String?>(null) }

    val emergencyContacts = listOf(
        EmergencyContact(
            id = "contact1",
            name = "María López García",
            email = "m.lopez.2023@alumnos.urjc.es"
        ),
        EmergencyContact(
            id = "contact2",
            name = "Carlos Rodríguez Martín",
            email = "j.rodriguez.2015@urjc.es"
        ),
        EmergencyContact(
            id = "contact3",
            name = "Ana Fernández Ruiz",
            email = "a.fernandez.2021@alumnos.urjc.es"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emergencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botones de emergencia
            Text(
                text = "Botones de Emergencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            startAlertService(context)
                            Toast.makeText(context, "🚨 Modo Alerta Activado", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                ) {
                    Text("🚨 Activar Modo Alerta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }



            Spacer(modifier = Modifier.height(8.dp))

            // Botón "Voy contigo"
            Text(
                text = "Solicitar Acompañamiento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Button(
                onClick = { showCompanionDialog(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("🤝 Voy contigo - Solicitar voluntario", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gestionar Incidencias
            Text(
                text = "Administración",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Button(
                onClick = {
                    onManageIncidentsClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF455A64)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("🛠️ Gestionar Incidencias", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contactos de emergencia
            Text(
                text = "Contactos de Emergencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            emergencyContacts.forEach { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📧 ${contact.email}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val isSelected = selectedEmergencyContact == contact.id
                        Button(
                            onClick = {
                                selectedEmergencyContact = if (isSelected) null else contact.id
                                val message = if (isSelected) {
                                    "Contacto de emergencia removido"
                                } else {
                                    "${contact.name} establecido como contacto de emergencia"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSelected) "Contacto de emergencia" else "Seleccionar como contacto de emergencia"
                            )
                        }
                    }
                }

            }
            Button(
                onClick = {
                    onReportClick()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1FA240)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("➕ Añadir Contacto de Emergencia", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }



            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "En caso de peligro inmediato, llama siempre al 112",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

        }

    }
}

private fun startAlertService(context: Context) {
    val intent = Intent(context, AlertForegroundService::class.java)
    ContextCompat.startForegroundService(context, intent)
}



private fun showCompanionDialog(context: Context) {
    val dialog = android.app.AlertDialog.Builder(context)
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
