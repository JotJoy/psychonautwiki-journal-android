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

package com.isaakhanimann.journal.ui.tabs.search.substance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import com.isaakhanimann.journal.ui.main.navigation.graphs.SubstanceRoute
import com.isaakhanimann.journal.ui.tabs.journal.experience.TimelineDisplayOption
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.DataForOneEffectLine
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.AllTimelinesModel
import com.isaakhanimann.journal.ui.utils.getInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SubstanceViewModel @Inject constructor(
    substanceRepo: SubstanceRepository,
    private val experienceRepo: ExperienceRepository,
    private val harmReductionRepository: com.isaakhanimann.journal.data.substances.classes.harm_reduction.HarmReductionRepository,
    private val substanceRepository: SubstanceRepository,
    private val dosageRepository: com.isaakhanimann.journal.data.substances.repositories.HarmReductionRepository,
    private val harmReductionReminderManager: com.isaakhanimann.journal.data.reminders.HarmReductionReminderManager,
    private val reminderScheduler: com.isaakhanimann.journal.data.reminders.ReminderScheduler,
    state: SavedStateHandle,
) : ViewModel() {

    val substanceName = state.toRoute<SubstanceRoute>().substanceName

    val substanceWithCategories = substanceRepo.getSubstanceWithCategories(substanceName)!!

    val customUnitsFlow = experienceRepo.getUnArchivedCustomUnitsFlow(substanceName).stateIn(
        initialValue = emptyList(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val ingestionTimeFlow = MutableStateFlow(LocalDateTime.now())

    fun changeIngestionTime(newTime: LocalDateTime) {
        viewModelScope.launch {
            ingestionTimeFlow.emit(newTime)
        }
    }

    val timelineDisplayOptionFlow = ingestionTimeFlow.map { ingestionTime ->
        val substance = substanceWithCategories.substance
        val roasWithDurationsDefined = substance.roas.filter { roa ->
            val roaDuration = roa.roaDuration
            val isEveryDurationNull =
                roaDuration?.onset == null && roaDuration?.comeup == null && roaDuration?.peak == null && roaDuration?.offset == null && roaDuration?.total == null
            return@filter !isEveryDurationNull
        }
        val roasWithDosesDefined = substance.roas.filter { roa ->
            val roaDose = roa.roaDose
            val isEveryDoseNull =
                roaDose?.lightMin == null && roaDose?.commonMin == null && roaDose?.strongMin == null && roaDose?.heavyMin == null
            return@filter !isEveryDoseNull
        }
        val firstAverageCommonDose =
            roasWithDosesDefined.firstNotNullOfOrNull { it.roaDose?.averageCommonDose } ?: 100.0
        val dataForEffectLines = roasWithDurationsDefined.mapIndexed { index, roa ->
            DataForOneEffectLine(
                substanceName = "name$index",
                route = roa.route,
                roaDuration = roa.roaDuration,
                height = roa.roaDose?.getStrengthRelativeToCommonDose(firstAverageCommonDose)
                    ?.toFloat() ?: 1f,
                horizontalWeight = 0.5f,
                color = roa.route.color,
                startTime = ingestionTime.getInstant(),
                endTime = null,
            )
        }
        if (dataForEffectLines.isEmpty()) {
            return@map TimelineDisplayOption.NotWorthDrawing
        } else {
            val model = AllTimelinesModel(
                dataForLines = dataForEffectLines,
                dataForRatings = emptyList(),
                timedNotes = emptyList(),
                areSubstanceHeightsIndependent = false
            )
            return@map TimelineDisplayOption.Shown(model)
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(
            initialValue = TimelineDisplayOption.Loading,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    // Harm Reduction
    val sideEffects = harmReductionRepository.getCommonSideEffects(substanceWithCategories.substance)
    
    val hydrationRemindersEnabled = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name)
        .map { it?.hydrationRemindersEnabled ?: false }
        .stateIn(
            initialValue = false,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    val recoveryReminderEnabled = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name)
        .map { it?.recoveryReminderEnabled ?: false }
        .stateIn(
            initialValue = false,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    val sleepReminderEnabled = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name)
        .map { it?.sleepReminderEnabled ?: false }
        .stateIn(
            initialValue = false,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    
    // Calendar Display
    val hideFromCalendar = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name)
        .map { it?.hideFromCalendar ?: false }
        .stateIn(
            initialValue = false,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    
    fun toggleHideFromCalendar(hide: Boolean) {
        viewModelScope.launch {
            val existingCompanion = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name)
                .map { it }
                .stateIn(viewModelScope)
                .value
                
            if (existingCompanion != null) {
                // Update existing companion
                val updated = existingCompanion.copy(hideFromCalendar = hide)
                experienceRepo.update(updated)
            } else {
                // Create new companion with default color and hide setting
                val newCompanion = com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
                    substanceName = substanceWithCategories.substance.name,
                    color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE,
                    hideFromCalendar = hide
                )
                experienceRepo.insert(newCompanion)
            }
        }
    }
    
    fun setHydrationReminders(enabled: Boolean) {
        viewModelScope.launch {
            val companion = getOrCreateCompanion()
            val updated = companion.copy(hydrationRemindersEnabled = enabled)
            experienceRepo.update(updated)
            
            if (enabled) {
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceWithCategories.substance,
                    customSubstanceName = null,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = updated,
                    ingestionTime = Instant.now()
                )
            }
        }
    }
    
    fun setRecoveryReminder(enabled: Boolean) {
        viewModelScope.launch {
            val companion = getOrCreateCompanion()
            val updated = companion.copy(recoveryReminderEnabled = enabled)
            experienceRepo.update(updated)
            
            if (enabled) {
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceWithCategories.substance,
                    customSubstanceName = null,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = updated,
                    ingestionTime = Instant.now()
                )
            }
        }
    }
    
    fun setSleepReminder(enabled: Boolean) {
        viewModelScope.launch {
            val companion = getOrCreateCompanion()
            val updated = companion.copy(sleepReminderEnabled = enabled)
            experienceRepo.update(updated)
            
            if (enabled) {
                harmReductionReminderManager.scheduleRemindersForIngestion(
                    substance = substanceWithCategories.substance,
                    customSubstanceName = null,
                    route = com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL,
                    companion = updated,
                    ingestionTime = Instant.now()
                )
            }
        }
    }

    private suspend fun getOrCreateCompanion(): com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion {
        val existing = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name).first()
        if (existing != null) return existing
        
        val newCompanion = com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion(
            substanceName = substanceWithCategories.substance.name,
            color = com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor.BLUE
        )
        experienceRepo.insert(newCompanion)
        return newCompanion
    }
    
    
    fun getMitigationInfo(type: com.isaakhanimann.journal.data.substances.classes.harm_reduction.MitigationType) =
        harmReductionRepository.getMitigationInfo(type)
    
    
    // Custom Dosages
    
    val customDosages = dosageRepository.getSubstanceDosagesFlow(substanceWithCategories.substance.name)
        .map { dosages ->
            dosages.associateBy { dosage ->
                dosage.getRouteEnum() ?: com.isaakhanimann.journal.data.substances.AdministrationRoute.ORAL
            }
        }
        .stateIn(
            initialValue = emptyMap(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    
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
    
    // Custom Durations (via SubstanceCompanion)
    
    val substanceCompanion = experienceRepo.getSubstanceCompanionFlow(substanceWithCategories.substance.name)
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    
    fun updateSubstanceCompanion(companion: com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion) {
        viewModelScope.launch {
            val existing = substanceCompanion.value
            if (existing != null) {
                experienceRepo.update(companion)
            } else {
                experienceRepo.insert(companion)
            }
        }
    }
    
    fun deleteCustomDurations() {
        viewModelScope.launch {
            val companion = substanceCompanion.value
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
}