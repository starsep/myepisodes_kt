package com.starsep.myepisodes_kt.config

import com.uchuhimo.konf.ConfigSpec

object OutputSpec : ConfigSpec() {
    val directory by optional(default = "output")
}