package com.starsep.myepisodeskt.config

import com.uchuhimo.konf.ConfigSpec

object MyEpisodesSpec : ConfigSpec(prefix = "myepisodes") {
    val username by required<String>()
    val password by required<String>()
    val cached by optional(default = false)
    val delay by optional(default = 300L)
}
