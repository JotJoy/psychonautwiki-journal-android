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

package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import kotlinx.serialization.Serializable

@Entity(
    tableName = "substancedosage",
    indices = [
        Index(value = ["substanceName", "route"], unique = true)
    ]
)
@Serializable
data class SubstanceDosage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val substanceName: String,
    val route: String, // Stored as String to avoid ordinal issues. Converted manually or via helper.
    
    // Light
    val lightMin: Double? = null,
    val lightMax: Double? = null,
    
    // Common
    val commonMin: Double? = null,
    val commonMax: Double? = null,
    
    // Strong
    val strongMin: Double? = null,
    val strongMax: Double? = null,
    
    val note: String? = null
) {
    // Helper to get enum if needed (though Data Layer usually just passes DTOs)
    fun getRouteEnum(): AdministrationRoute? {
        return try {
            AdministrationRoute.valueOf(route)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
