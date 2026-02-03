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

package com.isaakhanimann.journal.data.substances.repositories

import com.isaakhanimann.journal.data.room.experiences.SubstanceMetadataDao
import com.isaakhanimann.journal.data.room.experiences.entities.HarmReductionTip
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceDosage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HarmReductionRepository @Inject constructor(
    private val substanceMetadataDao: SubstanceMetadataDao
) {

    // --- Harm Reduction Tips ---

    fun getHarmReductionTipsFlow(substanceName: String): Flow<List<HarmReductionTip>> =
        substanceMetadataDao.getHarmReductionTipsFlow(substanceName)
            .flowOn(Dispatchers.IO)
            .conflate()

    suspend fun insertTip(tip: HarmReductionTip) = substanceMetadataDao.insertTip(tip)

    suspend fun updateTip(tip: HarmReductionTip) = substanceMetadataDao.updateTip(tip)

    suspend fun deleteTip(tip: HarmReductionTip) = substanceMetadataDao.deleteTip(tip)

    // --- Dosages ---

    fun getSubstanceDosagesFlow(substanceName: String): Flow<List<SubstanceDosage>> =
        substanceMetadataDao.getSubstanceDosagesFlow(substanceName)
            .flowOn(Dispatchers.IO)
            .conflate()

    suspend fun getSubstanceDosage(substanceName: String, route: String): SubstanceDosage? =
        substanceMetadataDao.getSubstanceDosage(substanceName, route)

    suspend fun insertDosage(dosage: SubstanceDosage) = substanceMetadataDao.insertDosage(dosage)

    suspend fun updateDosage(dosage: SubstanceDosage) = substanceMetadataDao.updateDosage(dosage)

    suspend fun deleteDosage(dosage: SubstanceDosage) = substanceMetadataDao.deleteDosage(dosage)

}
