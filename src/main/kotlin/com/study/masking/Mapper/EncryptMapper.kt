package com.study.masking.Mapper

import org.apache.ibatis.annotations.Mapper

@Mapper
interface EncryptMapper {

    fun select(param : MutableMap<String,MutableSet<String>>) : Map<String,String>

}