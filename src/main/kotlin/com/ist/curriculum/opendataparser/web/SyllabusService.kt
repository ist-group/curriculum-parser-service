package com.ist.curriculum.opendataparser.web

import org.edtech.curriculum.Subject
import org.edtech.curriculum.Syllabus
import org.edtech.curriculum.SyllabusType
import org.springframework.stereotype.Service
import java.io.File
import javax.ws.rs.NotFoundException

@Service
class SyllabusService {
    val data = SyllabusType.values().mapNotNull {
        try {
            Pair(it, Syllabus(it, File(System.getProperty("curriculum.files_dir", System.getProperty("java.io.tmpdir")))).getSubjects())
        } catch (e:IllegalArgumentException) {
            null
        }
    }.toMap()

    fun getSubject(syllabusType: SyllabusType, subjectCode: String): Subject {
        return data[syllabusType]?.firstOrNull { it.code == subjectCode }
                ?: throw NotFoundException("Subject not found: $subjectCode")
    }
    fun getSubjects(syllabusType: SyllabusType): List<Subject> {
        return data[syllabusType] ?: throw NotFoundException("Syllabus type not found: $syllabusType")
    }
}