package com.starsep.myepisodes_kt.config

import com.uchuhimo.konf.ConfigSpec

object TraktTVSpec : ConfigSpec(prefix = "trakt") {
    val clientID by required<String>()
    val clientSecret by required<String>()
    val code by optional<String?>(default = null)
    val tokenExpiresAt by optional<Int?>(default = null)
    val accessToken by optional<String?>(default = null)
    val refreshToken by optional<String?>(default = null)
    val delay by optional(default = 1000L)
}
