package com.starsep.myepisodes_kt

import CONFIG_FILENAME
import com.starsep.myepisodes_kt.config.TraktTVSpec
import com.starsep.myepisodes_kt.model.Episode
import com.starsep.myepisodes_kt.model.MyEpisodesTraktMatching
import com.starsep.myepisodes_kt.model.Show
import com.starsep.myepisodes_kt.model.TraktTVShowSearchResult
import com.starsep.myepisodes_kt.network.TokenRequest
import com.starsep.myepisodes_kt.network.TokenResponse
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.properties.toProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
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
        install(ContentNegotiation) {
            json()
        }
    }
    private val config: Config by inject()

    private suspend fun authorize() {
        val response = httpClient.get("/oauth/authorize") {
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

    suspend fun run(shows: List<Show>, showsData: Map<String, List<Episode>>, existingMatching: MyEpisodesTraktMatching): MyEpisodesTraktMatching {
        val accessToken = config[TraktTVSpec.accessToken] ?: requestToken()
        val authorizedHttpClient = httpClient.config {
            defaultRequest {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header("trakt-api-version", "2")
                header("trakt-api-key", config[TraktTVSpec.clientID])
            }
        }
        val results = existingMatching.toMutableMap()
        for (show in shows) {
            if (show.url in results) continue
            val nameWithoutParentheses = show.name.replace(Regex("\\(.*\\)"), "").trim()
            val response = authorizedHttpClient.get("/search/show") {
                parameter("query", nameWithoutParentheses)
                parameter("fields", "title")
            }.body<List<TraktTVShowSearchResult>>()
            when (response.size) {
                0 -> println("No results for $nameWithoutParentheses: ${show.url}")
                1 -> {
                    results[show.url] = response[0].show.ids.trakt
                }
                else -> {
                    val myEpisodesFirstEpisodeYear = showsData[show.id]!![0].date.takeLast(4)
                    val bestMatch = response[0]
                    if (bestMatch.show.year?.toString() == myEpisodesFirstEpisodeYear && bestMatch.show.title == nameWithoutParentheses) {
                        results[show.url] = bestMatch.show.ids.trakt
                    } else {
                        println("No good match for $nameWithoutParentheses: ${show.url} $myEpisodesFirstEpisodeYear")
                    }
                }
            }
        }
        return results
    }

    private suspend fun requestToken(): String {
        val code: String? = config[TraktTVSpec.code]
        if (code == null) {
            authorize(); return ""
        }
        val response: TokenResponse = httpClient.post("/oauth/token") {
            setBody(TokenRequest(
                code = code,
                clientID = config[TraktTVSpec.clientID],
                clientSecret = config[TraktTVSpec.clientSecret],
            ))
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.body()
        config[TraktTVSpec.tokenExpiresAt] = response.createdAt + response.expiresIn
        config[TraktTVSpec.accessToken] = response.accessToken
        config[TraktTVSpec.refreshToken] = response.refreshToken
        config.toProperties.toFile(CONFIG_FILENAME)
        return response.accessToken
    }
}