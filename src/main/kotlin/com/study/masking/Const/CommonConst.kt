package com.study.masking.Const

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration


@Configuration
class CommonConst(){

    @Value("\${syotodoke.masking.folder}")
    val folder : String = ""

    @Value("\${syotodoke.masking.file.prefix}")
    val fprefix : String = ""

}