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

package com.isaakhanimann.journal.ui.tabs.search.substance.customduration

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
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion

enum class TimeUnit(val displayName: String) {
    MINUTES("min"),
    HOURS("hours");
    
    fun toMinutes(value: Double): Double {
        return when (this) {
            MINUTES -> value
            HOURS -> value * 60
        }
    }
    
    fun fromMinutes(minutes: Double): Double {
        return when (this) {
            MINUTES -> minutes
            HOURS -> minutes / 60
        }
    }
}

fun suggestUnit(minutes: Double?): TimeUnit {
    return if ((minutes ?: 0.0) >= 90) TimeUnit.HOURS else TimeUnit.MINUTES
}

fun Double.toReadableDuration(): String {
    return when {
        this < 90 -> "${this.toInt()} min"
        this < 1440 -> {
            val hours = this / 60
            if (hours % 1.0 == 0.0) {
                "${hours.toInt()} hours"
            } else {
                String.format("%.1f hours", hours)
            }
        }
        else -> {
            val days = this / 1440
            if (days % 1.0 == 0.0) {
                "${days.toInt()} days"
            } else {
                String.format("%.1f days", days)
            }
        }
    }
}

fun SubstanceCompanion.hasCustomDurations(): Boolean {
    return onsetMin != null || onsetMax != null ||
           comeupMin != null || comeupMax != null ||
           peakMin != null || peakMax != null ||
           offsetMin != null || offsetMax != null ||
           totalMin != null || totalMax != null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDurationSheet(
    substanceName: String,
    existingCompanion: SubstanceCompanion?,
    onSave: (SubstanceCompanion) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    // State for each duration phase
    var onsetMin by remember { mutableStateOf(existingCompanion?.onsetMin?.toString() ?: "") }
    var onsetMax by remember { mutableStateOf(existingCompanion?.onsetMax?.toString() ?: "") }
    var comeupMin by remember { mutableStateOf(existingCompanion?.comeupMin?.toString() ?: "") }
    var comeupMax by remember { mutableStateOf(existingCompanion?.comeupMax?.toString() ?: "") }
    var peakMin by remember { mutableStateOf(existingCompanion?.peakMin?.toString() ?: "") }
    var peakMax by remember { mutableStateOf(existingCompanion?.peakMax?.toString() ?: "") }
    var offsetMin by remember { mutableStateOf(existingCompanion?.offsetMin?.toString() ?: "") }
    var offsetMax by remember { mutableStateOf(existingCompanion?.offsetMax?.toString() ?: "") }
    var totalMin by remember { mutableStateOf(existingCompanion?.totalMin?.toString() ?: "") }
    var totalMax by remember { mutableStateOf(existingCompanion?.totalMax?.toString() ?: "") }
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Determine units based on values
    val onsetUnit = suggestUnit(existingCompanion?.onsetMin ?: existingCompanion?.onsetMax)
    val comeupUnit = suggestUnit(existingCompanion?.comeupMin ?: existingCompanion?.comeupMax)
    val peakUnit = suggestUnit(existingCompanion?.peakMin ?: existingCompanion?.peakMax)
    val offsetUnit = suggestUnit(existingCompanion?.offsetMin ?: existingCompanion?.offsetMax)
    val totalUnit = suggestUnit(existingCompanion?.totalMin ?: existingCompanion?.totalMax)
    
    // Validation
    val isValidRange: (String, String) -> Boolean = { min, max ->
        if (min.isBlank() && max.isBlank()) {
            true // Empty is valid
        } else if (min.isBlank() || max.isBlank()) {
            false // Both or neither
        } else {
            val minVal = min.toDoubleOrNull()
            val maxVal = max.toDoubleOrNull()
            minVal != null && maxVal != null && minVal <= maxVal
        }
    }
    
    val onsetValid = isValidRange(onsetMin, onsetMax)
    val comeupValid = isValidRange(comeupMin, comeupMax)
    val peakValid = isValidRange(peakMin, peakMax)
    val offsetValid = isValidRange(offsetMin, offsetMax)
    val totalValid = isValidRange(totalMin, totalMax)
    val isFormValid = onsetValid && comeupValid && peakValid && offsetValid && totalValid
    
    // Check if at least one range is filled
    val hasContent = onsetMin.isNotBlank() || onsetMax.isNotBlank() ||
            comeupMin.isNotBlank() || comeupMax.isNotBlank() ||
            peakMin.isNotBlank() || peakMax.isNotBlank() ||
            offsetMin.isNotBlank() || offsetMax.isNotBlank() ||
            totalMin.isNotBlank() || totalMax.isNotBlank()
    
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
                text = "Custom Duration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = substanceName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set your personal duration estimates (optional). All times will be converted to minutes for storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Onset
            DurationPhaseInput(
                label = "Onset",
                min = onsetMin,
                max = onsetMax,
                onMinChange = { onsetMin = it },
                onMaxChange = { onsetMax = it },
                unit = onsetUnit,
                isValid = onsetValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Comeup
            DurationPhaseInput(
                label = "Comeup",
                min = comeupMin,
                max = comeupMax,
                onMinChange = { comeupMin = it },
                onMaxChange = { comeupMax = it },
                unit = comeupUnit,
                isValid = comeupValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Peak
            DurationPhaseInput(
                label = "Peak",
                min = peakMin,
                max = peakMax,
                onMinChange = { peakMin = it },
                onMaxChange = { peakMax = it },
                unit = peakUnit,
                isValid = peakValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Offset
            DurationPhaseInput(
                label = "Offset",
                min = offsetMin,
                max = offsetMax,
                onMinChange = { offsetMin = it },
                onMaxChange = { offsetMax = it },
                unit = offsetUnit,
                isValid = offsetValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total
            DurationPhaseInput(
                label = "Total Duration",
                min = totalMin,
                max = totalMax,
                onMinChange = { totalMin = it },
                onMaxChange = { totalMax = it },
                unit = totalUnit,
                isValid = totalValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Disclaimer
            Text(
                text = "For personal timeline estimates only. Not medical advice.",
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
                // Delete button (if editing existing with durations)
                if (onDelete != null && existingCompanion?.hasCustomDurations() == true) {
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
                        // Convert all values to minutes and create/update companion
                        val companion = (existingCompanion ?: SubstanceCompanion(
                            substanceName = substanceName,
                            color = AdaptiveColor.BLUE
                        )).copy(
                            onsetMin = onsetMin.toDoubleOrNull()?.let { onsetUnit.toMinutes(it) },
                            onsetMax = onsetMax.toDoubleOrNull()?.let { onsetUnit.toMinutes(it) },
                            comeupMin = comeupMin.toDoubleOrNull()?.let { comeupUnit.toMinutes(it) },
                            comeupMax = comeupMax.toDoubleOrNull()?.let { comeupUnit.toMinutes(it) },
                            peakMin = peakMin.toDoubleOrNull()?.let { peakUnit.toMinutes(it) },
                            peakMax = peakMax.toDoubleOrNull()?.let { peakUnit.toMinutes(it) },
                            offsetMin = offsetMin.toDoubleOrNull()?.let { offsetUnit.toMinutes(it) },
                            offsetMax = offsetMax.toDoubleOrNull()?.let { offsetUnit.toMinutes(it) },
                            totalMin = totalMin.toDoubleOrNull()?.let { totalUnit.toMinutes(it) },
                            totalMax = totalMax.toDoubleOrNull()?.let { totalUnit.toMinutes(it) }
                        )
                        onSave(companion)
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
            title = { Text("Delete Custom Durations?") },
            text = { Text("This will revert to using database defaults for timeline estimates.") },
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
private fun DurationPhaseInput(
    label: String,
    min: String,
    max: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    unit: TimeUnit,
    isValid: Boolean
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = min,
                onValueChange = onMinChange,
                label = { Text("Min") },
                suffix = { Text(unit.displayName) },
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
                suffix = { Text(unit.displayName) },
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
}
