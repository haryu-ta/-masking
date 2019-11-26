package com.study.masking.Component

import com.study.masking.Const.CommonConst
import org.springframework.stereotype.Component
import java.io.File
import java.io.FilenameFilter

@Component
class XMLFileFilter(private val commonConst: CommonConst) : FilenameFilter {
    override fun accept(dir: File?, name: String?): Boolean {
        if( name != null && name.startsWith(commonConst.fprefix) ){
            return true
        }
        return false
    }

}