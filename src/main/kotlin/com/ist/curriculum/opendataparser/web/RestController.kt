package com.ist.curriculum.opendataparser.web

import org.edtech.curriculum.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import javax.ws.rs.NotFoundException

@RestController
@RequestMapping("api")
class RestController {
    val cacheDir = File(System.getProperty("java.io.tmpdir"))

    @GetMapping("/subject/{schoolForm}/{subjectName}")
    fun subject(@PathVariable schoolForm: SkolverketFile, @PathVariable subjectName: String): Subject {
        return schoolForm.openSubject(subjectName, cacheDir).getSubject()
    }

    @GetMapping("/subject/{schoolForm}/{subjectName}/purpouse")
    fun subjectPurpose(@PathVariable schoolForm: SkolverketFile, @PathVariable subjectName: String): List<Purpose> {
        return schoolForm.openSubject(subjectName, cacheDir).getSubject().purposes
    }
    @GetMapping("/subject/{schoolForm}/{subjectName}/courses")
    fun courses(@PathVariable schoolForm: SkolverketFile, @PathVariable subjectName: String): List<Course> {
        return schoolForm.openSubject(subjectName, cacheDir).getSubject().courses
    }

    @GetMapping("/subject/{schoolForm}/{subjectName}/course/{code}")
    fun course(@PathVariable schoolForm: SkolverketFile, @PathVariable subjectName: String, @PathVariable code: String): Course {
        return schoolForm.openSubject(subjectName, cacheDir).getSubject().courses.firstOrNull() { it.code == code } ?: throw NotFoundException()
    }
}