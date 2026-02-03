/*
 * Copyright (c) 2022. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.search.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Preview
@Composable
fun AddOrEditCustomSubstanceContentPreview() {
    AddOrEditCustomSubstanceContent(
        name = "Medication",
        units = "mg",
        description = "My medication has a very long description to see how the text fits into the text field, to make sure it looks good.",
        onNameChange = {},
        onUnitsChange = {},
        onDescriptionChange = {},
        padding = PaddingValues(0.dp)
    )
}

@Composable
fun AddOrEditCustomSubstanceContent(
    padding: PaddingValues,
    name: String,
    onNameChange: (String) -> Unit,
    units: String,
    onUnitsChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    // Advanced settings
    hideFromCalendar: Boolean = false,
    onHideFromCalendarChange: (Boolean) -> Unit = {},
    recommendedBreakDays: Int? = null,
    onRecommendedBreakDaysChange: (Int?) -> Unit = {},
    // Callbacks for sheets
    onEditDosages: () -> Unit = {},
    onEditDurations: () -> Unit = {},
    // Reminders
    hydrationRemindersEnabled: Boolean = false,
    onHydrationRemindersChange: (Boolean) -> Unit = {},
    recoveryReminderEnabled: Boolean = false,
    onRecoveryReminderChange: (Boolean) -> Unit = {},
    sleepReminderEnabled: Boolean = false,
    onSleepReminderChange: (Boolean) -> Unit = {},
    // Status info
    hasCustomDosages: Boolean = false,
    hasCustomDurations: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = horizontalPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        val focusManager = LocalFocusManager.current
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = units,
            onValueChange = onUnitsChange,
            label = { Text("Units") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(onClick = { onUnitsChange("µg") }) {
                Text(text = "µg")
            }
            OutlinedButton(onClick = { onUnitsChange("mg") }) {
                Text(text = "mg")
            }
            OutlinedButton(onClick = { onUnitsChange("g") }) {
                Text(text = "g")
            }
            OutlinedButton(onClick = { onUnitsChange("mL") }) {
                Text(text = "mL")
            }
        }
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Advanced Section
        var isAdvancedExpanded by remember { mutableStateOf(false) }
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                val isBasicInfoComplete = name.isNotBlank() && units.isNotBlank()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Advanced Options",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBasicInfoComplete) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        if (!isBasicInfoComplete) {
                            Text(
                                text = "Enter name and units to enable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                    }
                    IconButton(
                        onClick = { isAdvancedExpanded = !isAdvancedExpanded },
                        enabled = isBasicInfoComplete
                    ) {
                        Icon(
                            imageVector = if (isAdvancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isAdvancedExpanded) "Collapse" else "Expand"
                        )
                    }
                }
                
                AnimatedVisibility(visible = isAdvancedExpanded) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Calendar visibility
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Hide from calendar",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Don't show experiences in the main calendar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = hideFromCalendar,
                                onCheckedChange = onHideFromCalendarChange
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Reminders
                        Text(
                            text = "Wellness Reminders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        ReminderToggleInEditor(
                            label = "Stay hydrated",
                            description = "Every hour for 6 hours",
                            checked = hydrationRemindersEnabled,
                            onCheckedChange = onHydrationRemindersChange
                        )
                        
                        ReminderToggleInEditor(
                            label = "Recovery self-care",
                            description = "Next-day reminder",
                            checked = recoveryReminderEnabled,
                            onCheckedChange = onRecoveryReminderChange
                        )
                        
                        ReminderToggleInEditor(
                            label = "Sleep preparation",
                            description = "8 hours later",
                            checked = sleepReminderEnabled,
                            onCheckedChange = onSleepReminderChange
                        )
                        
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Break Tracking
                        OutlinedTextField(
                            value = recommendedBreakDays?.toString() ?: "",
                            onValueChange = { 
                                onRecommendedBreakDaysChange(it.toIntOrNull())
                            },
                            label = { Text("Recommended break (days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Dosages
                        androidx.compose.material3.OutlinedButton(
                            onClick = onEditDosages,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = if (hasCustomDosages) "Edit Custom Dosages" else "Add Custom Dosages")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Durations
                        androidx.compose.material3.OutlinedButton(
                            onClick = onEditDurations,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = if (hasCustomDurations) "Edit Custom Durations" else "Add Custom Durations")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReminderToggleInEditor(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = label,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}