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

package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SubstanceCompanion(
    @PrimaryKey(autoGenerate = false)
    val substanceName: String,
    var color: AdaptiveColor,
    
    // Calendar Display
    @ColumnInfo(defaultValue = "0")
    val hideFromCalendar: Boolean = false,
    
    // Harm Reduction
    val recommendedBreakDays: Int? = null,

    // Durations (in minutes)
    val onsetMin: Double? = null,
    val onsetMax: Double? = null,
    val comeupMin: Double? = null,
    val comeupMax: Double? = null,
    val peakMin: Double? = null,
    val peakMax: Double? = null,
    val offsetMin: Double? = null,
    val offsetMax: Double? = null,
    val totalMin: Double? = null,
    val totalMax: Double? = null,

    // Reminder Settings
    @ColumnInfo(defaultValue = "0")
    val hydrationRemindersEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val recoveryReminderEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val sleepReminderEnabled: Boolean = false
)
