package com.starsep.myepisodeskt.model
import kotlinx.serialization.*

@Serializable
data class Show(
    val url: String,
    val name: String,
) {
    val id = url.split("/")[2]
}
