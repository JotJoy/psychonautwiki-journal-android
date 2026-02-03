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

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.search.substance.customduration.hasCustomDurations
import com.isaakhanimann.journal.ui.tabs.search.substance.customdosage.CustomDosageSheet
import com.isaakhanimann.journal.ui.tabs.search.substance.customduration.CustomDurationSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomSubstanceScreen(
    navigateBack: () -> Unit,
    initialName: String = "",
    viewModel: AddCustomSubstanceViewModel = hiltViewModel()
) {
    var showDosageSheet by remember { mutableStateOf(false) }
    var showDurationSheet by remember { mutableStateOf(false) }
    var selectedRoaForDosage by remember { mutableStateOf<com.isaakhanimann.journal.data.substances.AdministrationRoute?>(null) }

    LaunchedEffect(Unit) {
        viewModel.name = initialName
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add custom substance") })
        },
        floatingActionButton = {
            if (viewModel.isValid) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.imePadding(),
                    onClick = {
                        viewModel.addCustomSubstance {
                            navigateBack()
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Done"
                        )
                    },
                    text = { Text("Done") },
                )
            }
        }
    ) { padding ->
        AddOrEditCustomSubstanceContent(
            padding = padding,
            name = viewModel.name,
            units = viewModel.units,
            description = viewModel.description,
            onNameChange = { viewModel.name = it },
            onUnitsChange = { viewModel.units = it },
            onDescriptionChange = { viewModel.description = it },
            hideFromCalendar = viewModel.hideFromCalendar,
            onHideFromCalendarChange = viewModel::toggleHideFromCalendar,
            recommendedBreakDays = viewModel.recommendedBreakDays,
            onRecommendedBreakDaysChange = { viewModel.recommendedBreakDays = it },
            onEditDosages = { showDosageSheet = true },
            onEditDurations = { showDurationSheet = true },
            hasCustomDosages = viewModel.customDosages.isNotEmpty(),
            hasCustomDurations = viewModel.substanceCompanion?.hasCustomDurations() ?: false,
            hydrationRemindersEnabled = viewModel.hydrationRemindersEnabled,
            onHydrationRemindersChange = viewModel::setHydrationReminders,
            recoveryReminderEnabled = viewModel.recoveryReminderEnabled,
            onRecoveryReminderChange = viewModel::setRecoveryReminder,
            sleepReminderEnabled = viewModel.sleepReminderEnabled,
            onSleepReminderChange = viewModel::setSleepReminder
        )

        if (showDosageSheet) {
            selectedRoaForDosage = AdministrationRoute.ORAL
            showDosageSheet = false
        }

        if (selectedRoaForDosage != null) {
            val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            val roa = selectedRoaForDosage ?: AdministrationRoute.ORAL
            val existingDosage = viewModel.customDosages[roa]
            
            CustomDosageSheet(
                substanceName = viewModel.name,
                route = roa,
                existingDosage = existingDosage,
                unit = viewModel.units,
                onSave = { dosage ->
                    viewModel.saveDosage(dosage)
                    selectedRoaForDosage = null
                },
                onDelete = if (existingDosage != null) {
                    {
                        viewModel.deleteDosage(existingDosage)
                        selectedRoaForDosage = null
                    }
                } else null,
                onDismiss = { selectedRoaForDosage = null },
                sheetState = sheetState
            )
        }

        if (showDurationSheet) {
            val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            
            CustomDurationSheet(
                substanceName = viewModel.name,
                existingCompanion = viewModel.substanceCompanion,
                onSave = { companion ->
                    viewModel.updateSubstanceCompanion(companion)
                    showDurationSheet = false
                },
                onDelete = if (viewModel.substanceCompanion?.hasCustomDurations() == true) {
                    {
                        viewModel.deleteCustomDurations()
                        showDurationSheet = false
                    }
                } else null,
                onDismiss = { showDurationSheet = false },
                sheetState = sheetState
            )
        }
    }
}