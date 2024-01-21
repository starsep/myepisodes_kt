package com.starsep.myepisodeskt.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val code: String,
    @SerialName("client_id")
    val clientID: String,
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("redirect_uri")
    val redirectUri: String = "urn:ietf:wg:oauth:2.0:oob",
    @SerialName("grant_type")
    val grantType: String = "authorization_code",
)
