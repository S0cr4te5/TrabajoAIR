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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.DpOffset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import android.util.Patterns
import java.util.UUID
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Switch
import androidx.compose.material3.HorizontalDivider
import com.sendaurjc.ui.viewmodel.MainViewModel
import com.sendaurjc.data.repository.EmergencyContactRepository
import com.sendaurjc.data.repository.EmergencyContact
import com.sendaurjc.service.AlertForegroundService
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onClose: () -> Unit, onReportClick: () -> Unit, onManageIncidentsClick: () -> Unit, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { EmergencyContactRepository(context) }
    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<EmergencyContact?>(null) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    LaunchedEffect(Unit) {
        emergencyContacts = repository.getContacts()
    }

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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("🚨 Activar Modo Alerta", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
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
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("🚨 Prueba de Alerta", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

            // Modo Oscuro
            Text(
                text = "Personalización",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modo Oscuro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
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
                var showMenu by remember { mutableStateOf(false) }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        onClick = {
                                            showMenu = false
                                            contactToEdit = contact
                                            showAddContactDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Borrar") },
                                        onClick = {
                                            showMenu = false
                                            repository.deleteContact(contact.id)
                                            emergencyContacts = repository.getContacts()
                                            Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📧 ${contact.email}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val isSelected = contact.isSelected
                        Button(
                            onClick = {
                                repository.updateContactSelection(contact.id, !isSelected)
                                emergencyContacts = repository.getContacts()
                                val message = if (isSelected) {
                                    "Contacto de emergencia eliminado"
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
                    showAddContactDialog = true
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

            if (showAddContactDialog) {
                var newName by remember { mutableStateOf(contactToEdit?.name ?: "") }
                var newEmail by remember { mutableStateOf(contactToEdit?.email ?: "") }
                var emailError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { 
                        showAddContactDialog = false
                        contactToEdit = null
                    },
                    title = { Text(if (contactToEdit == null) "Nuevo Contacto de Emergencia" else "Editar Contacto") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = {
                                    newEmail = it
                                    emailError = !Patterns.EMAIL_ADDRESS.matcher(it).matches()
                                },
                                label = { Text("Email") },
                                isError = emailError,
                                supportingText = {
                                    if (emailError) {
                                        Text("Email no válido", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isNotBlank() && !emailError && newEmail.isNotBlank()) {
                                    val contact = EmergencyContact(
                                        id = contactToEdit?.id ?: UUID.randomUUID().toString(),
                                        name = newName,
                                        email = newEmail,
                                        isSelected = contactToEdit?.isSelected ?: false
                                    )
                                    repository.addContact(contact)
                                    emergencyContacts = repository.getContacts()
                                    showAddContactDialog = false
                                    contactToEdit = null
                                } else {
                                    Toast.makeText(context, "Por favor, rellena los campos correctamente", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { 
                            showAddContactDialog = false
                            contactToEdit = null
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
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
