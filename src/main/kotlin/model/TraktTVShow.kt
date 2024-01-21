package com.starsep.myepisodes_kt.model

import kotlinx.serialization.Serializable

@Serializable
data class TraktTVShowIds(
    val trakt: Int,
    val slug: String? = null,
    val tvdb: Int?,
    val imdb: String?,
    val tmdb: Int?,
    val tvrage: Int?,
)

@Serializable
data class TraktTVShow(
    val title: String,
    val year: Int?,
    val ids: TraktTVShowIds,
)

@Serializable
data class TraktTVShowSearchResult(
    val type: String,
    val score: Double,
    val show: TraktTVShow,
)

typealias MyEpisodesTraktMatching = Map<String, Int>