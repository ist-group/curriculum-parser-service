package com.ist.curriculum.opendataparser.diagostics

import org.edtech.curriculum.GradeStep
import org.edtech.curriculum.SubjectParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.regex.Pattern

@Service
class DiagnosticsService {
    @Autowired
    private var resourceLoader: ResourceLoader? = null
    private val missingDotPattern = Pattern.compile("(\\p{Lower})\\p{Blank}+(Vidare|Eleven|Dessutom)", Pattern.UNICODE_CHARACTER_CLASS)

    @Throws(IOException::class)
    fun loadResources(pattern: String): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern)
    }

    fun findMissingDots(): List<KnowledgeRequirementProblem> {
        val paragraphProblems = mutableListOf<KnowledgeRequirementProblem>()
        for (res in loadResources("classpath*:odata/subject/*.xml")) {
            val subject = SubjectParser(res.file)
            for (course in subject.getCourses()!!) {
                val courseParser = subject.getCourseParser(course.code)
                val htmlE = courseParser.extractKnowledgeRequirementForGradeStep(GradeStep.E)
                val htmlC = courseParser.extractKnowledgeRequirementForGradeStep(GradeStep.C)
                val htmlA = courseParser.extractKnowledgeRequirementForGradeStep(GradeStep.A)
                if (hasMissingDots(htmlE)) {
                    paragraphProblems.add(KnowledgeRequirementProblem(
                            "Rad på E-nivå saknar .",
                            subject.name,
                            subject.code,
                            res.filename.removeSuffix(".xml"),
                            course.code,
                            markMissingDots(htmlE),"", ""
                    ))
                }
                if (hasMissingDots(htmlC)) {
                    paragraphProblems.add(KnowledgeRequirementProblem(
                            "Rad på C-nivå saknar .",
                            subject.name,
                            subject.code,
                            res.filename.removeSuffix(".xml"),
                            course.code,
                            "", markMissingDots(htmlC), ""
                    ))
                }
                if (hasMissingDots(htmlA)) {
                    paragraphProblems.add(KnowledgeRequirementProblem(
                            "Rad på A-nivå saknar .",
                            subject.name,
                            subject.code,
                            res.filename.removeSuffix(".xml"),
                            course.code,
                            "","", markMissingDots(htmlA)
                    ))
                }

            }
        }
        return paragraphProblems
    }
    fun findParagraphProblems(): List<KnowledgeRequirementProblem> {
        val paragraphProblems = mutableListOf<KnowledgeRequirementProblem>()
        for (res in loadResources("classpath*:odata/subject/*.xml")) {
            val subject = SubjectParser(res.file)
            for (course in subject.getCourses()!!) {
                val courseParser = subject.getCourseParser(course.code)
                val htmlE = courseParser.extractKnowledgeRequirementForGradeStep(GradeStep.E)
                val htmlC = courseParser.extractKnowledgeRequirementForGradeStep(GradeStep.C)
                val htmlA =courseParser.extractKnowledgeRequirementForGradeStep(GradeStep.A)
                if (!paragraphsCountMatches(htmlE, htmlC, htmlA)) {
                    paragraphProblems.add(KnowledgeRequirementProblem(
                            "Olika antal paragrafer mellan E, C och A nivå",
                            subject.name,
                            subject.code,
                            res.filename.removeSuffix(".xml"),
                            course.code,
                            htmlE, htmlC, htmlA
                    ))
                }
            }
        }
        return paragraphProblems
    }

    fun findKnowledgeRequirementMatchProblems(): List<KnowledgeRequirementProblem> {
        val paragraphProblems = mutableListOf<KnowledgeRequirementProblem>()
        for (res in loadResources("classpath*:odata/subject/*.xml")) {
            val subject = SubjectParser(res.file)
            for (course in subject.getCourses()!!) {
                // Get the fully parsed course
                val fullCourse = subject.getCourse(course.code)
                val knList = fullCourse.knowledgeRequirement
                val combined: MutableMap<GradeStep, StringBuilder> = HashMap()
                var missingText = false
                if (knList != null) {
                    combined[GradeStep.E] = StringBuilder()
                    combined[GradeStep.C] = StringBuilder()
                    combined[GradeStep.A] = StringBuilder()
                    for(kn in knList) {
                        if (kn.knowledgeRequirementChoice[GradeStep.C]?.isEmpty() != false ||
                                kn.knowledgeRequirementChoice[GradeStep.A]?.isEmpty() != false) {
                            missingText = true
                            combined[GradeStep.E]?.append("<p class='error'>${kn.knowledgeRequirementChoice[GradeStep.E]}</p>")
                            combined[GradeStep.C]?.append("<p class='error'>${kn.knowledgeRequirementChoice[GradeStep.C]}</p>")
                            combined[GradeStep.A]?.append("<p class='error'>${kn.knowledgeRequirementChoice[GradeStep.A]}</p>")
                        } else {
                            combined[GradeStep.E]?.append("<p>${kn.knowledgeRequirementChoice[GradeStep.E]}</p>")
                            combined[GradeStep.C]?.append("<p>${kn.knowledgeRequirementChoice[GradeStep.C]}</p>")
                            combined[GradeStep.A]?.append("<p>${kn.knowledgeRequirementChoice[GradeStep.A]}</p>")
                        }
                    }
                    if (missingText) {
                        paragraphProblems.add(KnowledgeRequirementProblem(
                                "Det matchar inte på alla nivåer",
                                subject.name,
                                subject.code,
                                res.filename.removeSuffix(".xml"),
                                course.code,
                                combined[GradeStep.E]?.toString()?:"",
                                combined[GradeStep.C]?.toString()?:"",
                                combined[GradeStep.A]?.toString()?:""
                        ))
                    }
                }
            }
        }
        return paragraphProblems
    }

    private fun hasMissingDots(text: String): Boolean {
        return missingDotPattern.matcher(text).find()
    }
    private fun markMissingDots(text: String): String {
        return missingDotPattern.matcher(text).replaceAll("<span class=\"missing\">$1 $2</span>")
    }

    fun countParagraphs(s: String): Int = s.split(Regex("<p>")).size - 1

    private fun paragraphsCountMatches(htmlE: String, htmlC: String, htmlA: String): Boolean {
        val countE = countParagraphs(htmlE)
        return countE == countParagraphs(htmlC) &&  countE == countParagraphs(htmlA)
    }

    fun findKnowledgeRequirementMerges(): List<KnowledgeRequirementProblem> {
        val paragraphProblems = mutableListOf<KnowledgeRequirementProblem>()
        for (res in loadResources("classpath*:odata/subject/*.xml")) {
            val subject = SubjectParser(res.file)
            for (course in subject.getCourses()!!) {
                // Get the fully parsed course
                val fullCourse = subject.getCourse(course.code)
                val knList = fullCourse.knowledgeRequirement
                var merged = false
                if (knList != null) {
                    knList
                            .filter { kn -> kn.knowledgeRequirementChoice.count { it.value.count { c -> c == '.' } > 1 } > 0 }
                            .forEach { merged = true }
                    if (merged) {
                        paragraphProblems.add(KnowledgeRequirementProblem(
                                "Hittade ihopslagna meningar",
                                subject.name,
                                subject.code,
                                res.filename.removeSuffix(".xml"),
                                course.code,
                                "","",""
                        ))
                    }
                }
            }
        }
        return paragraphProblems
    }
}

data class KnowledgeRequirementProblem(val msg: String, val subject:String, val subjectCode:String, val file:String, val courseCode: String, val levelEHtml: String, val levelCHtml: String, val levelAHtml: String)