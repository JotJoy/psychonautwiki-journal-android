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

package com.isaakhanimann.journal.ui.tabs.safer

import androidx.compose.animation.animateContentSize
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.data.substances.classes.Substance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceArticleScreen(
    substanceName: String,
    navigateBack: () -> Unit,
    viewModel: SubstanceArticleViewModel = hiltViewModel()
) {
    LaunchedEffect(substanceName) {
        viewModel.loadSubstance(substanceName)
    }

    val state by viewModel.articleState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(substanceName) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is ArticleState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ArticleState.Error -> {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ArticleState.Success -> {
                    if (s.isOfflineMode) {
                    OfflineSubstanceView(
                        substance = s.substance,
                        onScheduleReminder = viewModel::scheduleReminder
                    )
                } else {
                        NativeArticleView(
                            htmlContent = s.cachedContent,
                            fallbackSubstance = s.substance,
                            onError = viewModel::showOfflineContent,
                            onScheduleReminder = viewModel::scheduleReminder
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun NativeArticleView(
    htmlContent: String?,
    fallbackSubstance: Substance?,

    onError: () -> Unit,
    onScheduleReminder: (title: String, message: String, delayMs: Long) -> Unit
) {
    if (htmlContent == null) {
        onError()
        return
    }

    // Parse into structured sections
    val sections = remember(htmlContent) {
        HtmlParser.parse(htmlContent)
    }

    // State for expanded sections (by index or title)
    // We default the first section (Overview) to expanded
    val expandedStates = remember { 
        mutableStateMapOf<Int, Boolean>().apply { 
             put(0, true) 
        } 
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
    ) {
        // Main Title
        if (fallbackSubstance != null) {
            item {
                Text(
                    text = fallbackSubstance.name,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        itemsIndexed(sections) { index, section ->
            val isExpanded = expandedStates[index] ?: false
            
            ExpandableSection(
                title = section.title,
                isExpanded = isExpanded,
                onToggle = { expandedStates[index] = !isExpanded },
                content = section.content
            )
        }
        
        item {
            if (fallbackSubstance != null && fallbackSubstance.saferUse.isNotEmpty()) {
                // Reminder state
                var showReminderDialog by remember { mutableStateOf(false) }
                var selectedTip by remember { mutableStateOf<String?>(null) }

                SaferUseList(
                    tips = fallbackSubstance.saferUse,
                    onScheduleReminder = { tip ->
                        selectedTip = tip
                        showReminderDialog = true
                    }
                )

                if (showReminderDialog && selectedTip != null) {
                    ReminderOptionsDialog(
                        tip = selectedTip!!,
                        onDismiss = { showReminderDialog = false },
                        onSchedule = { delayMs ->
                            onScheduleReminder("Safety Reminder", selectedTip!!, delayMs)
                            showReminderDialog = false
                        }
                    )
                }
            }
        }
        
        // Add footer for attribution
        item {
            Text(
                text = "Source: PsychonautWiki (content adapted for offline reading)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: List<ArticleBlock>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Content
            if (isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    content.forEach { block ->
                        RenderBlock(block)
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalTextApi::class)
@Composable
fun RenderBlock(block: ArticleBlock) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    
    when (block) {
        is ArticleBlock.Heading -> {
             // Subheadings H3-H6
            Text(
                text = block.text,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        is ArticleBlock.Paragraph -> {
            val primaryColor = MaterialTheme.colorScheme.primary
            val styledText = remember(block.content, primaryColor) {
                block.content.styleLinks(primaryColor)
            }
            androidx.compose.foundation.text.ClickableText(
                text = styledText,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(bottom = 8.dp),
                onClick = { offset ->
                    block.content.getUrlAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                        try { uriHandler.openUri(annotation.item.url) } catch (e: Exception) {}
                    }
                }
            )
        }
        is ArticleBlock.BulletList -> {
            Column(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                block.items.forEach { item ->
                    val styledItem = remember(item, primaryColor) { item.styleLinks(primaryColor) }
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text("• ", style = MaterialTheme.typography.bodyMedium)
                         androidx.compose.foundation.text.ClickableText(
                            text = styledItem,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            onClick = { offset ->
                                item.getUrlAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                                    try { uriHandler.openUri(annotation.item.url) } catch (e: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
        is ArticleBlock.OrderedList -> {
            Column(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                block.items.forEachIndexed { index, item ->
                    val styledItem = remember(item, primaryColor) { item.styleLinks(primaryColor) }
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium)
                        androidx.compose.foundation.text.ClickableText(
                            text = styledItem,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            onClick = { offset ->
                                item.getUrlAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                                    try { uriHandler.openUri(annotation.item.url) } catch (e: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
        is ArticleBlock.Image -> {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                color = androidx.compose.ui.graphics.Color(0xFFF5F5F5), // Light gray/white background for visibility
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AsyncImage(
                        model = block.url,
                        contentDescription = block.caption ?: "Article image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth,
                        onError = { /* Hide gracefully by not rendering placeholder or height */ }
                    )
                    if (block.caption != null) {
                        Text(
                            text = block.caption,
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = androidx.compose.ui.graphics.Color.Black, // Ensure caption is readable on light bg
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        is ArticleBlock.Callout -> {
            val (containerColor, contentColor, icon) = when (block.type) {
                CalloutType.WARNING -> Triple(
                    // Soft Amber/Orange
                    if (isSystemInDarkTheme()) androidx.compose.ui.graphics.Color(0xFF433008) else androidx.compose.ui.graphics.Color(0xFFFFF8E1),
                    if (isSystemInDarkTheme()) androidx.compose.ui.graphics.Color(0xFFFFD54F) else androidx.compose.ui.graphics.Color(0xFF795548),
                    Icons.Default.Warning
                )
                CalloutType.DANGER -> Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    Icons.Default.Warning // Exclamation usually fine, or distinct icon if available
                )
                CalloutType.INFO -> Triple(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    null
                )
            }
            
            // Link color logic: Use the content color but maybe slightly bolder or same?
            // "De-emphasize link color... keep readable". 
            // Underline is present from parser. Let's strictly use contentColor for links to blend in, 
            // or a slightly brighter shade? Let's use contentColor for "Safety" boxes to avoid distraction.
            val styledContent = remember(block.content, contentColor) {
               block.content.styleLinks(contentColor)
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.padding(end = 16.dp).size(24.dp)
                        )
                    }
                    androidx.compose.foundation.text.ClickableText(
                        text = styledContent,
                        style = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                        onClick = { offset ->
                            block.content.getUrlAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                                try { uriHandler.openUri(annotation.item.url) } catch (e: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
fun androidx.compose.ui.text.AnnotatedString.styleLinks(linkColor: androidx.compose.ui.graphics.Color): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append(this@styleLinks)
        this@styleLinks.getUrlAnnotations(0, length).forEach { range ->
            addStyle(
                androidx.compose.ui.text.SpanStyle(
                    color = linkColor,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                ),
                range.start,
                range.end
            )
        }
    }
}

@Composable
fun OfflineSubstanceView(
    substance: Substance?,
    onScheduleReminder: (title: String, message: String, delayMs: Long) -> Unit
) {
    if (substance == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Text("Content Unavailable")
        }
        return
    }

    var showReminderDialog by remember { mutableStateOf(false) }
    var selectedTip by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
         Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
             Column(modifier = Modifier.padding(16.dp)) {
                 Text(
                     text = "Quick Summary",
                     style = MaterialTheme.typography.titleMedium,
                     color = MaterialTheme.colorScheme.onTertiaryContainer
                 )
                 Text(
                     text = substance.summary ?: "No summary available.",
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onTertiaryContainer,
                     modifier = Modifier.padding(top = 8.dp)
                 )
             }
         }
         
         // Effects
         if (!substance.effectsSummary.isNullOrBlank()) {
             Text(
                 text = "Subjective Effects",
                 style = MaterialTheme.typography.titleLarge,
                 modifier = Modifier.padding(vertical = 8.dp)
             )
             Text(
                 text = substance.effectsSummary,
                 style = MaterialTheme.typography.bodyMedium,
                 modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
             )
         }
         // Add Harm Reduction Tips
         if (substance.saferUse.isNotEmpty()) {
             SaferUseList(
                 tips = substance.saferUse,
                 onScheduleReminder = { tip ->
                     selectedTip = tip
                     showReminderDialog = true
                 }
             )
         }
    }

    if (showReminderDialog && selectedTip != null) {
        ReminderOptionsDialog(
            tip = selectedTip!!,
            onDismiss = { showReminderDialog = false },
            onSchedule = { delayMs ->
                onScheduleReminder("Safety Reminder", selectedTip!!, delayMs)
                showReminderDialog = false
            }
        )
    }
}

@Composable
fun SaferUseList(
    tips: List<String>,
    onScheduleReminder: (String) -> Unit
) {
    if (tips.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Harm Reduction & Safety",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        
        tips.forEach { tip ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).padding(horizontal = 8.dp)
            ) {
                Row(
                   modifier = Modifier.padding(12.dp),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onScheduleReminder(tip) }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Set Reminder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderOptionsDialog(
    tip: String,
    onDismiss: () -> Unit,
    onSchedule: (Long) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
        title = { Text("Set Reminder") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "\"$tip\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { onSchedule(15 * 60 * 1000L) }, // 15 mins
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("In 15 Minutes") }
                
                Button(
                    onClick = { onSchedule(60 * 60 * 1000L) }, // 1 hour
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("In 1 Hour") }
                
                Button(
                    onClick = { onSchedule(4 * 60 * 60 * 1000L) }, // 4 hours
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("In 4 Hours") }
            }
        },
        confirmButton = {
             androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
