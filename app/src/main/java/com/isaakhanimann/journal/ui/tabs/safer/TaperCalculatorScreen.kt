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

package com.isaakhanimann.journal.ui.tabs.safer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaperCalculatorScreen(
    navigateBack: () -> Unit,
    viewModel: TaperViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    val calculatedPlan by viewModel.calculatedPlan.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val activeSteps by viewModel.activeSteps.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taper Calculator") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activePlan != null) {
                // --- ACTIVE PLAN VIEW ---
                 item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = "Active Taper Plan: ${activePlan?.substanceName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Reduction: ${activePlan?.reductionPercentage}% every ${activePlan?.daysPerStep} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Progress List
                item {
                    Text("Progress", style = MaterialTheme.typography.titleMedium)
                }
                
                items(activeSteps) { step ->
                    Card(
                        colors = if (step.isComplete) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) else CardDefaults.cardColors()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Step ${step.stepNumber}: ${step.dose} ${activePlan?.units}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (step.isComplete) Color.Gray else MaterialTheme.colorScheme.onSurface
                                )
                                if (step.isComplete && step.completionDate != null) {
                                     Text(
                                        "Completed: ${step.completionDate.atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM"))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                } else {
                                     // Show expected date?
                                     // For simplicity, just show status
                                     Text(
                                        "Pending",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Checkbox(
                                checked = step.isComplete,
                                onCheckedChange = { isChecked ->
                                    viewModel.markStepComplete(step, isChecked)
                                }
                            )
                        }
                    }
                }
                
                item {
                     var showDeleteDialog by remember { mutableStateOf(false) }
                     if (showDeleteDialog) {
                         AlertDialog(
                             onDismissRequest = { showDeleteDialog = false },
                             title = { Text("Archive Plan?") },
                             text = { Text("This will archive the current plan and allow you to create a new one. History is preserved in the database.") },
                             confirmButton = {
                                 TextButton(onClick = {
                                     viewModel.archiveCurrentPlan()
                                     showDeleteDialog = false
                                 }) { Text("Archive") }
                             },
                             dismissButton = {
                                 TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                             }
                         )
                     }
                     Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Archive Plan & Start Over")
                    }
                }

            } else {
                // --- CREATE NEW PLAN VIEW (Existing UI) ---
            
            // Disclaimer
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "This tool is for informational planning only. It is not medical advice. Always consult a medical professional before starting a tapering schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Inputs
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Settings", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = config.substanceName,
                            onValueChange = { viewModel.updateConfig(config.copy(substanceName = it)) },
                            label = { Text("Substance Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = if (config.startingDose == 0.0) "" else config.startingDose.toString(),
                                onValueChange = { 
                                    viewModel.updateConfig(config.copy(startingDose = it.toDoubleOrNull() ?: 0.0)) 
                                },
                                label = { Text("Start Dose") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = config.units,
                                onValueChange = { viewModel.updateConfig(config.copy(units = it)) },
                                label = { Text("Units") },
                                modifier = Modifier.width(80.dp)
                            )
                        }

                        Column {
                            Text("Reduction per Step: ${config.reductionPercentage.toInt()}%")
                            Slider(
                                value = config.reductionPercentage.toFloat(),
                                onValueChange = { viewModel.updateConfig(config.copy(reductionPercentage = it.toDouble())) },
                                valueRange = 1f..50f,
                                steps = 49
                            )
                        }
                        
                         Column {
                            Text("Days per Step: ${config.daysPerStep}")
                            Slider(
                                value = config.daysPerStep.toFloat(),
                                onValueChange = { viewModel.updateConfig(config.copy(daysPerStep = it.toInt())) },
                                valueRange = 1f..31f,
                                steps = 30
                            )
                        }
                    }
                }
            }
            
            // Graph Visualization
            if (calculatedPlan.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Column(Modifier.padding(16.dp)) {
                             Text("Projected Schedule", style = MaterialTheme.typography.titleSmall)
                             Spacer(modifier = Modifier.height(8.dp))
                             Row(
                                 modifier = Modifier.weight(1f).fillMaxWidth(),
                                 verticalAlignment = Alignment.Bottom,
                                 horizontalArrangement = Arrangement.SpaceBetween // Distribute bars
                             ) {
                                 val maxDose = config.startingDose
                                 calculatedPlan.forEach { step ->
                                     // Simple Bar
                                     Box(
                                         modifier = Modifier
                                             .weight(1f)
                                             .fillMaxWidth(0.8f) // Gap
                                             .height((150.dp * (step.dose / maxDose).toFloat())) 
                                             .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                             .background(MaterialTheme.colorScheme.primary)
                                     )
                                 }
                             }
                        }
                    }
                }
            }

            // List Results
            items(calculatedPlan) { step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${step.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Step ${step.stepNumber}",
                         style = MaterialTheme.typography.bodySmall,
                         color = Color.Gray
                    )
                    Text(
                        "${step.dose} ${config.units}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Save Button
            if (calculatedPlan.isNotEmpty()) {
                item {
                    Button(
                        onClick = { viewModel.saveCurrentPlan() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Taper Plan")
                    }
                }
            }
            } // End else (Create Mode)
                item {
                    Button(
                        onClick = { 
                            scope.launch { viewModel.scheduleRemindersForPlan() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Schedule Reminder for Next Step")
                }
            }

        }
    }
}
