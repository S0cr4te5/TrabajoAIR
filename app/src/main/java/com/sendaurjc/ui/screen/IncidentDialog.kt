package com.sendaurjc.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
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
fun IncidentDialog(
    initialType: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onReport: (String, String) -> Unit,
    viewModel: com.sendaurjc.ui.viewmodel.MainViewModel? = null
) {
    val context = LocalContext.current
    
    fun t(key: String): String = viewModel?.getTranslation(key) ?: key

    var options by remember { mutableStateOf(listOf(t("loading"))) }
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(initialType) }
    var description by remember { mutableStateOf(initialDescription) }

    LaunchedEffect(Unit) {
        val jsonString = context.assets.open("incident_types.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("types")
        val typesList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            typesList.add(jsonArray.getString(i))
        }
        options = typesList
        if (selectedType.isEmpty() && typesList.isNotEmpty()) {
            selectedType = typesList.first()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialType.isEmpty()) t("report_incident") else t("edit_incident")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (options.isNotEmpty() && options.first() != t("loading")) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(t("type")) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            options.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    selectedType = item
                                    expanded = false
                                })
                            }
                        }
                    }
                } else {
                    Text(t("loading_incident_types"))
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(t("description")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onReport(selectedType, description) },
                enabled = selectedType.isNotEmpty() && 
                         selectedType != t("loading") && 
                         description.isNotBlank()
            ) {
                Text(t("save"))
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(t("cancel")) } }
    )
}
