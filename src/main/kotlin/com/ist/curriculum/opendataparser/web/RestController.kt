package com.ist.curriculum.opendataparser.web

import org.edtech.curriculum.Course
import org.edtech.curriculum.Purpose
import org.edtech.curriculum.Subject
import org.edtech.curriculum.SubjectParser
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class RestController {
    private fun getSubjectParser(subjectName: String): SubjectParser {
        return SubjectParser(ClassPathResource("odata/subject/$subjectName.xml").file)
    }

    @GetMapping("/subject/{subjectName}")
    fun subject(@PathVariable subjectName: String): Subject {
        return getSubjectParser(subjectName).getSubject()
    }

    @GetMapping("/subject/{subjectName}/purpouse")
    fun subjectPurpose(@PathVariable subjectName: String): List<Purpose> {
        return getSubjectParser(subjectName).getSubject().purposes
    }

    @GetMapping("/subject/{subjectName}/courses")
    fun courses(@PathVariable subjectName: String): List<Course> {
        return getSubjectParser(subjectName).getCourses() ?: listOf()
    }

    @GetMapping("/subject/{subjectName}/course/{code}")
    fun course(@PathVariable subjectName: String, @PathVariable code: String): Course {
        return getSubjectParser(subjectName).getCourse(code)
    }
}