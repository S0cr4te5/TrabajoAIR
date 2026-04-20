package com.sendaurjc.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDialog(onDismiss: () -> Unit, onReport: (String) -> Unit) {
    val options = listOf("Farola rota", "Zona sucia", "Mal aspecto")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(options.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar incidencia") },
        text = {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selected,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
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
        },
        confirmButton = { Button(onClick = { onReport(selected) }) { Text("Guardar") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}
