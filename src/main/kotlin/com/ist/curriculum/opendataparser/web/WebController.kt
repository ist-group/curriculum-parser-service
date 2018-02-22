package com.ist.curriculum.opendataparser.web
import com.ist.curriculum.opendataparser.diagostics.DiagnosticsService
import com.ist.curriculum.opendataparser.diagostics.KnowledgeRequirementProblem
import org.edtech.curriculum.Course
import org.edtech.curriculum.PurposeType
import org.edtech.curriculum.SubjectParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class WebController(@Autowired
                    private val diagnosticsService: DiagnosticsService) {
    private fun getSubjectParser(subjectName: String): SubjectParser {
        return SubjectParser(ClassPathResource("odata/subject/$subjectName.xml").file)
    }
    @GetMapping("/")
    fun courseList(model: MutableMap<String, Any>): String {
        val resourceList = ResourcePatternUtils
                .getResourcePatternResolver(DefaultResourceLoader())
                .getResources("classpath*:odata/subject/*.xml")

        val fileList: List<String> = resourceList.map { it -> it.filename.removeSuffix(".xml")  }
        model.put("subjectList", fileList)
        return "subject_list"
    }

    @GetMapping("/subject/{subjectName}")
    fun courses(@PathVariable subjectName: String, model: MutableMap<String, Any>): String {
        // Parse Subject XMl Structure
        val subject = getSubjectParser(subjectName).getSubject()
        model.put("subject", subject)
        model.put("subjectFileName", subjectName)
        model.put("purposeSection", subject.purposes.filter { it.type == PurposeType.SECTION }.map{ it.content })
        model.put("purposeHeader", subject.purposes.filter { it.type == PurposeType.HEADING }.joinToString { it.content })
        model.put("purposeBullets", subject.purposes.filter { it.type == PurposeType.BULLET }.map { it.content })
        model.put("courses", getSubjectParser(subjectName).getCourses() ?: listOf<Course>())
        return "subject"
    }

    @GetMapping("/subject/{subjectName}/course/{code}")
    fun course(@PathVariable subjectName: String, @PathVariable code: String, model: MutableMap<String, Any>): String {
        // Parse Subject XMl Structure
        val sp = getSubjectParser(subjectName)
        // Lookup course with correct code
        model.put("subject", sp.getSubject())
        model.put("course", sp.getCourse(code))
        return "course"
    }

    @GetMapping("/problems")
    fun problems(model: MutableMap<String, Any>): String {
        model.put("dotProblems", diagnosticsService.findMissingDots())
        model.put("matchProblems", diagnosticsService.findKnowledgeRequirementMatchProblems())
        return "diagnostics"
    }

    @GetMapping("/merges")
    fun merges(model: MutableMap<String, Any>): String {
        model.put("dotProblems", listOf<KnowledgeRequirementProblem>())
        model.put("matchProblems", diagnosticsService.findKnowledgeRequirementMerges())
        return "diagnostics"
    }
}