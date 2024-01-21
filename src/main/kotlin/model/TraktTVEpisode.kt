package com.starsep.myepisodeskt.model

import kotlinx.serialization.Serializable

@Serializable
data class TraktTVEpisode(val season: Int, val number: Int, val title: String, val ids: TraktTVShowIds)
