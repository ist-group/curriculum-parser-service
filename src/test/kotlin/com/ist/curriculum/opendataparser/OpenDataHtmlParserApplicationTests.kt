package com.ist.curriculum.opendataparser

import com.ist.curriculum.opendataparser.web.RestController
import com.ist.curriculum.opendataparser.web.WebController
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.beans.factory.annotation.Autowired



@RunWith(SpringRunner::class)
@SpringBootTest
class OpenDataHtmlParserApplicationTests {
	@Autowired
	private val controller: WebController? = null
	@Autowired
	private val restController: RestController? = null

	@Test
	fun contextLoads() {
		assertNotNull(controller)
		assertNotNull(restController)
	}

}
