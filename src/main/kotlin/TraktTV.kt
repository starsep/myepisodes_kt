package com.starsep.myepisodes_kt

import CONFIG_FILENAME
import com.starsep.myepisodes_kt.config.TraktTVSpec
import com.starsep.myepisodes_kt.network.TokenRequest
import com.starsep.myepisodes_kt.network.TokenResponse
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.properties.toProperties
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.util.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import kotlin.system.exitProcess

class TraktTV : KoinComponent {
    private val httpClient = get<HttpClient>().config {
        defaultRequest {
            url {
                host = "api.trakt.tv"
                protocol = URLProtocol.HTTPS
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
    private val config: Config by inject()

    private suspend fun authorize() {
        val response = httpClient.get<HttpResponse>("/oauth/authorize") {
            parameter("response_type", "code")
            parameter("client_id", config[TraktTVSpec.clientID])
            parameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
        }
        println("Visit this url in a browser")
        println(response.call.request.url)
        println("Input code in config")
        println("And relaunch program")
        exitProcess(0)
    }

    suspend fun run() {
        val accessToken = config[TraktTVSpec.accessToken] ?: requestToken()
        val authorizedHttpClient = httpClient.config {
            defaultRequest {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header("trakt-api-version", "2")
                header("trakt-api-key", config[TraktTVSpec.clientID])
            }
        }
    }

    private suspend fun requestToken(): String {
        val code: String? = config[TraktTVSpec.code]
        if (code == null) {
            authorize(); return ""
        }
        val response = httpClient.post<TokenResponse>("/oauth/token") {
            body = TokenRequest(
                code = code,
                clientID = config[TraktTVSpec.clientID],
                clientSecret = config[TraktTVSpec.clientSecret],
            )
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
        config[TraktTVSpec.tokenExpiresAt] = response.createdAt + response.expiresIn
        config[TraktTVSpec.accessToken] = response.accessToken
        config[TraktTVSpec.refreshToken] = response.refreshToken
        config.toProperties.toFile(CONFIG_FILENAME)
        return response.accessToken
    }
}