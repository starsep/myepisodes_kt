package com.starsep.myepisodes_kt.model

import kotlinx.serialization.Serializable

@Serializable
data class TraktTVEpisode(val season: Int, val number: Int, val title: String, val ids: TraktTVShowIds)