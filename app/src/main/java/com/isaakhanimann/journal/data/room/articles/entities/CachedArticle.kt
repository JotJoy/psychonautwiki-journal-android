package com.isaakhanimann.journal.data.room.articles.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "cached_article")
data class CachedArticle(
    @PrimaryKey
    val url: String,
    val htmlContent: String,
    val lastUpdated: Instant,
    val eTag: String? = null // For cache validation if needed later
)
