package com.study.masking

import com.study.masking.Component.XMLFileFilter
import com.study.masking.Service.ReadXMLService
import com.study.masking.Const.CommonConst
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class MaskingApplication(private val service : ReadXMLService,private val commonConst: CommonConst,private val  xmlFileFilter: XMLFileFilter) : CommandLineRunner {

	override fun run(vararg args: String?) {

		// マスキング項目を取得
		val maskList : List<String> = service.readSettingFile()

		File(commonConst.folder).listFiles(xmlFileFilter).iterator().forEach {
			service.readXml(maskList,it.name,it.name.replace(commonConst.fprefix,""))
		}

	}
}

fun main(args: Array<String>) {
	runApplication<MaskingApplication>(*args)
}
