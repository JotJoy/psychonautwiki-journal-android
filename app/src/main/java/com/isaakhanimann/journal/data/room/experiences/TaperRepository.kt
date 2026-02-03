/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 */

package com.isaakhanimann.journal.data.room.experiences

import com.isaakhanimann.journal.data.room.experiences.entities.TaperPlan
import com.isaakhanimann.journal.data.room.experiences.entities.TaperStatus
import com.isaakhanimann.journal.data.room.experiences.entities.TaperStepEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaperRepository @Inject constructor(
    private val taperDao: TaperDao
) {

    val activePlanFlow: Flow<TaperPlan?> = taperDao.getActivePlanFlow()

    fun getStepsForPlan(planId: Long): Flow<List<TaperStepEntity>> {
        return taperDao.getStepsForPlan(planId)
    }

    suspend fun saveNewPlan(plan: TaperPlan, steps: List<TaperStepEntity>) = withContext(Dispatchers.IO) {
        // Archive existing active plan if any
        val existingPlan = taperDao.getActivePlan()
        if (existingPlan != null) {
            taperDao.archivePlan(existingPlan.id)
        }

        val planId = taperDao.insertPlan(plan)
        val stepsWithPlanId = steps.map { it.copy(planId = planId) }
        taperDao.insertSteps(stepsWithPlanId)
    }

    suspend fun updateStep(step: TaperStepEntity) = withContext(Dispatchers.IO) {
        taperDao.updateStep(step)
    }
    
    suspend fun pausePlan(planId: Long) = withContext(Dispatchers.IO) {
        // We might need to fetch, modify, update or just have a specific query
        // For now, simpler to fetch and update or just assume we use updatePlan
        // In the DAO, updatePlan is generic.
        // Let's verify if we need to fetch first. Use a DAO query for efficiency if added later.
        // For now, assume ViewModel handles the object modification.
    }

    suspend fun updatePlan(plan: TaperPlan) = withContext(Dispatchers.IO) {
        taperDao.updatePlan(plan)
    }

    suspend fun archivePlan(planId: Long) = withContext(Dispatchers.IO) {
        taperDao.archivePlan(planId)
    }
    
    suspend fun  deletePlan(planId: Long) = withContext(Dispatchers.IO) {
       taperDao.deletePlan(planId)
    }
}
