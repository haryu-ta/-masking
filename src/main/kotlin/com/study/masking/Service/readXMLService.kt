package com.study.masking.Service

import com.study.masking.Dto.XMLNode
import com.study.masking.Mapper.EncryptMapper
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


@Component
class ReadXMLService(private val encryptDao : EncryptMapper){

    val FILEPATH : String = "src/main/resources/xml/setup.xml"
    val OUTFILEPATH : String = "src/main/resources/xml/output.xml"

    /**
     * XMLを読み込み
     * @param   暗号化対象項目リスト(テーブル名@項目名)
     */
    fun readXml(maskList: List<String>): Unit{

        val builder : DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document : Document = Paths.get(FILEPATH).toFile().let{builder.parse(it)}
        val nodelist : NodeList = document.documentElement.childNodes as NodeList

        val itemList = mutableListOf<XMLNode>()

        // XML情報取得
        for(i in 0..nodelist.length - 1){

            // ノードの型が『要素』 。かつ、テーブル名が暗号化対象の項目の場合
            if(nodelist.item(i).nodeType == Node.ELEMENT_NODE && maskList.filter { it.startsWith( nodelist.item(i).nodeName.toUpperCase() + "@") }.isNotEmpty() ){

                var tablename : String = nodelist.item(i).nodeName.toUpperCase()

                // テーブル名(カラム名)と設定値(value)を格納するMAP
                val colmap = mutableMapOf<String,String>()

                for(x in 0..nodelist.item(i).attributes.length - 1){
                    val node = nodelist.item(i).attributes.item(x)

                    // マスキング対象項目を含む場合のみ格納
                    if( maskList.filter{ it.equals(tablename + "@" + node.nodeName.toUpperCase())}.isNotEmpty() ) {
                        // 列名と値を格納
                        colmap.put(node.nodeName.toUpperCase(), node.nodeValue)
                    }

                }
                itemList.add(XMLNode(nodelist.item(i).nodeName.toUpperCase(),colmap))
            }

        }

//        // 結果を表示
//        for( item in itemList ){
//            var tablename : String = item.tablename
//            print("${tablename} /")
//
//            for((col,value) in item.itemmap){
//               print(" ${col} = ${value} ")
//            }
//            println("")
//        }

        // 検索したい内容を取得する
        val conditionSets = mutableSetOf<String>()

        // 暗号化対象項目の値をSetに格納
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

    /**
     * 設定ファイルよりマスキング対象項目を取得してListに格納して返却
     * @param  なし
     * @return List（テーブル名@カラム名（全て大文字）)
     */
    fun readSettingFile() : List<String> {

        val settingf : String = "src/main/resources/setting/exclusion.lst"
        val f : BufferedReader = BufferedReader(FileReader(File(settingf)))

        val colList  = mutableListOf<String>()
        f.use{
            it.lineSequence()
                    .filter(String::isNotBlank)
                    .forEach {
                        colList.add(it.toUpperCase())
                    }
        }

        return colList
    }

}