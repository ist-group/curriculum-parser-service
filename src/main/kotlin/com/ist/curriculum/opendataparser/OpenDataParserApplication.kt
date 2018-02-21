package com.ist.curriculum.opendataparser

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class OpenDataParserApplication

fun main(args: Array<String>) {
    SpringApplication.run(OpenDataParserApplication::class.java, *args)
}
