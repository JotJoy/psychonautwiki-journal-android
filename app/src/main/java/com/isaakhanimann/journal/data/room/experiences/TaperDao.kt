/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 */

package com.isaakhanimann.journal.data.room.experiences

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.isaakhanimann.journal.data.room.experiences.entities.TaperPlan
import com.isaakhanimann.journal.data.room.experiences.entities.TaperStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaperDao {

    @Query("SELECT * FROM taper_plan WHERE isArchived = 0 LIMIT 1")
    fun getActivePlan(): TaperPlan?

    @Query("SELECT * FROM taper_plan WHERE isArchived = 0 LIMIT 1")
    fun getActivePlanFlow(): Flow<TaperPlan?>

    @Query("SELECT * FROM taper_step WHERE planId = :planId ORDER BY stepNumber ASC")
    fun getStepsForPlan(planId: Long): Flow<List<TaperStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: TaperPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<TaperStepEntity>)

    @Update
    suspend fun updateStep(step: TaperStepEntity)
    
    @Update
    suspend fun updatePlan(plan: TaperPlan)

    @Query("UPDATE taper_plan SET isArchived = 1 WHERE id = :planId")
    suspend fun archivePlan(planId: Long)

    @Query("DELETE FROM taper_plan WHERE id = :planId")
    suspend fun deletePlan(planId: Long)
}
