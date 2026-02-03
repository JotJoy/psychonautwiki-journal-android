package com.isaakhanimann.journal.data.room.articles

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isaakhanimann.journal.data.room.articles.entities.CachedArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM cached_article WHERE url = :url")
    suspend fun getArticle(url: String): CachedArticle?

    @Query("SELECT * FROM cached_article WHERE url = :url")
    fun getArticleFlow(url: String): Flow<CachedArticle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: CachedArticle)

    @Query("SELECT COUNT(*) FROM cached_article")
    suspend fun getCount(): Int

    @Query("DELETE FROM cached_article")
    suspend fun deleteAll()
    
    @Query("DELETE FROM cached_article WHERE url = :url")
    suspend fun deleteArticle(url: String)
        
    @Query("DELETE FROM cached_article WHERE lastUpdated < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)
    
    @Query("SELECT MAX(lastUpdated) FROM cached_article")
    suspend fun getLastUpdatedTimestamp(): java.time.Instant?
}
