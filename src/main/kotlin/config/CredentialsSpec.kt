package com.starsep.myepisodes_kt.config

import com.uchuhimo.konf.ConfigSpec

object CredentialsSpec : ConfigSpec() {
    val username by required<String>()
    val password by required<String>()
}