package com.starsep.myepisodeskt.config

import com.uchuhimo.konf.ConfigSpec

object OutputSpec : ConfigSpec(prefix = "output") {
    val directory by optional(default = "output")
}
