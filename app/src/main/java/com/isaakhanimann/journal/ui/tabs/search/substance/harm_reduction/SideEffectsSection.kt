package com.isaakhanimann.journal.ui.tabs.search.substance.harm_reduction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.classes.harm_reduction.MitigationTopic
import com.isaakhanimann.journal.data.substances.classes.harm_reduction.MitigationType
import com.isaakhanimann.journal.data.substances.classes.harm_reduction.SideEffect
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SideEffectsSection(
    substance: Substance,
    sideEffects: List<SideEffect>,
    onMitigationClick: (MitigationType) -> Unit,
    hydrationRemindersEnabled: Boolean,
    onHydrationRemindersChange: (Boolean) -> Unit,
    recoveryReminderEnabled: Boolean,
    onRecoveryReminderChange: (Boolean) -> Unit,
    sleepReminderEnabled: Boolean,
    onSleepReminderChange: (Boolean) -> Unit
) {
    if (sideEffects.isEmpty()) return

    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = horizontalPadding, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Comfort & Safety Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Commonly discussed effects and comfort strategies shared within the community.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            sideEffects.forEachIndexed { index, effect ->
                SideEffectItem(
                    sideEffect = effect,
                    onMitigationClick = onMitigationClick
                )
                
                if (index < sideEffects.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Safety Tools Section
            Text(
                text = "Optional Wellness Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Set gentle reminders to support comfort and self-care practices (completely optional).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hydration Reminder Toggle
            ReminderToggle(
                label = "Stay hydrated",
                description = "Gentle reminders to drink water at a moderate pace",
                checked = hydrationRemindersEnabled,
                onCheckedChange = onHydrationRemindersChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recovery Reminder Toggle
            ReminderToggle(
                label = "Recovery self-care",
                description = "Next-day reminder to rest, hydrate, and nourish yourself",
                checked = recoveryReminderEnabled,
                onCheckedChange = onRecoveryReminderChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sleep Reminder Toggle
            ReminderToggle(
                label = "Sleep preparation",
                description = "Reminder to consider winding down and preparing for rest",
                checked = sleepReminderEnabled,
                onCheckedChange = onSleepReminderChange
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SideEffectItem(
    sideEffect: SideEffect,
    onMitigationClick: (MitigationType) -> Unit
) {
    Column {
        Text(
            text = sideEffect.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = sideEffect.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (sideEffect.mitigations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "May help with comfort:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sideEffect.mitigations.forEach { mitigation ->
                    MitigationChip(
                        mitigation = mitigation,
                        onClick = { onMitigationClick(mitigation.type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MitigationChip(
    mitigation: MitigationTopic,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = mitigation.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Learn more",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.height(16.dp).width(16.dp)
            )
        }
    }
}

@Composable
private fun ReminderToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
