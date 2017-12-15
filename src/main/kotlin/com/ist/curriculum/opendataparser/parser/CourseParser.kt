package com.ist.curriculum.opendataparser.parser

import com.ist.curriculum.opendataparser.CentralContent
import com.ist.curriculum.opendataparser.Course
import com.ist.curriculum.opendataparser.GradeStep
import com.ist.curriculum.opendataparser.KnowledgeRequirement
import org.dom4j.Element
import org.jsoup.Jsoup

class CourseParser(private val CourseElement: Element): BasicCourseParser(CourseElement) {
    fun extractKnowledgeRequirementForGradeStep(gradeStep: GradeStep): String {
        CourseElement.elements("knowledgeRequirements")
            .filterIsInstance<Element>()
            .filter { it.element("gradeStep").stringValue == gradeStep.name }
            .forEach { return it.element("text").stringValue }
        return ""
    }
    private fun getCentralContent(): List<CentralContent> {
        return HtmlParser()
            .toCentralContent(Jsoup.parse(
                CourseElement.element("centralContent")
                    .stringValue
                    .orEmpty()
            ))
    }
    private fun getKnowledgeRequirements(): List<KnowledgeRequirement>? {
        return KnowledgeRequirementParser().getKnowledgeRequirements(
                extractKnowledgeRequirementForGradeStep(GradeStep.E),
                extractKnowledgeRequirementForGradeStep(GradeStep.C),
                extractKnowledgeRequirementForGradeStep(GradeStep.A)
        )
    }
    override fun getCourse(): Course {
        val basicCourse = super.getCourse()
        return Course(
                basicCourse.name,
                basicCourse.description,
                basicCourse.code,
                basicCourse.point,
                this.getCentralContent(),
                this.getKnowledgeRequirements()
        )
    }
}