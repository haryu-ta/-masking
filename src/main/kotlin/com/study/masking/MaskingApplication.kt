package com.study.masking

import com.study.masking.Service.ReadXMLService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MaskingApplication(private val service : ReadXMLService) : CommandLineRunner {

	override fun run(vararg args: String?) {
		service.readXml()
	}
}

fun main(args: Array<String>) {
	runApplication<MaskingApplication>(*args)
}
