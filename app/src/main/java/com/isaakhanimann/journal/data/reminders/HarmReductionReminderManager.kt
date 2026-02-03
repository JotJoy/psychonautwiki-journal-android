/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 */

package com.isaakhanimann.journal.data.reminders

import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceCompanion
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.ui.tabs.settings.combinations.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HarmReductionReminderManager @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
    private val userPreferences: UserPreferences
) {
    /**
     * Schedules phase-aware reminders for a new ingestion.
     */
    suspend fun scheduleRemindersForIngestion(
        substance: Substance?,
        customSubstanceName: String?,
        route: AdministrationRoute,
        companion: SubstanceCompanion?,
        ingestionTime: Instant
    ) {
        // 1. Respect global harm-reduction toggle
        if (!userPreferences.areEducationalRemindersEnabledFlow.first()) return

        // 2. Resolve durations
        val durations = getEffectiveDurations(substance, route, companion)
        if (durations.totalSec == 0L) return

        // 3. Schedule hydration reminders
        if (companion?.hydrationRemindersEnabled == true) {
            scheduleHydrationReminders(durations, ingestionTime)
        }

        // 4. Schedule recovery reminders
        if (companion?.recoveryReminderEnabled == true) {
            scheduleRecoveryReminders(durations, ingestionTime)
        }

        // 5. Schedule sleep reminders
        if (companion?.sleepReminderEnabled == true) {
            scheduleSleepReminders(durations, ingestionTime)
        }
    }

    private fun getEffectiveDurations(
        substance: Substance?,
        route: AdministrationRoute,
        companion: SubstanceCompanion?
    ): PhaseDurations {
        // Priority 1: Custom overrides in companion
        if (companion != null && (companion.totalMin != null || companion.onsetMin != null)) {
            val onset = (companion.onsetMin ?: 0.0)
            val comeup = (companion.comeupMin ?: 0.0)
            val peak = (companion.peakMin ?: 0.0)
            val offset = (companion.offsetMin ?: 0.0)
            val total = (companion.totalMin ?: (onset + comeup + peak + offset))

            return PhaseDurations(
                onsetSec = (onset * 60).toLong(),
                comeupSec = (comeup * 60).toLong(),
                peakSec = (peak * 60).toLong(),
                offsetSec = (offset * 60).toLong(),
                totalSec = (total * 60).toLong()
            )
        }

        // Priority 2: Built-in substance data
        val roaDur = substance?.roas?.find { it.route == route }?.roaDuration
        if (roaDur != null) {
            return PhaseDurations(
                onsetSec = roaDur.onset?.averageInSec?.toLong() ?: 0L,
                comeupSec = roaDur.comeup?.averageInSec?.toLong() ?: 0L,
                peakSec = roaDur.peak?.averageInSec?.toLong() ?: 0L,
                offsetSec = roaDur.offset?.averageInSec?.toLong() ?: 0L,
                totalSec = roaDur.total?.averageInSec?.toLong() ?: 0L
            )
        }

        return PhaseDurations(0, 0, 0, 0, 0)
    }

    private fun scheduleHydrationReminders(durations: PhaseDurations, ingestionTime: Instant) {
        val totalSec = durations.totalSec
        
        if (totalSec >= 2 * 3600) {
            // Long duration: peak-based pacing
            val peakStartTime = durations.onsetSec + durations.comeupSec
            val peakDuration = durations.peakSec.coerceAtLeast(3600) // Assumed 1h if missing
            val peakEndTime = peakStartTime + peakDuration
            
            // First peak reminder
            reminderScheduler.scheduleCustomReminder(
                title = "Hydration Check",
                message = "The experience is reaching its peak. Remember to take small, regular sips of water or electrolytes.",
                delayMs = peakStartTime * 1000
            )

            // Subsequent peak reminders every 90 mins if peak is long enough
            var nextReminderSec = peakStartTime + 5400
            while (nextReminderSec < peakEndTime) {
                reminderScheduler.scheduleCustomReminder(
                    title = "Hydration Check",
                    message = "Stay hydrated! Regular water pacing helps maintain equilibrium during long experiences.",
                    delayMs = nextReminderSec * 1000
                )
                nextReminderSec += 5400
            }
            
            // Near offset
            val offsetTimeSec = peakEndTime + (durations.offsetSec / 2).coerceAtLeast(1800)
            reminderScheduler.scheduleCustomReminder(
                title = "Hydration Check",
                message = "The experience is winding down. A glass of water now supports your recovery phase.",
                delayMs = offsetTimeSec * 1000
            )
        } else {
            // Short duration: Mid-experience reminder
            reminderScheduler.scheduleCustomReminder(
                title = "Hydration Check",
                message = "Mid-experience check: Remember to stay hydrated even during shorter sessions.",
                delayMs = (totalSec / 2) * 1000
            )
        }
    }

    private fun scheduleRecoveryReminders(durations: PhaseDurations, ingestionTime: Instant) {
        val totalSec = durations.totalSec
        
        // Reminder near end of experience
        reminderScheduler.scheduleCustomReminder(
            title = "Recovery & Nutrition",
            message = "As effects subside, consider a light, nutritious snack and restful environment to support recovery.",
            delayMs = (totalSec - 600) * 1000 // 10 mins before end
        )

        // Next day follow-up (24h after ingestion)
        reminderScheduler.scheduleCustomReminder(
            title = "Post-Experience Self-Care",
            message = "Good morning! Focus on rest, balanced meals, and gentle hydration today to support your well-being.",
            delayMs = 24 * 3600 * 1000L
        )
    }

    private fun scheduleSleepReminders(durations: PhaseDurations, ingestionTime: Instant) {
        val totalSec = durations.totalSec
        
        // Near expected end of experience
        reminderScheduler.scheduleCustomReminder(
            title = "Wind-Down Preparation",
            message = "The experience is reaching its expected conclusion. Dimming lights and quiet activity can help your body transition toward rest.",
            delayMs = totalSec * 1000
        )
    }

    private data class PhaseDurations(
        val onsetSec: Long,
        val comeupSec: Long,
        val peakSec: Long,
        val offsetSec: Long,
        val totalSec: Long
    )
}
