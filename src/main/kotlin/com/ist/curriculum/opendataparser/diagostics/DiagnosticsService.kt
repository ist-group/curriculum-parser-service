package com.ist.curriculum.opendataparser.diagostics

import org.edtech.curriculum.GradeStep
import org.edtech.curriculum.Curriculum
import org.edtech.curriculum.SchoolType
import org.springframework.stereotype.Service

@Service
class DiagnosticsService {
    fun getAllCCHeadings(): List<CCHeading> {
        val result = mutableListOf<CCHeading>()
        for (subject in Curriculum(SchoolType.GY).subjects) {
            for (course in subject.courses) {
                result.addAll(course.centralContent
                        .map{ it.heading}
                        .filter { it.isNotEmpty() }
                        .filter { it != "Undervisningen i kursen ska behandla följande centrala innehåll:" }
                        .map { CCHeading(subject.name, subject.code, course.name, it)}
                )
            }
        }
        return result
    }

    fun findKnowledgeRequirementMatchProblems(): List<KnowledgeRequirementProblem> {
        val paragraphProblems = mutableListOf<KnowledgeRequirementProblem>()
        for (subject in Curriculum(SchoolType.GY).subjects) {
            for (course in subject.courses) {
                // Get the fully parsed course
                val knList = course.knowledgeRequirementParagraphs
                val combined: MutableMap<GradeStep, StringBuilder> = HashMap()
                var missingText = false
                combined[GradeStep.E] = StringBuilder()
                combined[GradeStep.C] = StringBuilder()
                combined[GradeStep.A] = StringBuilder()
                for(knp in knList) {
                   for(kn in knp.knowledgeRequirements) {
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
                }
                if (missingText) {
                    paragraphProblems.add(KnowledgeRequirementProblem(
                            "Det matchar inte på alla nivåer",
                            subject.name,
                            subject.code,
                            "",
                            course.code,
                            combined[GradeStep.E]?.toString()?:"",
                            combined[GradeStep.C]?.toString()?:"",
                            combined[GradeStep.A]?.toString()?:""
                    ))
                }
            }
        }
        return paragraphProblems
    }

    fun findKnowledgeRequirementMerges(): List<KnowledgeRequirementProblem> {
        val paragraphProblems = mutableListOf<KnowledgeRequirementProblem>()
        for (subject in Curriculum(SchoolType.GY).subjects) {
            for (course in subject.courses) {
                // Get the fully parsed course
                val knList = course.knowledgeRequirementParagraphs
                var merged = false
                knList.flatMap { it.knowledgeRequirements }
                        .filter { kn -> kn.knowledgeRequirementChoice.count { it.value.count { c -> c == '.' } > 1 } > 0 }
                        .forEach { merged = true }
                if (merged) {
                    paragraphProblems.add(KnowledgeRequirementProblem(
                            "Hittade ihopslagna meningar",
                            subject.name,
                            subject.code,
                            "",
                            course.code,
                            "","",""
                    ))
                }
            }
        }
        return paragraphProblems
    }
}

data class KnowledgeRequirementProblem(val msg: String, val subject:String, val subjectCode:String, val file:String, val courseCode: String, val levelEHtml: String, val levelCHtml: String, val levelAHtml: String)
data class CCHeading(val subject: String, val subjectCode: String, val course: String, val heading: String)