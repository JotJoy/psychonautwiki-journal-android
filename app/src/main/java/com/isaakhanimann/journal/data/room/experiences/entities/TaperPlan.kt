/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 */

package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "taper_plan")
data class TaperPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val substanceName: String,
    val startingDose: Double,
    val units: String,
    val reductionPercentage: Double,
    val daysPerStep: Int,
    val creationDate: Instant = Instant.now(),
    val isArchived: Boolean = false
)

enum class TaperStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}
