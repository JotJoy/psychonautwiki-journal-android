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

package com.isaakhanimann.journal.data.room.experiences

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.isaakhanimann.journal.data.room.experiences.entities.HarmReductionTip
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceMetadataDao {

    // --- Harm Reduction Tips ---

    @Query("SELECT * FROM harmreductiontip WHERE substanceName = :substanceName")
    fun getHarmReductionTipsFlow(substanceName: String): Flow<List<HarmReductionTip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: HarmReductionTip): Long

    @Update
    suspend fun updateTip(tip: HarmReductionTip)

    @Delete
    suspend fun deleteTip(tip: HarmReductionTip)

    // --- Substance Dosages ---

    @Query("SELECT * FROM substancedosage WHERE substanceName = :substanceName")
    fun getSubstanceDosagesFlow(substanceName: String): Flow<List<SubstanceDosage>>
    
    @Query("SELECT * FROM substancedosage WHERE substanceName = :substanceName AND route = :route LIMIT 1")
    suspend fun getSubstanceDosage(substanceName: String, route: String): SubstanceDosage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDosage(dosage: SubstanceDosage): Long

    @Update
    suspend fun updateDosage(dosage: SubstanceDosage)

    @Delete
    suspend fun deleteDosage(dosage: SubstanceDosage)
}
