package com.study.masking.Service

import com.study.masking.Dto.XMLNode
import com.study.masking.Mapper.EncryptMapper
import com.study.masking.Const.CommonConst
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
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


@Component
class ReadXMLService(private val encryptDao : EncryptMapper,private val commonConst : CommonConst){

    /**
     * XMLを読み込み
     * @param   暗号化対象項目リスト(テーブル名@項目名)
     */
    fun readXml(maskList: List<String>,filename : String,newfilename : String): Unit{

        val builder : DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document : Document = Paths.get(commonConst.folder + "/" + filename).toFile().let{builder.parse(it)}
        val nodeList : NodeList = document.documentElement.childNodes as NodeList

        val itemList = mutableListOf<Map<String,String>>()

        // XML情報取得
        for(i in 0 until nodeList.length){

            // ノードの型が『要素』 。かつ、テーブル名が暗号化対象の項目の場合
            if(nodeList.item(i).nodeType == Node.ELEMENT_NODE && maskList.filter { it.startsWith( nodeList.item(i).nodeName.toUpperCase() + "@") }.isNotEmpty() ){

                var tablename : String = nodeList.item(i).nodeName.toUpperCase()

                // テーブル名(カラム名)と設定値(value)を格納するMAP
                val colmap = mutableMapOf<String,String>()

                // 属性値分のループ処理
                for(x in 0 until nodeList.item(i).attributes.length ){
                    val node = nodeList.item(i).attributes.item(x)

                    // マスキング対象項目を含む場合のみ格納
                    if( maskList.filter{ it.equals(tablename + "@" + node.nodeName.toUpperCase())}.isNotEmpty() ) {
                        // 列名と値を格納
                        colmap.put(node.nodeName.toUpperCase(), node.nodeValue)
                    }

                }
                itemList.add(colmap)
            }

        }

//        // 結果を表示
//        for( item in itemList ){
//
//            for((col,value) in item.entries){
//               print(" ${col} = ${value} ")
//            }
//            println("")
//        }

        // 検索したい内容を取得する
        val conditionSets = mutableSetOf<String>()

        // 暗号化対象項目の値をSetに格納
        for(list in itemList){
            for(value in list.values){
                conditionSets.add(value)
            }
        }

        // SQL実行
        val map = mutableMapOf("conditionItem" to conditionSets )
        // 暗号化前の値と暗号化後の値を格納したMap
        val encryptMap =  encryptDao.select(map)

        // Setをクリア
        conditionSets.clear()

        // XML情報編集
        for(i in 0 until nodeList.length){

            if(nodeList.item(i).nodeType == Node.ELEMENT_NODE && maskList.filter { it.startsWith( nodeList.item(i).nodeName.toUpperCase() + "@") }.isNotEmpty()){

                var tablename : String = nodeList.item(i).nodeName.toUpperCase()

                for(x in 0 until nodeList.item(i).attributes.length){
                    val node = nodeList.item(i).attributes.item(x)

                    // マスキング対象項目を含む場合のみ格納
                    if( maskList.filter{ it.equals(tablename + "@" + node.nodeName.toUpperCase())}.isNotEmpty() ) {
                        var text = nodeList.item(i).attributes.item(x).nodeValue
                        nodeList.item(i).attributes.item(x).nodeValue = encryptMap.get(text)
                    }
                }
            }
        }

        // CSV書出
        val tfFactory = TransformerFactory.newInstance()
        val tf = tfFactory.newTransformer()
        tf.transform(DOMSource(document), StreamResult(File(commonConst.folder + "/" + newfilename)))
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

        //        val map = colList.groupBy ({
        //            it -> it.split("@")[0]
        //        },{it.split("@")[1]})
        //
        //        for((key,content) in map.entries ){
        //            println("=====${key}=====")
        //            for(item in content){
        //                println(item)
        //            }
        //        }

        return colList
    }

}