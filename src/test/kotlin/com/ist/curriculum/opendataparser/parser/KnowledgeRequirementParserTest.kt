package com.ist.curriculum.opendataparser.parser

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ist.curriculum.opendataparser.Course
import com.ist.curriculum.opendataparser.GradeStep
import org.dom4j.io.SAXReader
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.DefaultResourceLoader

import org.springframework.test.context.junit4.SpringRunner
import java.io.IOException
import org.springframework.core.io.support.ResourcePatternUtils


@RunWith(SpringRunner::class)
class KnowledgeRequirementParserTest {

    @Throws(IOException::class)
    private fun loadResources(pattern: String): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(DefaultResourceLoader()).getResources(pattern)
    }

    @Test
    fun testTextMatches() {
        assertTrue(textMatches("Eleven förhåller sig konstnärligt till rörelsevokabulär samt varierar och utvecklar rörelseuttryck utifrån",
                               "Eleven förhåller sig konstnärligt till rörelsevokabulär och varierar, och utvecklar rörelseuttryck efter.")
        )
        assertTrue(textMatches("Eleven planerar och organiserar med handledare matlagning.",
                               "Eleven planerar och organiserar matlagning.")
        )
        assertTrue(textMatches("Dessutom beskriver eleven <strong>översiktligt <strong>både enkla och någon mer komplex </strong></strong>språklig struktur i bekanta och mindre bekanta språk samt drar <strong>enkla </strong>slutsatser även i förhållande till sitt modersmål.",
                               "Dessutom beskriver eleven <strong>utförligt både enkla och några mer komplexa </strong>språkliga strukturer i bekanta och mindre bekanta språk samt drar <strong>välgrundade </strong>slutsatser även i förhållande till sitt modersmål.")
        )
        assertFalse(textMatches(" Vid arbete i <strong>bekanta</strong> situationer använder eleven <strong>med säkerhet</strong> instruktioner och utrustningsbeskrivningar.",
                " I arbetet använder eleven i <strong>nya</strong> situationer <strong>med säkerhet </strong>instruktioner och utrustningsbeskrivningar")
        )
    }

    @Test
    fun testGetTextWithoutBoldWords() {
        assertEquals("Eleven förhåller sig konstnärligt till rörelsevokabulär samt varierar och utvecklar rörelseuttryck utifrån ",
                getTextWithoutBoldWords("Eleven förhåller sig <strong>med viss säkerhet </strong>konstnärligt till rörelsevokabulär samt varierar och utvecklar rörelseuttryck utifrån <strong>instruktioner</strong>")
        )
        assertEquals("Eleven förhåller sig konstnärligt till rörelsevokabulär och varierar, och utvecklar rörelseuttryck efter .",
                getTextWithoutBoldWords("Eleven förhåller sig <strong>med god säkerhet </strong>konstnärligt till rörelsevokabulär och varierar, <strong>undersöker </strong>och utvecklar<strong> konsekvent </strong>rörelseuttryck efter <strong>olika krav</strong>.")
        )
    }

    private fun textCourseCode(code: String, xmlFileName: String) {
        val stream = ClassPathResource("$code.json").inputStream
        val referenceObject:Course =  jacksonObjectMapper().readValue(stream)
        val course = getSubjectParser(xmlFileName).getCourse(code)
        assertEquals(referenceObject.name, course.name)
        assertEquals(referenceObject.code, course.code)
        assertEquals(referenceObject.centralContent, course.centralContent)
        for ((index, kn) in (course.knowledgeRequirement?:listOf()).withIndex()) {
            assertEquals(referenceObject.knowledgeRequirement?.get(index), kn)
        }
    }

    @Test
    fun testDansgestaltning() {
        textCourseCode("DAGDAS0", "Dansgestaltning for yrkesdansare")
    }
    @Test
    fun testDansteknik() {
        textCourseCode("DAKKLA02", "Dansteknik for yrkesdansare")
    }

    @Test
    fun testTESPRO01() {
        textCourseCode("TESPRO01", "Tekniska system - VVS")
    }

    @Test
    fun testTravkunskap() {
        textCourseCode("TRVTRA01", "Travkunskap")
    }

    fun testManniskansSprak() {
        textCourseCode("MÄKMÄK02", "Manniskans sprak")
    }

    @Test
    fun testMath() {
        textCourseCode("MATMAT00S", "Matematik")
        textCourseCode("MATMAT01b", "Matematik")
        textCourseCode("MATMAT01c", "Matematik")
        textCourseCode("MATMAT02a", "Matematik")
        textCourseCode("MATMAT02b", "Matematik")
    }
    @Test
    fun testMatlagningskunskap() {
        textCourseCode("MALMAL04", "Matlagningskunskap")
    }

    @Test
    fun testBildteori() {
        textCourseCode("BIDBIT0", "Bildteori")
    }

    @Test
    fun testSparfordon() {
        textCourseCode("SPOSPA0", "Sparfordon")
    }

    @Test
    fun textByggproduktionsledning() {
        textCourseCode("BYPRIT0", "Byggproduktionsledning")
    }

    @Test
    fun parseAllOpenDataStructures() {
         for (res in loadResources("classpath*:odata/subject/*.xml")) {
             val subject = SubjectParser(SAXReader().read(res.inputStream))
             for (course in subject.getCourses()!!) {
                 // Get the fully parsed course
                 val fullCourse = subject.getCourse(course.code)
                 val knList = fullCourse.knowledgeRequirement
                 val combined: MutableMap<GradeStep, StringBuilder> = HashMap()
                 if (knList != null) {
                     for(kn in knList) {
                         for ( (g,s) in kn.knowledgeRequirementChoice) {
                             if (combined.containsKey(g)) {
                                 combined[g]?.append(s)
                             } else {
                                combined[g] = StringBuilder(s)
                             }
                         }
                     }
                 }
                 val cp = subject.getCourseParser(course.code)
                 for( (gradestep, text) in combined) {
                     val textExpected = fixCurriculumErrors(Jsoup.parse(cp.extractKnowledgeRequirementForGradeStep(gradestep)).select("p").html())
                             .replace("  ", " ")
                             .replace("\n", "")
                             .replace("<strong> <italic> .  </italic></strong>", ". ")
                             .removeSuffix("<strong> </strong>")
                     assertEquals(textExpected, text.toString().replace("  ", " "))
                 }

             }
         }
    }
}
