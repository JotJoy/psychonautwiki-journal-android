package com.isaakhanimann.journal.ui.tabs.safer

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalTextApi::class)
object HtmlParser {

    fun parse(html: String): List<ArticleSection> {
        val doc = Jsoup.parse(html)
        // Cleanup first
        doc.select("script, style, .mw-editsection, .toc, .infobox, .navbox, .metadata").remove()
        
        val contentRoot = doc.selectFirst("div#mw-content-text > div.mw-parser-output") 
            ?: doc.body()

        // 1. Flatten the document structure into a linear list of Blocks
        val flatBlocks = mutableListOf<ArticleBlock>()
        contentRoot.children().forEach { child ->
            extractBlocksRecursive(child, flatBlocks)
        }

        // 2. Group Blocks into Sections (Split by H2)
        val sections = mutableListOf<ArticleSection>()
        
        var currentTitle = "Overview"
        var currentSectionBlocks = mutableListOf<ArticleBlock>()

        flatBlocks.forEach { block ->
            if (block is ArticleBlock.Heading && block.level == 2) {
                // Close current section
                if (currentSectionBlocks.isNotEmpty()) {
                    sections.add(ArticleSection(currentTitle, currentSectionBlocks.toList()))
                }
                // Start new section
                currentTitle = block.text
                currentSectionBlocks = mutableListOf()
            } else {
                currentSectionBlocks.add(block)
            }
        }
        
        // Add final section
        if (currentSectionBlocks.isNotEmpty()) {
            sections.add(ArticleSection(currentTitle, currentSectionBlocks.toList()))
        }

        // 3. Fallback: If result is empty or just generic, ensure we return something
        if (sections.isEmpty()) {
             // Should not happen with recursive extraction if there is any P or H2, 
             // but if somehow empty, return raw text as paragraph?
             val rawText = contentRoot.text()
             if (rawText.isNotBlank()) {
                 sections.add(ArticleSection("Content", listOf(ArticleBlock.Paragraph(androidx.compose.ui.text.AnnotatedString(rawText)))))
             }
        }

        return sections
    }

    private fun extractBlocksRecursive(element: Element, targetList: MutableList<ArticleBlock>) {
        // Tag-based handling
        when (element.tagName()) {
            "h2" -> targetList.add(ArticleBlock.Heading(2, cleanText(element)))
            "h3" -> targetList.add(ArticleBlock.Heading(3, cleanText(element)))
            "h4" -> targetList.add(ArticleBlock.Heading(4, cleanText(element)))
            "h5" -> targetList.add(ArticleBlock.Heading(5, cleanText(element)))
            "h6" -> targetList.add(ArticleBlock.Heading(6, cleanText(element)))
            "p" -> {
                val richText = parseRichText(element)
                if (richText.text.isNotBlank()) targetList.add(ArticleBlock.Paragraph(richText))
            }
            "ul" -> {
                val items = element.children().filter { it.tagName() == "li" }
                    .map { parseRichText(it) }
                    .filter { it.text.isNotBlank() }
                if (items.isNotEmpty()) targetList.add(ArticleBlock.BulletList(items))
            }
            "ol" -> {
                val items = element.children().filter { it.tagName() == "li" }
                    .map { parseRichText(it) }
                    .filter { it.text.isNotBlank() }
                if (items.isNotEmpty()) targetList.add(ArticleBlock.OrderedList(items))
            }
            "div" -> {
                // Is this an image wrapper?
                if (element.hasClass("thumb") || element.hasClass("tright") || element.hasClass("tleft")) {
                    val imgBlock = parseImage(element)
                    if (imgBlock != null) targetList.add(imgBlock)
                } else if (isCalloutCandidate(element)) {
                     // Treat as Callout
                     processCallout(element, targetList)
                } else {
                    // Recurse for generic divs (like column wrappers)
                    if (element.childrenSize() > 0) {
                        element.children().forEach { child -> extractBlocksRecursive(child, targetList) }
                    }
                }
            }
            "table" -> {
                if (isCalloutCandidate(element)) {
                    processCallout(element, targetList)
                } else {
                    // If it's a data table, we might want to recurse to extract text?
                    // For now, flattening tables often results in mess. 
                    // But blocking them hides content.
                    // Let's recurse safely.
                     if (element.childrenSize() > 0) {
                        element.children().forEach { child -> extractBlocksRecursive(child, targetList) }
                    }
                }
            }
            "img" -> {
                 val imgBlock = parseImage(element) // Direct image
                 if (imgBlock != null) targetList.add(imgBlock)
            }
            "blockquote" -> {
                val richText = parseRichText(element)
                 if (richText.text.isNotBlank()) targetList.add(ArticleBlock.Callout(richText, CalloutType.INFO))
            }
            else -> {
                 // Recurse unknown containers
                 if (element.childrenSize() > 0) {
                     element.children().forEach { child -> extractBlocksRecursive(child, targetList) }
                 }
            }
        }
    }

    private fun isCalloutCandidate(element: Element): Boolean {
        val classAndStyle = element.className() + " " + element.attr("style")
        if (classAndStyle.contains("messagebox", ignoreCase = true) || 
            classAndStyle.contains("warning", ignoreCase = true) || 
            classAndStyle.contains("alert", ignoreCase = true) ||
            classAndStyle.contains("background", ignoreCase = true)) return true
            
        // Text heuristics for unstyled tables
        val text = element.text().lowercase()
        // Shorter length check to avoid false positives on long paragraphs
        if (text.length < 500) {
            if (text.contains("fatal overdose") || text.contains("risk of death") || text.contains("strongly discouraged")) return true
        }
        return false
    }

    private fun processCallout(element: Element, targetList: MutableList<ArticleBlock>) {
        val text = parseRichText(element)
        if (text.text.isNotBlank()) {
             val rawText = element.text().lowercase()
             val type = when {
                 rawText.contains("fatal") || rawText.contains("death") || rawText.contains("overdose") || rawText.contains("danger") -> CalloutType.DANGER
                 rawText.contains("warning") || rawText.contains("caution") || rawText.contains("risk") -> CalloutType.WARNING
                 else -> CalloutType.INFO
             }
             targetList.add(ArticleBlock.Callout(text, type))
        }
    }

    private fun cleanText(element: Element): String {
        return element.text().trim()
    }

    private fun parseRichText(element: Element): androidx.compose.ui.text.AnnotatedString {
        return androidx.compose.ui.text.buildAnnotatedString {
            parseRichTextRecursive(element, this)
        }
    }

    private fun parseRichTextRecursive(element: org.jsoup.nodes.Node, builder: androidx.compose.ui.text.AnnotatedString.Builder) {
        if (element is TextNode) {
            builder.append(element.text())
            return
        }

        if (element is Element) {
            when (element.tagName()) {
                "br", "li", "tr", "div", "p" -> {
                    // Block elements implicitly imply newlines if they aren't the first thing
                    if (builder.length > 0 && !builder.toString().endsWith("\n")) {
                         builder.append("\n")
                    }
                    element.childNodes().forEach { child -> parseRichTextRecursive(child, builder) }
                    // Double newline for paragraphs? Maybe just one.
                } 
                "a" -> {
                    val href = element.attr("href")
                    var url = href
                    if (href.startsWith("/")) {
                        url = "https://psychonautwiki.org$href"
                    }
                    
                    if (url.isNotBlank()) {
                        builder.pushUrlAnnotation(androidx.compose.ui.text.UrlAnnotation(url))
                        // Add style (underlined only, color handled by UI)
                        builder.withStyle(androidx.compose.ui.text.SpanStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )) {
                             element.childNodes().forEach { child -> parseRichTextRecursive(child, builder) }
                        }
                        builder.pop()
                    } else {
                        element.childNodes().forEach { child -> parseRichTextRecursive(child, builder) }
                    }
                }
                "b", "strong", "th" -> { // Table Headers usually bold
                    builder.withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                        element.childNodes().forEach { child -> parseRichTextRecursive(child, builder) }
                    }
                }
                "i", "em" -> {
                    builder.withStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                         element.childNodes().forEach { child -> parseRichTextRecursive(child, builder) }
                    }
                }
                else -> {
                    // Default traversal
                    element.childNodes().forEach { child -> parseRichTextRecursive(child, builder) }
                }
            }
        }
    }

    private fun parseImage(element: Element): ArticleBlock.Image? {
        val img = if (element.tagName() == "img") element else element.selectFirst("img") ?: return null
        
        var src = img.attr("src")
        if (src.isBlank()) return null
        
        // Resolve relative URLs
        if (src.startsWith("/")) {
            src = "https://psychonautwiki.org$src"
        } else if (!src.startsWith("http")) {
            return null // Skip weird schemes
        }

        // Filtering
        // Skip obvious icons/UI
        if (src.contains("pixel") || src.contains("Bit.png") || src.contains("icon")) return null
        
        // Width check - User asked for >= 150px
        val widthStr = img.attr("width")
        if (widthStr.isNotBlank()) {
            val width = widthStr.toIntOrNull()
            if (width != null && width < 150) return null // Too small
        }

        // Caption logic
        var caption: String? = null
        if (element.hasClass("thumb")) {
            caption = element.select(".thumbcaption").text()
        }
        if (caption.isNullOrBlank()) {
             caption = img.attr("alt").takeIf { it.isNotBlank() }
        }

        return ArticleBlock.Image(src, caption)
    }
}
