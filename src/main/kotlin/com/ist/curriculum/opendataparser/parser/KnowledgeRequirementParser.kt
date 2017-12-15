package com.ist.curriculum.opendataparser.parser

import com.ist.curriculum.opendataparser.GradeStep
import com.ist.curriculum.opendataparser.KnowledgeRequirement
import info.debatty.java.stringsimilarity.NormalizedLevenshtein
import org.jsoup.Jsoup
import kotlin.math.min

fun fixCurriculumErrors(text: String): String {
    return text
            .replace(Regex("(?<=[a-zåäö]) (Vidare|Eleven|Dessutom)"), ". $1")
            .replace("&lt;" ,"<")
            .replace("</strong><strong>", "")
            .replace(Regex("<strong> </strong>"), "")
            .replace("<strong><italic>. </italic></strong>", ". ")
            .replace("<strong> <italic>  .  </italic></strong>", ". ")
            .replace("<br/>", " ")
            .replace("<br>", " ").trim()
}


/**
 * Count the number of words that matches, value needs to be over 80% to count as a match
 */
fun textMatches(text1: String, text2: String): Boolean {
    if (text2.trim().isBlank() || text1.trim().isBlank()) return false

    val similarityThreshold = 0.8
    val r = Regex("[\\s,.-]+")
    val wordList1  = getTextWithoutBoldWords(text1).trim().split(r).filter { it.isNotEmpty() }
    val wordList2  = getTextWithoutBoldWords(text2).trim().split(r).filter { it.isNotEmpty() }

    val minLength = min(wordList1.size, wordList2.size)

    val matchesWordCount = wordList1.filterIndexed { index, word -> wordList2.contains(word) && wordList2.indexOf(word)  - index in -2..2 } .size
    if (matchesWordCount.toDouble() / minLength.toDouble() > similarityThreshold) {
        return true
    }
    val l = NormalizedLevenshtein()
    if (l.similarity(wordList1.joinToString(" "),  wordList2.joinToString(" ")) > 0.8 ) {
        return true
    }
    return false
}

/**
 * Removes all delimiters and bold words
 */
fun getTextWithoutBoldWords(htmlText: String): String {
    return htmlText
            .replace(Regex("<strong> [^>]* </strong>"), " ")
            .replace(Regex("<strong>[^>]*</strong>"), "")
}


class KnowledgeRequirementParser {

    /**
     * Replaces the bold words with ________
     */
    private fun getPlaceHolderText(htmlText: String): String {
        return htmlText
                .replace(Regex("<strong>[^>]*</strong>"), "<strong>________</strong>")
    }
    /**
     * Get all top level paragraphs from an html string
     */
    private fun getParagraphs(html: String): List<String> {
        return Jsoup.parse(html).select("p").map { it.html() }.filter { it.trim().isNotEmpty() }
    }
    private fun splitParagraph(text: String): List<String> {
        return text.split(Regex("(?<=\\.)")).toList().filter { it.trim().isNotEmpty() }
    }

    /**
     * Create a working structure based on the e-level paragraphs and lines
     */
    private fun eLevelToBaseKnowledgeRequirements(eLevelHtml: String): List<KnowledgeRequirement> {
        val knowledgeRequirements = ArrayList<KnowledgeRequirement>()
        val eLevelParagraphs = getParagraphs(fixCurriculumErrors(eLevelHtml))

        for ((paragraphNo, eParagraph) in eLevelParagraphs.withIndex()) {
            // Map the the data object structure
            splitParagraph(eParagraph)
                    .toList()
                    .mapIndexedTo(knowledgeRequirements) { kkrNo, krText ->
                        KnowledgeRequirement(
                            // Generate Placeholder from E level
                            getPlaceHolderText(krText),
                            kkrNo,
                            paragraphNo,
                            mapOf(Pair(GradeStep.E, krText), Pair(GradeStep.C, ""), Pair(GradeStep.A, "")).toMutableMap()
                    )
            }
        }
        return knowledgeRequirements
    }

    private fun commitLines(text: String, lineNo: Int, knowledgeRequirements: List<KnowledgeRequirement>, gradeStep: GradeStep) {
        // Append to existing
        val index = if (lineNo >= 0) lineNo  else 0
        if (knowledgeRequirements.size > lineNo) {
            if (knowledgeRequirements[index].knowledgeRequirementChoice.containsKey(gradeStep)) {
                knowledgeRequirements[index].knowledgeRequirementChoice[gradeStep] =
                        knowledgeRequirements[index].knowledgeRequirementChoice[gradeStep] + text
            } else {
                knowledgeRequirements[index].knowledgeRequirementChoice[gradeStep] = text
            }
        } else {
            throw Error("Cannot commit lines outside range..")
        }
    }

    private fun addGradeStep(knowledgeRequirements: List<KnowledgeRequirement>, html: String, gradeStep: GradeStep) {
        // Convert all html paragraphs to a flat line of texts
        val unmappedLines: MutableList<String> = ArrayList()

        fun matchLineToLineNo(line: String, mappedLineNo: Int): Boolean {
            if (mappedLineNo < 0 || mappedLineNo >= knowledgeRequirements.size) {
                return false
            }
            if ( textMatches(knowledgeRequirements[mappedLineNo].knowledgeRequirementChoice[GradeStep.E] ?: "", line) ||
                    (gradeStep == GradeStep.A && textMatches(knowledgeRequirements[mappedLineNo].knowledgeRequirementChoice[GradeStep.C] ?: "", line)) ) {
                // Commit unmapped lines to previous row
                if (unmappedLines.isNotEmpty()) {
                    commitLines(unmappedLines.joinToString(""), mappedLineNo - 1, knowledgeRequirements, gradeStep)
                    unmappedLines.clear()
                }

                // Add the matched line
                commitLines(line, mappedLineNo, knowledgeRequirements, gradeStep)
                return true
            }
            return false
        }

        var mappedLineNo = 0
        for (line in getParagraphs(html)
                .map { splitParagraph(it) }
                .flatten()) {
            // Does the current or next line match
            when {
                matchLineToLineNo(line, mappedLineNo) -> mappedLineNo++
                matchLineToLineNo(line, mappedLineNo + 1) -> mappedLineNo += 2
                else -> unmappedLines.add(line)
            }
        }

        if (unmappedLines.size > 0) {
            // Add the extra rest to the last line that did match
            if (unmappedLines.size > 0) {
                commitLines(unmappedLines.joinToString(""), knowledgeRequirements.size - 1, knowledgeRequirements, gradeStep)
            }
        }
    }

    fun getKnowledgeRequirements(eLevelHtml: String, cLevelHtml: String, aLevelHtml: String): List<KnowledgeRequirement> {
        val knowledgeRequirements = eLevelToBaseKnowledgeRequirements(fixCurriculumErrors(eLevelHtml))

        // Map other levels into the existing structure
        addGradeStep(knowledgeRequirements, fixCurriculumErrors(cLevelHtml), GradeStep.C)
        addGradeStep(knowledgeRequirements, fixCurriculumErrors(aLevelHtml), GradeStep.A)

        return knowledgeRequirements
    }
}

