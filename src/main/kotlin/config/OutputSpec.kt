package com.starsep.myepisodes_kt.config

import com.uchuhimo.konf.ConfigSpec

object OutputSpec : ConfigSpec(prefix = "output") {
    val directory by optional(default = "output")
}