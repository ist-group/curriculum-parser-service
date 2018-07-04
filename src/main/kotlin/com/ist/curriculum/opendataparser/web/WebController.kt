package com.ist.curriculum.opendataparser.web
import com.ist.curriculum.opendataparser.diagostics.DiagnosticsService
import com.ist.curriculum.opendataparser.diagostics.KnowledgeRequirementProblem
import org.edtech.curriculum.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


data class NameAndCode(val name: String,val code: String)

@Controller
class WebController(@Autowired
                    private val diagnosticsService: DiagnosticsService,
                    @Autowired
                    private val curriculumService: CurriculumService) {

    val subjectNamesPerSchoolForm: Map<SchoolType, List<NameAndCode>> = SchoolType.values()
            .map {
                Pair(it, curriculumService.getSubjects(it)
                        .map { NameAndCode(it.name, it.code) }
                        .sortedBy { it.name }
                        .toList())
            }.toMap()

    @GetMapping("/")
    fun courseList(model: MutableMap<String, Any>): String {
        model["syllabuses"] = subjectNamesPerSchoolForm
        model["schoolType"] = SchoolType.GR
        return "subject_list"
    }

    @GetMapping("/subject/{schoolType}")
    fun subjects(@PathVariable schoolType: SchoolType, model: MutableMap<String, Any>): String {
        // Parse Subject XMl Structure
        model["syllabuses"] = subjectNamesPerSchoolForm
        model["schoolType"] = schoolType
        return "subject_list"
    }

    @GetMapping("/subject/{schoolType}/{subjectCode}")
    fun subject(@PathVariable schoolType: SchoolType, @PathVariable subjectCode: String, model: MutableMap<String, Any>): String {
        // Parse Subject XMl Structure
        model["subject"] = curriculumService.getSubject(schoolType, subjectCode)
        model["schoolType"] = schoolType
        model["syllabuses"] = subjectNamesPerSchoolForm
        return "subject"
    }

    @GetMapping("/problems")
    fun problems(model: MutableMap<String, Any>): String {
        model["schoolType"] = "GY"
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
        model["schoolType"] = "GY"
        model["dotProblems"] = listOf<KnowledgeRequirementProblem>()
        model["matchProblems"] = diagnosticsService.findKnowledgeRequirementMerges()
        return "diagnostics"
    }
}