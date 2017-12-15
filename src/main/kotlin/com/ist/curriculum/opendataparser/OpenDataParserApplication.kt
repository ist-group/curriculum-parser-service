package com.ist.curriculum.opendataparser

import com.ist.curriculum.opendataparser.parser.HtmlParser
import com.ist.curriculum.opendataparser.parser.SubjectParser
import com.ist.curriculum.opendataparser.parser.getSubjectParser
import org.dom4j.io.SAXReader
import org.jsoup.Jsoup
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.core.io.ClassPathResource


@SpringBootApplication
class OpenDataParserApplication

fun main(args: Array<String>) {
    SpringApplication.run(OpenDataParserApplication::class.java, *args)
}
