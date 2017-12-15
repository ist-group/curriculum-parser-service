package com.ist.curriculum.opendataparser.parser

import com.ist.curriculum.opendataparser.*
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.jsoup.Jsoup
import org.springframework.core.io.ClassPathResource

fun getSubjectParser(subjectName: String): SubjectParser {
    // Load XML Data
    val resource = ClassPathResource("odata/subject/$subjectName.xml")
    // Parse Subject XMl Structure
    return SubjectParser(SAXReader().read(resource.inputStream))
}

class SubjectParser(private val openDataDocument: Document) {
    private fun extractString(xPathString: String): String = openDataDocument.selectSingleNode("/*[name()='subject']/*[name()='$xPathString']" ).stringValue.orEmpty()
    private fun extractNodes(xPathString: String): MutableList<Any?>? = openDataDocument.selectNodes("/*[name()='subject']/*[name()='$xPathString']" )

    val name: String = extractString("name")
    val description: String = extractString("description").removePrefix("<p>").removeSuffix("</p>")
    val code: String = extractString("code")
    val purpose: String = extractString("purpose")
    val applianceDate: String = extractString("applianceDate")

    fun getSubject(): Subject {
        val doc = Jsoup.parse(purpose)
        return Subject(name, description, code, HtmlParser().toPurposes(doc))
    }

    fun getCourses(): List<Course>? {
        // Get the list of courses and return as CoursePOJOs
        val nodes: MutableList<Any?>? = extractNodes("courses")
        return nodes?.map { BasicCourseParser(it as Element).getCourse() }?.toList()
    }
    fun getCourse(code: String):Course {
        return getCourseParser(code).getCourse()
    }
    fun getCourseParser(code: String):CourseParser {
        // Get the list of courses and return as CoursePOJOs
        val element = extractNodes("courses")
                ?.filterIsInstance<Element>()?.first { it.element("code").stringValue == code } ?: throw Error("Coruse with code:$code not found")
        return CourseParser(element)
    }
}

