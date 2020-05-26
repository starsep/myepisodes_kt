package com.starsep.myepisodes_kt.model
import kotlinx.serialization.*

@Serializable
data class Show(
    val url: String,
    val name: String
) {
    val id = url.split("/")[2]
}