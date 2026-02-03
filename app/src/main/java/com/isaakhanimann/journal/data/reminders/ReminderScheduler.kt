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

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleDailyHarmReductionTips() {
        // Enqueue daily work
        val request = PeriodicWorkRequestBuilder<HarmReductionWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS) // Estimate to trigger around mid-day if scheduled in morning
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DailyHarmReduction",
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if existing
            request
        )
    }

    fun scheduleCustomReminder(title: String, message: String, delayMs: Long) {
        val data = Data.Builder()
            .putString(HarmReductionWorker.KEY_TITLE, title)
            .putString(HarmReductionWorker.KEY_MESSAGE, message)
            .build()

        val request = OneTimeWorkRequestBuilder<HarmReductionWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "HarmReduction_${System.currentTimeMillis()}", // Allow multiple unique reminders
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    fun scheduleBreakCompletionReminder(substanceName: String, targetTime: Instant) {
        val delayMs = targetTime.toEpochMilli() - System.currentTimeMillis()
        if (delayMs <= 0) return

        // Modern Android: Prefer WorkManager for non-exact requirements
        // "Break completion" is not time-critical enough to demand Exact Alarm permissions unless user explicitly enabled it settings.
        // We use WorkManager here as the primary robust mechanism.
        
        val data = Data.Builder()
            .putString(BreakTimeWorker.KEY_SUBSTANCE_NAME, substanceName)
            .build()

        val request = OneTimeWorkRequestBuilder<BreakTimeWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "Break_$substanceName",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Optional: Schedule exact alarm if permissions are granted.
     * Fallback to WorkManager is handled implicitly by the method above if this returns false/fails.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager: AlarmManager? = context.getSystemService()
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true // True for pre-Android 12
        }
    }
}
