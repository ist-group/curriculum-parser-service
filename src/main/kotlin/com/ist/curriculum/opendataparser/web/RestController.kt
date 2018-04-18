package com.ist.curriculum.opendataparser.web

import org.edtech.curriculum.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.ws.rs.NotFoundException

@RestController
@RequestMapping("api")
class RestController (@Autowired
                      private val syllabusService: SyllabusService){

    @GetMapping("/subject/{schoolForm}/{subjectName}")
    fun subject(@PathVariable schoolForm: SyllabusType, @PathVariable subjectName: String): Subject {
        return syllabusService.getSubject(schoolForm, subjectName)
    }

    @GetMapping("/subject/{schoolForm}/{subjectName}/courses")
    fun courses(@PathVariable schoolForm: SyllabusType, @PathVariable subjectName: String): List<Course> {
        return  syllabusService.getSubject(schoolForm, subjectName).courses
    }

    @GetMapping("/subject/{schoolForm}/{subjectName}/course/{code}")
    fun course(@PathVariable schoolForm: SyllabusType, @PathVariable subjectName: String, @PathVariable code: String): Course {
        return  syllabusService.getSubject(schoolForm, subjectName)
                .courses.firstOrNull { it.code == code }
                    ?: throw NotFoundException("Course $code not found in subject $subjectName")
    }
}