package com.isaakhanimann.journal.ui.settings.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataContentScreen(
    navigateBack: () -> Unit,
    viewModel: DataContentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showConfirmDownload by remember { mutableStateOf(false) }
    var showConfirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Content") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introduction Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Text(
                            text = "Advanced Content Management",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Download substance articles from PsychonautWiki for offline access. All downloads are user-initiated and throttled to respect the server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Storage Status
            Text(
                text = "Storage Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatusRow("Substances in Database", "${state.totalSubstancesCount}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusRow("Cached Articles", "${state.cachedArticleCount}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusRow("Approximate Storage", String.format("%.1f MB", state.approximateStorageMb))
                    
                    if (state.lastUpdated != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                        StatusRow("Last Updated", formatter.format(state.lastUpdated))
                    }
                }
            }

            HorizontalDivider()

            // Download Section
            Text(
                text = "Offline Content",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (state.isDownloading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Downloading...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(state.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = state.progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Progress details
                        if (state.currentDownloadIndex > 0) {
                            Text(
                                text = "Article ${state.currentDownloadIndex} of ${state.totalDownloadCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        state.currentDownloadName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Downloaded: ${state.downloadedCount}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (state.skippedCount > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.height(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.padding(2.dp))
                                        Text(
                                            text = "Skipped: ${state.skippedCount} (cached)",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            
                            // Estimated time
                            state.estimatedSecondsRemaining?.let { seconds ->
                                if (seconds > 0) {
                                    Text(
                                        text = formatDuration(seconds),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = viewModel::cancelDownload,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Download all substance articles for offline viewing. Downloads are throttled (2 seconds per article) to respect PsychonautWiki's infrastructure.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Already cached articles are skipped automatically\n• Downloads can be safely resumed after cancellation\n• Source: psychonautwiki.org",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { showConfirmDownload = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Download All Articles")
                        }
                    }
                }
            }

            HorizontalDivider()

            // Maintenance
            Text(
                text = "Maintenance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Clear all cached articles to free up storage space. You will need an internet connection to view articles again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showConfirmClear = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isClearing && state.cachedArticleCount > 0
                    ) {
                        if (state.isClearing) {
                            Text("Clearing...")
                        } else {
                            Text("Clear Article Cache")
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showConfirmDownload) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmDownload = false },
            title = { Text("Download All Articles?") },
            text = { 
                Text(
                    "This will download articles for all ${state.totalSubstancesCount} substances. " +
                    "Already cached articles will be skipped. " +
                    "The process is throttled (2s per article) and may take considerable time.\n\n" +
                    "Ensure you have a stable internet connection."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDownload = false
                    viewModel.cacheAllArticles()
                }) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDownload = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmClear) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("Clear Article Cache?") },
            text = { 
                Text(
                    "This will remove all ${state.cachedArticleCount} cached articles (approximately ${String.format("%.1f MB", state.approximateStorageMb)}). " +
                    "You will need an internet connection to view them again."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmClear = false
                        viewModel.clearCache()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val duration = Duration.ofSeconds(seconds.toLong())
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val secs = duration.seconds % 60
    
    return when {
        hours > 0 -> String.format("~%dh %dm remaining", hours, minutes)
        minutes > 0 -> String.format("~%dm %ds remaining", minutes, secs)
        else -> String.format("~%ds remaining", secs)
    }
}
