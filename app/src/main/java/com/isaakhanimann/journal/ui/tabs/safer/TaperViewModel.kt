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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt
import com.isaakhanimann.journal.data.room.experiences.TaperRepository
import com.isaakhanimann.journal.data.room.experiences.entities.TaperPlan
import com.isaakhanimann.journal.data.room.experiences.entities.TaperStepEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest

@HiltViewModel
class TaperViewModel @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
    private val taperRepository: TaperRepository
) : ViewModel() {

    private val _config = MutableStateFlow(TaperConfig())
    val config: StateFlow<TaperConfig> = _config.asStateFlow()

    // Transient plan (calculated but not saved)
    private val _calculatedPlan = MutableStateFlow<List<TaperStep>>(emptyList())
    val calculatedPlan: StateFlow<List<TaperStep>> = _calculatedPlan.asStateFlow()

    // Persisted plan (loaded from DB)
    val activePlan: StateFlow<TaperPlan?> = taperRepository.activePlanFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSteps: StateFlow<List<TaperStepEntity>> = activePlan
        .map { plan ->
             if (plan != null) {
                 taperRepository.getStepsForPlan(plan.id)
             } else {
                 kotlinx.coroutines.flow.flowOf(emptyList())
             }
        }
        .flatMapLatest { it } // Flatten the flow of flow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateConfig(newConfig: TaperConfig) {
        _config.value = newConfig
        calculateTaper(newConfig)
    }

    fun saveCurrentPlan() {
        val currentConfig = _config.value
        val steps = _calculatedPlan.value
        if (steps.isEmpty()) return

        val newPlan = TaperPlan(
            substanceName = currentConfig.substanceName,
            startingDose = currentConfig.startingDose,
            units = currentConfig.units,
            reductionPercentage = currentConfig.reductionPercentage,
            daysPerStep = currentConfig.daysPerStep
        )
        
        val entities = steps.map { step ->
             TaperStepEntity(
                 planId = 0, // Will be set by repo
                 stepNumber = step.stepNumber,
                 dose = step.dose,
                 isComplete = false
             )
        }

        viewModelScope.launch {
            taperRepository.saveNewPlan(newPlan, entities)
        }
    }

    fun markStepComplete(step: TaperStepEntity, isComplete: Boolean) {
        viewModelScope.launch {
            taperRepository.updateStep(step.copy(isComplete = isComplete, completionDate = if(isComplete) Instant.now() else null))
        }
    }

    fun archiveCurrentPlan() {
         val plan = activePlan.value ?: return
         viewModelScope.launch {
             taperRepository.archivePlan(plan.id)
         }
    }

    private fun calculateTaper(config: TaperConfig) {
        if (config.startingDose <= 0 || config.reductionPercentage <= 0 || config.daysPerStep <= 0) {
            _calculatedPlan.value = emptyList()
            return
        }

        val steps = mutableListOf<TaperStep>()
        var currentDose = config.startingDose
        var currentDate = LocalDate.now()
        var stepNumber = 1

        // Safety cap: Prevent infinite loops if reduction is near zero
        // Also stop if dose is negligible (< 1% of start or < 0.01)
        val minDose = config.startingDose * 0.01

        while (currentDose > minDose && stepNumber < 52) { // Cap at ~1 year of weekly usage to be safe
            steps.add(
                TaperStep(
                    stepNumber = stepNumber,
                    date = currentDate,
                    dose = (currentDose * 100.0).roundToInt() / 100.0 // Round to 2 decimals
                )
            )

            currentDose -= (currentDose * (config.reductionPercentage / 100.0))
            currentDate = currentDate.plusDays(config.daysPerStep.toLong())
            stepNumber++
        }
        
        // Add final "jump" step if appropriate, or just stop.
        // For visual completeness, the loop end usually implies the user stops or is at a micro amount.
        
        _calculatedPlan.value = steps
    }

    fun scheduleRemindersForPlan() {
        val steps = activeSteps.value
        if (steps.isEmpty()) return
        val currentPlan = activePlan.value ?: return
        
        // Find next incomplete step
        val nextStep = steps.firstOrNull { !it.isComplete } ?: return
        
        // Calculate target date based on last completed step
        val lastCompleted = steps.lastOrNull { it.isComplete }
        val daysToAdd = currentPlan.daysPerStep.toLong()
        
        val baseDate = if (lastCompleted?.completionDate != null) {
             lastCompleted.completionDate.atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
             // If no steps completed, base it on plan creation + (stepNumber-1) * interval?
             // Or simpler: just treat "now" as the start if purely manual?
             // Prompt requested: "generated step list... persist progress".
             // Let's rely on user manually checking.
             // But for reminder, we need a target.
             // Fallback: Plan Creation Date + StepIndex * Days
             currentPlan.creationDate.atZone(ZoneId.systemDefault()).toLocalDate()
                 .plusDays((nextStep.stepNumber - 1) * daysToAdd)
        }
        
        val targetDate = baseDate.plusDays(daysToAdd)
        
        if (targetDate.isAfter(LocalDate.now())) {
             val targetInstance = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
             reminderScheduler.scheduleBreakCompletionReminder(
                 substanceName = "${currentPlan.substanceName} (Step ${nextStep.stepNumber}: ${nextStep.dose}${currentPlan.units})", 
                 targetTime = targetInstance
             )
        }
    }
}

data class TaperConfig(
    val substanceName: String = "",
    val startingDose: Double = 0.0,
    val units: String = "mg",
    val reductionPercentage: Double = 10.0, // 10%
    val daysPerStep: Int = 14
)

data class TaperStep(
    val stepNumber: Int,
    val date: LocalDate,
    val dose: Double
)
