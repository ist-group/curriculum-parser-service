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
                      private val curriculumService: CurriculumService){

    @GetMapping("/subject/{schoolType}/{subjectName}")
    fun subject(@PathVariable schoolType: SchoolType, @PathVariable subjectName: String): Subject {
        return curriculumService.getSubject(schoolType, subjectName)
    }

    @GetMapping("/subject/{schoolType}/{subjectName}/courses")
    fun courses(@PathVariable schoolType: SchoolType, @PathVariable subjectName: String): List<Course> {
        return  curriculumService.getSubject(schoolType, subjectName).courses
    }

    @GetMapping("/subject/{schoolType}/{subjectName}/course/{code}")
    fun course(@PathVariable schoolType: SchoolType, @PathVariable subjectName: String, @PathVariable code: String): Course {
        return  curriculumService.getSubject(schoolType, subjectName)
                .courses.firstOrNull { it.code == code }
                    ?: throw NotFoundException("Course $code not found in subject $subjectName")
    }
}