package com.starsep.myepisodes_kt.model
import kotlinx.serialization.*

@Serializable
data class Episode(
    val date: String,
    val showName: String,
    val showId: String,
    val number: String,
    val name: String,
    val acquired: Boolean,
    val watched: Boolean
)