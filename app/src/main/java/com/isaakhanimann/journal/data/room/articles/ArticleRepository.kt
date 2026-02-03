package com.isaakhanimann.journal.data.room.articles

import com.isaakhanimann.journal.data.room.articles.entities.CachedArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleDao: ArticleDao
) {

    /**
     * Tries to get the article from cache only.
     */
    suspend fun getCachedArticle(url: String): CachedArticle? = withContext(Dispatchers.IO) {
        articleDao.getArticle(url)
    }

    /**
     * Tries to get the article from cache.
     * If not found, attempts to fetch from network and cache it.
     * Emits the cached article, or throws an error if fetch fails.
     */
    fun getArticle(url: String): Flow<CachedArticle?> = flow {
        // 1. Emit cached content if available
        val cached = articleDao.getArticle(url)
        if (cached != null) {
            emit(cached)
        }

        // 2. Fetch from network
        try {
            val html = fetchHtml(url)
            val newArticle = CachedArticle(
                url = url,
                htmlContent = html,
                lastUpdated = Instant.now()
            )
            articleDao.insertArticle(newArticle)
            emit(newArticle)
        } catch (e: Exception) {
            // Network failed. If we haven't emitted anything yet, rethrow or emit null?
            // If we emitted cache, we just suppress the error or let UI know via a wrapper?
            // For now, simpler approach: if cache exists, we are good (maybe stale).
            // If cache empty, we throw.
            if (cached == null) {
                // If offline and no cache, this will cause the UI to show error/offline message
                throw e
            }
            // If we have cache, we might want to signal that update failed, but for this task
            // "Offline first" means cache is king.
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchHtml(urlString: String): String = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "PsychonautWiki Journal Android App")

        try {
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val rawHtml = connection.inputStream.bufferedReader().use { it.readText() }
                cleanHtml(rawHtml)
            } else {
                throw Exception("HTTP Error: ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun cleanHtml(rawHtml: String): String {
        val doc = org.jsoup.Jsoup.parse(rawHtml)
        
        // 1. Extract Title
        val titleText = doc.selectFirst("#firstHeading")?.text() ?: "Article"

        // 2. Extract Main Content (Strict Selector)
        // PsychonautWiki (MediaWiki) content is inside #mw-content-text > .mw-parser-output
        val contentElement = doc.selectFirst("div#mw-content-text > div.mw-parser-output")
        
        // If strict selector fails, fallback to #mw-content-text or body, but try to be strict first
        val finalContent = contentElement ?: doc.selectFirst("#mw-content-text") ?: doc.body()

        // 3. Clean the Extracted Content
        // Remove TOC, Edit sections, chemical info boxes that might be too complex, etc.
        // We keep it simple: Remove known non-text/interactive elements
        finalContent.select("script, style, .mw-editsection, .toc, #toc, .infobox, .navbox, .metadata").remove()
        
        // Remove images to enforce offline stability (as they are external/relative)
        finalContent.select("img").remove()
        
        // Strip inline styles to ensure our local CSS takes precedence and layout doesn't break
        finalContent.select("*").removeAttr("style")

        // 4. Build Offline HTML
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: sans-serif;
                        line-height: 1.6;
                        padding: 16px;
                        color: #222;
                        background-color: #fcfcfc;
                        max-width: 800px;
                        margin: 0 auto;
                    }
                    h1.article-title { 
                        font-size: 2em; 
                        margin-bottom: 0.5em; 
                        color: #111; 
                        border-bottom: 2px solid #333;
                    }
                    h1, h2, h3 { color: #333; margin-top: 1.5em; margin-bottom: 0.5em; }
                    h2 { border-bottom: 1px solid #eee; padding-bottom: 8px; }
                    p { margin-bottom: 1em; }
                    ul, ol { margin-left: 1.5em; margin-bottom: 1em; }
                    li { margin-bottom: 0.5em; }
                    /* Tables: Force horizontal scroll if too wide */
                    table { display: block; overflow-x: auto; white-space: nowrap; width: 100%; border-collapse: collapse; margin-bottom: 16px; }
                    th, td { padding: 8px; border: 1px solid #ddd; text-align: left; }
                    blockquote { border-left: 4px solid #ddd; padding-left: 16px; color: #555; margin: 16px 0; }
                    .footer-note {
                        margin-top: 48px;
                        padding-top: 16px;
                        border-top: 1px solid #ccc;
                        font-size: 0.8em;
                        color: #666;
                        text-align: center;
                        font-style: italic;
                    }
                </style>
            </head>
            <body>
                <h1 class="article-title">$titleText</h1>
                ${finalContent.html()}
                <div class="footer-note">Source: PsychonautWiki (content adapted for offline reading)</div>
            </body>
            </html>
        """.trimIndent()
    }


    suspend fun getCachedArticleCount(): Int = withContext(Dispatchers.IO) {
        articleDao.getCount()
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        articleDao.deleteAll()
    }

    /**
     * Explicitly fetches and caches the article.
     * Useful for bulk download operations.
     */
    suspend fun fetchAndCache(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val fetchedContent = fetchHtml(url)
            if (fetchedContent != null) {
                val article = CachedArticle(
                     url = url,
                     htmlContent = fetchedContent,
                     lastUpdated = Instant.now()
                )
                articleDao.insertArticle(article)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the timestamp of the most recently updated cached article.
     * Useful for displaying "Last updated" information.
     */
    suspend fun getLastUpdatedTimestamp(): Instant? = withContext(Dispatchers.IO) {
        articleDao.getLastUpdatedTimestamp()
    }
}
