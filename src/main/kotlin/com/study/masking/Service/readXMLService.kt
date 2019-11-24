package com.study.masking.Service

import com.study.masking.Dto.XMLNode
import com.study.masking.Mapper.EncryptMapper
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


@Component
class ReadXMLService(private val encryptDao : EncryptMapper){

    val FILEPATH : String = "src/main/resources/xml/setup.xml"
    val OUTFILEPATH : String = "src/main/resources/xml/output.xml"

    fun readXml () : Unit{

        val builder : DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document : Document = Paths.get(FILEPATH).toFile().let{builder.parse(it)}
        val nodelist : NodeList = document.documentElement.childNodes as NodeList

        val itemList = mutableListOf<XMLNode>()

        // XML情報取得
        for(i in 0..nodelist.length - 1){

            if(nodelist.item(i).nodeType == Node.ELEMENT_NODE ){

                val colmap = mutableMapOf<String,String>()

                for(x in 0..nodelist.item(i).attributes.length - 1){
                    val node = nodelist.item(i).attributes.item(x)
                    // 列名と値を格納
                    colmap.put(node.nodeName.toUpperCase(),node.nodeValue)
                }

                itemList.add(XMLNode(nodelist.item(i).nodeName.toUpperCase(),colmap))

            }

        }

        // 結果を表示
        for( item in itemList ){
            var tablename : String = item.tablename
            print("${tablename} /")

            for((col,value) in item.itemmap){
               print(" ${col} = ${value} ")
            }
            println("")
        }

        // 検索したい内容を取得する
        val conditionSets = mutableSetOf<String>()

        for(list in itemList){
            for(value in list.itemmap.values){
                conditionSets.add(value)
            }
        }

        // SQL実行
        val map = mutableMapOf("conditionItem" to conditionSets )
        val result =  encryptDao.select(map)

        for((col,values) in result.entries){
            println("${col} : ${values}")
        }

//        // XML情報編集
//        for(i in 0..nodelist.length - 1){
//
//            if(nodelist.item(i).nodeType == Node.ELEMENT_NODE ){
//
//                for(x in 0..nodelist.item(i).attributes.length - 1){
//
//                    nodelist.item(i).attributes.item(x).nodeValue = "ccc"
//
//                }
//
//            }
//
//        }
//
//        // CSV書出
//        val tfFactory = TransformerFactory.newInstance()
//        val tf = tfFactory.newTransformer()
//        tf.transform(DOMSource(document),StreamResult(File(OUTFILEPATH)))
    }

}