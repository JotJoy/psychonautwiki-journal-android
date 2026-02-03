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

package com.isaakhanimann.journal.ui.tabs.journal.calendar

import androidx.lifecycle.ViewModel
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.ui.utils.getInstant
import com.kizitonwose.calendar.core.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class DayViewModel @Inject constructor(
    val experienceRepo: ExperienceRepository,
) : ViewModel() {

    suspend fun getExperienceInfo(day: CalendarDay): ExperienceInfo {
        val startOfDay = day.date.atStartOfDay().getInstant()
        val endOfDay = startOfDay.plusMillis(24 * 60 * 60 * 1000)
        val ingestions = experienceRepo.getIngestionsWithCompanionsAndExperience(
            fromInstant = startOfDay,
            toInstant = endOfDay
        )
        // Filter out entries hidden by experience OR by substance companion
        val visibleIngestions = ingestions.filter { 
            it.experience?.isHiddenFromCalendar != true &&
            it.substanceCompanion?.hideFromCalendar != true
        }
        
        // Check if there are any hidden entries this day (either type)
        val hasHiddenEntries = ingestions.any { 
            it.experience?.isHiddenFromCalendar == true ||
            it.substanceCompanion?.hideFromCalendar == true
        }

        return ExperienceInfo(
            experienceIds = visibleIngestions.map { it.ingestion.experienceId }.toSet().toList(),
            colors = visibleIngestions.mapNotNull { it.substanceCompanion?.color },
            hasHiddenEntries = hasHiddenEntries
        )
    }
}

data class ExperienceInfo(
    val experienceIds: List<Int>,
    val colors: List<AdaptiveColor>,
    val hasHiddenEntries: Boolean = false
)