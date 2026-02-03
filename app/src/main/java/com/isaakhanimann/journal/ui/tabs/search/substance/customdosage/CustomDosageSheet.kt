/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.search.substance.customdosage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage
import com.isaakhanimann.journal.data.substances.AdministrationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDosageSheet(
    substanceName: String,
    route: AdministrationRoute,
    existingDosage: SubstanceDosage?,
    unit: String,
    onSave: (SubstanceDosage) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var lightMin by remember { mutableStateOf(existingDosage?.lightMin?.toString() ?: "") }
    var lightMax by remember { mutableStateOf(existingDosage?.lightMax?.toString() ?: "") }
    var commonMin by remember { mutableStateOf(existingDosage?.commonMin?.toString() ?: "") }
    var commonMax by remember { mutableStateOf(existingDosage?.commonMax?.toString() ?: "") }
    var strongMin by remember { mutableStateOf(existingDosage?.strongMin?.toString() ?: "") }
    var strongMax by remember { mutableStateOf(existingDosage?.strongMax?.toString() ?: "") }
    var note by remember { mutableStateOf(existingDosage?.note ?: "") }
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Validation
    val isValidRange: (String, String) -> Boolean = { min, max ->
        if (min.isBlank() && max.isBlank()) {
            true // Empty is valid (optional)
        } else if (min.isBlank() || max.isBlank()) {
            false // Both or neither
        } else {
            val minVal = min.toDoubleOrNull()
            val maxVal = max.toDoubleOrNull()
            minVal != null && maxVal != null && minVal <= maxVal
        }
    }
    
    val lightValid = isValidRange(lightMin, lightMax)
    val commonValid = isValidRange(commonMin, commonMax)
    val strongValid = isValidRange(strongMin, strongMax)
    val isFormValid = lightValid && commonValid && strongValid
    
    // Check if at least one range is filled
    val hasContent = lightMin.isNotBlank() || lightMax.isNotBlank() ||
            commonMin.isNotBlank() || commonMax.isNotBlank() ||
            strongMin.isNotBlank() || strongMax.isNotBlank()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Title
            Text(
                text = "Custom Dosage",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$substanceName - ${route.displayText}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set your personal dosage ranges (optional). Leave sections empty to use database defaults.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Light range
            Text(
                text = "Light",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            DosageRangeInputs(
                min = lightMin,
                max = lightMax,
                onMinChange = { lightMin = it },
                onMaxChange = { lightMax = it },
                unit = unit,
                isValid = lightValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Common range
            Text(
                text = "Common",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            DosageRangeInputs(
                min = commonMin,
                max = commonMax,
                onMinChange = { commonMin = it },
                onMaxChange = { commonMax = it },
                unit = unit,
                isValid = commonValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Strong range
            Text(
                text = "Strong",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            DosageRangeInputs(
                min = strongMin,
                max = strongMax,
                onMinChange = { strongMin = it },
                onMaxChange = { strongMax = it },
                unit = unit,
                isValid = strongValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Note field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Disclaimer
            Text(
                text = "This is for personal tracking only. Not dosing advice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button (if editing existing)
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Cancel button
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Save button
                Button(
                    onClick = {
                        val dosage = SubstanceDosage(
                            id = existingDosage?.id ?: 0,
                            substanceName = substanceName,
                            route = route.name,
                            lightMin = lightMin.toDoubleOrNull(),
                            lightMax = lightMax.toDoubleOrNull(),
                            commonMin = commonMin.toDoubleOrNull(),
                            commonMax = commonMax.toDoubleOrNull(),
                            strongMin = strongMin.toDoubleOrNull(),
                            strongMax = strongMax.toDoubleOrNull(),
                            note = note.ifBlank { null }
                        )
                        onSave(dosage)
                    },
                    enabled = isFormValid && hasContent
                ) {
                    Text("Save")
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Custom Dosage?") },
            text = { Text("This will revert to using database defaults for this route.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DosageRangeInputs(
    min: String,
    max: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    unit: String,
    isValid: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = min,
            onValueChange = onMinChange,
            label = { Text("Min") },
            suffix = { Text(unit) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = !isValid && min.isNotBlank()
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "-",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        OutlinedTextField(
            value = max,
            onValueChange = onMaxChange,
            label = { Text("Max") },
            suffix = { Text(unit) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = !isValid && max.isNotBlank()
        )
    }
    
    if (!isValid && (min.isNotBlank() || max.isNotBlank())) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Min must be less than or equal to max",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
