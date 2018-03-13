package com.ist.curriculum.opendataparser.web
import com.ist.curriculum.opendataparser.diagostics.DiagnosticsService
import com.ist.curriculum.opendataparser.diagostics.KnowledgeRequirementProblem
import org.edtech.curriculum.Course
import org.edtech.curriculum.PurposeType
import org.edtech.curriculum.SkolverketFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.io.File


@Controller
class WebController(@Autowired
                    private val diagnosticsService: DiagnosticsService) {

    val tempDir = File(System.getProperty("java.io.tmpdir"))

    @GetMapping("/")
    fun courseList(model: MutableMap<String, Any>): String {
        model["subjectListGY"] = SkolverketFile.GY.subjectNames(tempDir)
        model["subjectListVUXGR"] = SkolverketFile.VUXGR.subjectNames(tempDir)
        return "subject_list"
    }

    @GetMapping("/subject/{schoolForm}/{subjectName}")
    fun courses(@PathVariable schoolForm: SkolverketFile, @PathVariable subjectName: String, model: MutableMap<String, Any>): String {
        // Parse Subject XMl Structure
        val subjectParser = schoolForm.openSubject(subjectName, tempDir)
        val subject = subjectParser.getSubject()
        model["schoolForm"] = schoolForm.name
        model["subjectFileName"] = subjectName
        model["subject"] = subject
        model["purposeSection"] = subject.purposes.filter { it.type == PurposeType.SECTION }.map{ it.content }
        model["purposeHeader"] = subject.purposes.filter { it.type == PurposeType.HEADING }.joinToString { it.content }
        model["purposeBullets"] = subject.purposes.filter { it.type == PurposeType.BULLET }.map { it.content }
        model["courses"] = subjectParser.getSubject().courses
        return "subject"
    }

    @GetMapping("/subject/{schoolForm}/{subjectName}/course/{code}")
    fun course(@PathVariable schoolForm: SkolverketFile, @PathVariable subjectName: String, @PathVariable code: String, model: MutableMap<String, Any>): String {
        // Parse Subject XMl Structure
        val subjectParser = schoolForm.openSubject(subjectName, tempDir)
        // Lookup course with correct code
        model["schoolForm"] = schoolForm.name
        model["subjectFileName"] = subjectName
        model["subject"] = subjectParser.getSubject()
        model["course"] = subjectParser.getSubject().courses.firstOrNull { it.code == code } ?: Course("No course extracted", "", "error", 0)
        return "course"
    }

    @GetMapping("/problems")
    fun problems(model: MutableMap<String, Any>): String {
        model["schoolForm"] = "GY"
        model["dotProblems"] = listOf<KnowledgeRequirementProblem>() //diagnosticsService.findMissingDots()
        model["matchProblems"] = diagnosticsService.findKnowledgeRequirementMatchProblems()
        return "diagnostics"
    }

    @GetMapping("/cc-headings")
    fun ccHeadings(model: MutableMap<String, Any>): String {
        model["headings"] = diagnosticsService.getAllCCHeadings()
        return "cc-headings"
    }

    @GetMapping("/merges")
    fun merges(model: MutableMap<String, Any>): String {
        model["schoolForm"] = "GY"
        model["dotProblems"] = listOf<KnowledgeRequirementProblem>()
        model["matchProblems"] = diagnosticsService.findKnowledgeRequirementMerges()
        return "diagnostics"
    }
}