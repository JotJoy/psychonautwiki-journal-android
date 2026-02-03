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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCustomSubstanceViewModel @Inject constructor(
    val experienceRepo: ExperienceRepository,
    private val substanceRepo: com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository,
    private val dosageRepository: com.isaakhanimann.journal.data.substances.repositories.HarmReductionRepository,
    private val harmReductionReminderManager: com.isaakhanimann.journal.data.reminders.HarmReductionReminderManager,
    private val reminderScheduler: com.isaakhanimann.journal.data.reminders.ReminderScheduler,
) : ViewModel() {

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

    // Dosage and Duration state (stored in memory until final save)
    var customDosages by mutableStateOf<Map<com.isaakhanimann.journal.data.substances.AdministrationRoute, com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage>>(emptyMap())
    var substanceCompanion by mutableStateOf<com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion?>(null)

    val isValid get() = name.isNotBlank() && units.isNotBlank()

    fun toggleHideFromCalendar(hide: Boolean) {
        hideFromCalendar = hide
    }

    fun setHydrationReminders(enabled: Boolean) {
        hydrationRemindersEnabled = enabled
        if (enabled) {
            viewModelScope.launch {
                val currentCompanion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
                )
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceRepo.getSubstance(name),
                    customSubstanceName = name,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = currentCompanion.copy(hydrationRemindersEnabled = true),
                    ingestionTime = java.time.Instant.now()
                )
            }
        }
    }

    fun setRecoveryReminder(enabled: Boolean) {
        recoveryReminderEnabled = enabled
        if (enabled) {
            viewModelScope.launch {
                val currentCompanion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
                )
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceRepo.getSubstance(name),
                    customSubstanceName = name,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = currentCompanion.copy(recoveryReminderEnabled = true),
                    ingestionTime = java.time.Instant.now()
                )
            }
        }
    }

    fun setSleepReminder(enabled: Boolean) {
        sleepReminderEnabled = enabled
        if (enabled) {
            viewModelScope.launch {
                val currentCompanion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
                )
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceRepo.getSubstance(name),
                    customSubstanceName = name,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = currentCompanion.copy(sleepReminderEnabled = true),
                    ingestionTime = java.time.Instant.now()
                )
            }
        }
    }

    fun saveDosage(dosage: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage) {
        val route = dosage.getRouteEnum() ?: com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL
        customDosages = customDosages.toMutableMap().apply {
            put(route, dosage)
        }
    }

    fun deleteDosage(dosage: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage) {
        val route = dosage.getRouteEnum() ?: com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL
        customDosages = customDosages.toMutableMap().apply {
            remove(route)
        }
    }

    fun updateSubstanceCompanion(companion: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion) {
        substanceCompanion = companion
    }

    fun deleteCustomDurations() {
        substanceCompanion = substanceCompanion?.copy(
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
    }

    fun addCustomSubstance(onDone: (customSubstanceName: String) -> Unit) {
        viewModelScope.launch {
            val customSubstance = CustomSubstance(
                name = name,
                units = units,
                description = description
            )
            experienceRepo.insert(customSubstance)

            // Save companion
            val currentCompanion = substanceCompanion ?: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                substanceName = name,
                color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
            )
            val updatedCompanion = currentCompanion.copy(
                substanceName = name,
                hideFromCalendar = hideFromCalendar,
                recommendedBreakDays = recommendedBreakDays,
                hydrationRemindersEnabled = hydrationRemindersEnabled,
                recoveryReminderEnabled = recoveryReminderEnabled,
                sleepReminderEnabled = sleepReminderEnabled
            )
            experienceRepo.insert(updatedCompanion)

            // Save dosages
            customDosages.values.forEach { dosage ->
                dosageRepository.insertDosage(dosage.copy(substanceName = name))
            }

            onDone(name)
        }
    }
}