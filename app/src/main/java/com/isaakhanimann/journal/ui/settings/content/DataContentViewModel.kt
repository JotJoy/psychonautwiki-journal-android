package com.isaakhanimann.journal.ui.settings.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaakhanimann.journal.data.room.articles.ArticleRepository
import com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DataContentState(
    val cachedArticleCount: Int = 0,
    val totalSubstancesCount: Int = 0,
    val isDownloading: Boolean = false,
    val isClearing: Boolean = false,
    val progress: Float = 0f,
    val currentDownloadName: String? = null,
    val currentDownloadIndex: Int = 0,
    val totalDownloadCount: Int = 0,
    val skippedCount: Int = 0,
    val downloadedCount: Int = 0,
    val estimatedSecondsRemaining: Int? = null,
    val lastUpdated: java.time.Instant? = null,
    val approximateStorageMb: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class DataContentViewModel @Inject constructor(
    private val substanceRepository: SubstanceRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DataContentState())
    val state: StateFlow<DataContentState> = _state.asStateFlow()
    
    private var downloadJob: Job? = null

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            val count = articleRepository.getCachedArticleCount()
            val total = substanceRepository.getAllSubstances().size
            
            // Calculate approximate storage (rough estimate: 50KB per article on average)
            val approximateStorageMb = (count * 50) / 1024f
            
            // Get last updated timestamp (most recent cached article)
            val lastUpdated = articleRepository.getLastUpdatedTimestamp()
            
            _state.update { 
                it.copy(
                    cachedArticleCount = count, 
                    totalSubstancesCount = total,
                    approximateStorageMb = approximateStorageMb,
                    lastUpdated = lastUpdated
                ) 
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(isClearing = true) }
            try {
                articleRepository.clearCache()
                refreshStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to clear cache: ${e.message}") }
            } finally {
                _state.update { it.copy(isClearing = false) }
            }
        }
    }

    fun cacheAllArticles() {
        if (_state.value.isDownloading) return

        downloadJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _state.update { 
                it.copy(
                    isDownloading = true, 
                    progress = 0f, 
                    error = null,
                    skippedCount = 0,
                    downloadedCount = 0,
                    currentDownloadIndex = 0
                ) 
            }
            
            val allSubstances = substanceRepository.getAllSubstances()
            val total = allSubstances.size
            if (total == 0) {
                 _state.update { it.copy(isDownloading = false) }
                 return@launch
            }

            var processed = 0
            var skipped = 0
            var downloaded = 0
            
            for (substance in allSubstances) {
                if (!isActive) break // Handle cancel
                
                val currentIndex = processed + 1
                
                // Calculate estimated time remaining
                val elapsedMs = System.currentTimeMillis() - startTime
                val avgMsPerItem = if (processed > 0) elapsedMs / processed else 0
                val remainingItems = total - processed
                val estimatedSecondsRemaining = if (avgMsPerItem > 0) {
                    ((remainingItems * avgMsPerItem) / 1000).toInt()
                } else null
                
                _state.update { 
                    it.copy(
                        currentDownloadName = substance.name,
                        currentDownloadIndex = currentIndex,
                        totalDownloadCount = total,
                        progress = processed.toFloat() / total,
                        estimatedSecondsRemaining = estimatedSecondsRemaining,
                        skippedCount = skipped,
                        downloadedCount = downloaded
                    ) 
                }

                try {
                    // Check if already cached
                    val exists = articleRepository.getCachedArticle(substance.url) != null
                    if (!exists) {
                        val success = articleRepository.fetchAndCache(substance.url)
                        if (success) {
                            downloaded++
                        }
                        // Throttling: 2 seconds delay to be respectful to PsychonautWiki
                        delay(2000) 
                    } else {
                        skipped++
                        // No delay for skipped articles
                    }
                } catch (e: Exception) {
                    // Log error but continue
                }

                processed++
            }

            _state.update { 
                it.copy(
                    isDownloading = false, 
                    currentDownloadName = null, 
                    progress = 1f,
                    currentDownloadIndex = 0,
                    estimatedSecondsRemaining = null
                ) 
            }
            refreshStats()
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _state.update { it.copy(isDownloading = false, currentDownloadName = null) }
    }
}
