package com.ist.curriculum.opendataparser.parser
import com.ist.curriculum.opendataparser.Course
import org.dom4j.Element

open class BasicCourseParser(private val CourseElement: Element) {

    open fun getCourse(): Course {
        return Course(
                Companion.extractString(this, "name"),
                Companion.extractString(this, "description").removePrefix("<p>").removeSuffix("</p>"),
                Companion.extractString(this, "code"),
                Companion.extractString(this, "point").toInt(),
                null,
                null
        )
    }

    companion object {
        fun extractString(courseParser: BasicCourseParser, xPathString: String) = courseParser.CourseElement.element(xPathString).stringValue.orEmpty()
    }
}