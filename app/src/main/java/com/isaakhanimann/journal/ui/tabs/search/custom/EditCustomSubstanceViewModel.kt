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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.ui.main.navigation.graphs.EditCustomSubstanceRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCustomSubstanceViewModel @Inject constructor(
    val experienceRepo: ExperienceRepository,
    private val substanceRepo: com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository,
    private val dosageRepository: com.isaakhanimann.journal.data.substances.repositories.HarmReductionRepository,
    private val harmReductionReminderManager: com.isaakhanimann.journal.data.reminders.HarmReductionReminderManager,
    private val reminderScheduler: com.isaakhanimann.journal.data.reminders.ReminderScheduler,
    state: SavedStateHandle
) : ViewModel() {

    var id = 0
    var name by mutableStateOf("")
    var units by mutableStateOf("")
    var description by mutableStateOf("")

    // Advanced settings
    var hideFromCalendar by mutableStateOf(false)
    var recommendedBreakDays by mutableStateOf<Int?>(null)
    
    // Reminders
    var hydrationRemindersEnabled by mutableStateOf(false)
    var recoveryReminderEnabled by mutableStateOf(false)
    var sleepReminderEnabled by mutableStateOf(false)

    // Dosage and Duration state
    var customDosages by mutableStateOf<Map<com.isaakhanimann.journal.data.substances.AdministrationRoute, com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage>>(emptyMap())
    var substanceCompanion by mutableStateOf<com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion?>(null)

    val isValid get() = name.isNotBlank() && units.isNotBlank()

    init {
        val editCustomSubstanceRoute = state.toRoute<EditCustomSubstanceRoute>()
        val customSubstanceId = editCustomSubstanceRoute.customSubstanceId
        viewModelScope.launch {
            val customSubstance =
                experienceRepo.getCustomSubstanceFlow(customSubstanceId).firstOrNull() ?: return@launch
            id = customSubstanceId
            name = customSubstance.name
            units = customSubstance.units
            description = customSubstance.description

            // Load advanced settings
            launch {
                experienceRepo.getSubstanceCompanionFlow(name).collect { companion ->
                    substanceCompanion = companion
                    hideFromCalendar = companion?.hideFromCalendar ?: false
                    recommendedBreakDays = companion?.recommendedBreakDays
                    hydrationRemindersEnabled = companion?.hydrationRemindersEnabled ?: false
                    recoveryReminderEnabled = companion?.recoveryReminderEnabled ?: false
                    sleepReminderEnabled = companion?.sleepReminderEnabled ?: false
                }
            }

            launch {
                dosageRepository.getSubstanceDosagesFlow(name).collect { dosages ->
                    customDosages = dosages.associateBy { dosage ->
                        dosage.getRouteEnum() ?: com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL
                    }
                }
            }
        }
    }

    fun onDoneTap() {
        viewModelScope.launch {
            val customSubstance = CustomSubstance(
                id,
                name,
                units,
                description
            )
            experienceRepo.insert(customSubstance)

            // Update companion
            val currentCompanion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                substanceName = name,
                color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
            )
            val updatedCompanion = currentCompanion.copy(
                substanceName = name, // in case name changed
                hideFromCalendar = hideFromCalendar,
                recommendedBreakDays = recommendedBreakDays,
                hydrationRemindersEnabled = hydrationRemindersEnabled,
                recoveryReminderEnabled = recoveryReminderEnabled,
                sleepReminderEnabled = sleepReminderEnabled
            )
            if (substanceCompanion == null) {
                experienceRepo.insert(updatedCompanion)
            } else {
                experienceRepo.update(updatedCompanion)
            }
        }
    }

    fun toggleHideFromCalendar(hide: Boolean) {
        hideFromCalendar = hide
    }


    fun setHydrationReminders(enabled: Boolean) {
        hydrationRemindersEnabled = enabled
        if (enabled) {
            viewModelScope.launch {
                val companion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
                )
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceRepo.getSubstance(name),
                    customSubstanceName = name,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = companion.copy(hydrationRemindersEnabled = true),
                    ingestionTime = java.time.Instant.now()
                )
            }
        }
    }

    fun setRecoveryReminder(enabled: Boolean) {
        recoveryReminderEnabled = enabled
        if (enabled) {
            viewModelScope.launch {
                val companion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
                )
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceRepo.getSubstance(name),
                    customSubstanceName = name,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = companion.copy(recoveryReminderEnabled = true),
                    ingestionTime = java.time.Instant.now()
                )
            }
        }
    }

    fun setSleepReminder(enabled: Boolean) {
        sleepReminderEnabled = enabled
        if (enabled) {
            viewModelScope.launch {
                val companion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
                )
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceRepo.getSubstance(name),
                    customSubstanceName = name,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = companion.copy(sleepReminderEnabled = true),
                    ingestionTime = java.time.Instant.now()
                )
            }
        }
    }

    fun saveDosage(dosage: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage) {
        viewModelScope.launch {
            if (dosage.id == 0) {
                dosageRepository.insertDosage(dosage)
            } else {
                dosageRepository.updateDosage(dosage)
            }
        }
    }

    fun deleteDosage(dosage: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage) {
        viewModelScope.launch {
            dosageRepository.deleteDosage(dosage)
        }
    }

    fun updateSubstanceCompanion(companion: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion) {
        viewModelScope.launch {
            if (substanceCompanion != null) {
                experienceRepo.update(companion)
            } else {
                experienceRepo.insert(companion)
            }
        }
    }

    fun deleteCustomDurations() {
        viewModelScope.launch {
            val companion = substanceCompanion
            if (companion != null) {
                val cleared = companion.copy(
                    onsetMin = null,
                    onsetMax = null,
                    comeupMin = null,
                    comeupMax = null,
                    peakMin = null,
                    peakMax = null,
                    offsetMin = null,
                    offsetMax = null,
                    totalMin = null,
                    totalMax = null
                )
                experienceRepo.update(cleared)
            }
        }
    }

    fun deleteCustomSubstance() {
        viewModelScope.launch {
            experienceRepo.delete(CustomSubstance(id, name, units, description))
        }
    }
}