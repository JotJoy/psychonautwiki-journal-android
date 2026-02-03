package com.isaakhanimann.journal.ui.tabs.safer

import androidx.compose.ui.text.AnnotatedString

sealed class ArticleBlock {
    data class Heading(val level: Int, val text: String) : ArticleBlock()
    data class Paragraph(val content: AnnotatedString) : ArticleBlock()
    data class BulletList(val items: List<AnnotatedString>) : ArticleBlock()
    data class OrderedList(val items: List<AnnotatedString>) : ArticleBlock()
    data class Image(val url: String, val caption: String?) : ArticleBlock()
    data class Callout(val content: AnnotatedString, val type: CalloutType) : ArticleBlock()
}

enum class CalloutType {
    INFO, WARNING, DANGER
}

data class ArticleSection(
    val title: String,
    val content: List<ArticleBlock>,
    val isExpanded: Boolean = false // Default state
)
