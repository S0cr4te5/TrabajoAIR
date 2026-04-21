package com.sendaurjc.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDialog(onDismiss: () -> Unit, onReport: (String) -> Unit) {
    val context = LocalContext.current
    var options by remember { mutableStateOf(listOf("Cargando...")) }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val jsonString = context.assets.open("incident_types.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("types")
        val typesList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            typesList.add(jsonArray.getString(i))
        }
        options = typesList
        if (typesList.isNotEmpty()) {
            selected = typesList.first()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar incidencia") },
        text = {
            if (selected.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selected,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = {
                                selected = item
                                expanded = false
                            })
                        }
                    }
                }
            } else {
                Text("Cargando tipos de incidencia...")
            }
        },
        confirmButton = {
            Button(
                onClick = { onReport(selected) },
                enabled = selected.isNotEmpty() && selected != "Cargando..."
            ) {
                Text("Guardar")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}
