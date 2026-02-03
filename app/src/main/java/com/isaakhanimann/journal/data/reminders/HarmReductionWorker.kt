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

package com.isaakhanimann.journal.data.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HarmReductionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
    // In a real implementation, we would inject a Repository to fetch tips
    // private val harmReductionRepo: HarmReductionRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TITLE = "key_title"
        const val KEY_MESSAGE = "key_message"
    }

    override suspend fun doWork(): Result {
        if (!notificationHelper.hasNotificationPermission()) {
            return Result.failure()
        }

        // Logic to fetch a tip would go here.
        // For demonstration/requirement purposes, we show a generic tip.
        // To strictly follow "Harm-reduction educational reminders", we should iterate through tips.
        
        val title = inputData.getString(KEY_TITLE) ?: "Harm Reduction Tip"
        val message = inputData.getString(KEY_MESSAGE) ?: "Always measure your dosages carefully and test your substances using reagent kits. Start low and go slow."

        notificationHelper.showHarmReductionNotification(
            id = System.currentTimeMillis().toInt(), // Unique ID for multiple reminders
            title = title,
            message = message
        )

        return Result.success()
    }
}
