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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.substances.classes.Substance
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.isaakhanimann.journal.data.room.articles.ArticleRepository
import com.isaakhanimann.journal.data.room.articles.entities.CachedArticle
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

import com.isaakhanimann.journal.data.reminders.ReminderScheduler

@HiltViewModel
class SubstanceArticleViewModel @Inject constructor(
    private val substanceRepository: SubstanceRepository,
    private val articleRepository: ArticleRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    fun scheduleReminder(title: String, message: String, delayMs: Long) {
        reminderScheduler.scheduleCustomReminder(title, message, delayMs)
    }

    private val _articleState = MutableStateFlow<ArticleState>(ArticleState.Loading)
    val articleState: StateFlow<ArticleState> = _articleState.asStateFlow()

    fun loadSubstance(substanceName: String) {
        viewModelScope.launch {
            _articleState.value = ArticleState.Loading
            val substance = substanceRepository.getSubstance(substanceName)
            
            if (substance != null) {
                // 1. Check local cache first
                val cachedArticle = articleRepository.getCachedArticle(substance.url)

                if (cachedArticle != null) {
                    // Cache Hit: Show Full Article immediately
                    _articleState.value = ArticleState.Success(
                        substance = substance,
                        isOfflineMode = false,
                        cachedContent = cachedArticle.htmlContent
                    )
                } else {
                    // Cache Miss: Show Summary immediately, BUT try to fetch full article in background
                    _articleState.value = ArticleState.Success(
                        substance = substance,
                        isOfflineMode = true,
                        cachedContent = null
                    )
                    
                    // Auto-fetch in background
                    fetchOnlineContent(substance.url, substance)
                }
            } else {
                _articleState.value = ArticleState.Error("Substance not found")
            }
        }
    }

    private fun fetchOnlineContent(url: String, substance: Substance) {
        viewModelScope.launch {
            articleRepository.getArticle(url)
                .catch { e ->
                    // Network error: Silent failure.
                    // User stays on Summary view (isOfflineMode = true).
                    // We might log this, but UI shouldn't disrupt.
                }
                .collect { cachedArticle ->
                    if (cachedArticle != null) {
                        // Fetch Success: Upgrade to Full Article
                        _articleState.value = ArticleState.Success(
                            substance = substance,
                            isOfflineMode = false,
                            cachedContent = cachedArticle.htmlContent
                        )
                    }
                }
        }
    }

    fun showOfflineContent() {
        val currentState = _articleState.value
        if (currentState is ArticleState.Success) {
            _articleState.value = currentState.copy(isOfflineMode = true)
        }
    }
}

sealed class ArticleState {
    object Loading : ArticleState()
    data class Success(
        val substance: Substance, 
        val isOfflineMode: Boolean,
        val cachedContent: String? = null
    ) : ArticleState()
    data class Error(val message: String) : ArticleState()
}
