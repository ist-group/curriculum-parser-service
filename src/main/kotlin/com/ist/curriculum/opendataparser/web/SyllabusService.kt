package com.ist.curriculum.opendataparser.web

import org.edtech.curriculum.Subject
import org.edtech.curriculum.Curriculum
import org.edtech.curriculum.SchoolType
import org.springframework.stereotype.Service
import java.io.File
import javax.ws.rs.NotFoundException

@Service
class CurriculumService {
    val data = SchoolType.values().mapNotNull {
        try {
            Pair(it, Curriculum(it, File(System.getProperty("curriculum.files_dir", System.getProperty("java.io.tmpdir")))).subjects)
        } catch (e:IllegalArgumentException) {
            null
        }
    }.toMap()

    fun getSubject(schoolType: SchoolType, subjectCode: String): Subject {
        return data[schoolType]?.firstOrNull { it.code == subjectCode }
                ?: throw NotFoundException("Subject not found: $subjectCode")
    }
    fun getSubjects(schoolType: SchoolType): List<Subject> {
        return data[schoolType] ?: throw NotFoundException("School type not found: $schoolType")
    }
}