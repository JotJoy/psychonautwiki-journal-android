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

package com.isaakhanimann.journal.ui.tabs.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.HarmReductionTip
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage
import com.isaakhanimann.journal.data.substances.repositories.HarmReductionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HarmReductionViewModel @Inject constructor(
    private val harmReductionRepo: HarmReductionRepository,
    private val experienceRepo: ExperienceRepository
) : ViewModel() {

    private val _targetSubstanceName = MutableStateFlow<String?>(null)

    fun setSubstanceName(name: String) {
        _targetSubstanceName.value = name
    }

    // --- Tips ---

    val harmReductionTips: StateFlow<List<HarmReductionTip>> = _targetSubstanceName
        .flatMapLatest { name ->
            if (name == null) flowOf(emptyList()) else harmReductionRepo.getHarmReductionTipsFlow(
                name
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dosages ---

    val substanceDosages: StateFlow<List<SubstanceDosage>> = _targetSubstanceName
        .flatMapLatest { name ->
            if (name == null) flowOf(emptyList()) else harmReductionRepo.getSubstanceDosagesFlow(
                name
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Break Tracking ---

    /**
     * Calculates the break status for the currently selected substance.
     * Returns a [BreakStatus] object containing days since last use and recommendation.
     */
    val breakStatus: StateFlow<BreakStatus> = _targetSubstanceName
        .flatMapLatest { name ->
            if (name == null) return@flatMapLatest flowOf(BreakStatus.Unknown)

            // Flow 1: Get user-defined rules (recommended break days) from SubstanceCompanion
            val companionFlow = experienceRepo.getSubstanceCompanionFlow(name)

            // Flow 2: Get last ingestion time
            val lastIngestionsFlow = experienceRepo.getSortedIngestionsFlow(name, 1)

            combine(companionFlow, lastIngestionsFlow) { companion, ingestions ->
                val lastIngestion = ingestions.firstOrNull()
                
                if (lastIngestion == null) {
                    return@combine BreakStatus.NoHistory
                }

                val recommendedBreakDays = companion?.recommendedBreakDays ?: 0
                val lastUseTime = lastIngestion.time
                val now = Instant.now()
                
                // Calculate days since last use (rounding down to conservative full days)
                val daysSinceLastUse = ChronoUnit.DAYS.between(lastUseTime, now)
                
                // Determine if warning is needed
                val isWarning = daysSinceLastUse < recommendedBreakDays
                val remainingDays = if (isWarning) recommendedBreakDays - daysSinceLastUse else 0

                BreakStatus.Tracked(
                    lastUseDate = lastUseTime,
                    daysSinceLastUse = daysSinceLastUse,
                    recommendedBreakDays = recommendedBreakDays,
                    isWarning = isWarning,
                    remainingDaysCallback = remainingDays
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BreakStatus.Unknown)
    
}

sealed class BreakStatus {
    object Unknown : BreakStatus()
    object NoHistory : BreakStatus()
    data class Tracked(
        val lastUseDate: Instant,
        val daysSinceLastUse: Long,
        val recommendedBreakDays: Int,
        val isWarning: Boolean,
        val remainingDaysCallback: Long
    ) : BreakStatus()
}
