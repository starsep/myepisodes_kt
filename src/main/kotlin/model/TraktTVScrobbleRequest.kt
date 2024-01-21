package com.starsep.myepisodeskt.model

import kotlinx.serialization.Serializable

@Serializable
data class TraktTVScrobbleRequest(val action: String = "scrobble", val episode: TraktTVEpisode, val progress: Double = 100.0)
