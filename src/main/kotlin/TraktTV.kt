package com.starsep.myepisodeskt

import CONFIG_FILENAME
import com.starsep.myepisodeskt.config.TraktTVSpec
import com.starsep.myepisodeskt.model.*
import com.starsep.myepisodeskt.network.TokenRequest
import com.starsep.myepisodeskt.network.TokenResponse
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.properties.toProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tongfei.progressbar.ProgressBar
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import kotlin.system.exitProcess

class TraktTV : KoinComponent {
    private val httpClient =
        get<HttpClient>().config {
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
    private val json: Json by inject()

    private suspend fun authorize() {
        val response =
            httpClient.get("/oauth/authorize") {
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

    suspend fun run(
        shows: List<Show>,
        showsData: Map<String, List<Episode>>,
        matchingFile: File,
    ) {
        val accessToken = config[TraktTVSpec.accessToken] ?: requestToken()
        val authorizedHttpClient =
            httpClient.config {
                defaultRequest {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header("trakt-api-version", "2")
                    header("trakt-api-key", config[TraktTVSpec.clientID])
                }
            }
        val existingMatching =
            if (matchingFile.exists()) {
                json.decodeFromString<MyEpisodesTraktMatching>(matchingFile.readText())
            } else {
                emptyMap()
            }
        val newMatching = matchShowsWithMyEpisodes(authorizedHttpClient, shows, showsData, existingMatching)
        matchingFile.writeText(json.encodeToString(newMatching))
        syncEpisodes(authorizedHttpClient, showsData, newMatching)
    }

    private suspend fun matchShowsWithMyEpisodes(
        authorizedHttpClient: HttpClient,
        shows: List<Show>,
        showsData: Map<String, List<Episode>>,
        existingMatching: MyEpisodesTraktMatching,
    ): MyEpisodesTraktMatching {
        val results = existingMatching.toMutableMap()
        ProgressBar.wrap(shows, "Matching shows").forEach { show ->
            if (show.url in results) return@forEach
            val nameWithoutParentheses = show.name.replace(Regex("\\(.*\\)"), "").trim()
            val response =
                authorizedHttpClient.get("/search/show") {
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

    private suspend fun syncEpisodes(
        authorizedHttpClient: HttpClient,
        showsData: Map<String, List<Episode>>,
        matching: MyEpisodesTraktMatching,
    ) {
        val delayDuration = config[TraktTVSpec.delay]
        val matchingById = matching.map { it.key.split("/")[2] to it.value }.toMap()
        ProgressBar.wrap(showsData.keys, "Scrobbling shows").forEach { showId ->
            val traktId = matchingById[showId]
            if (traktId == null) {
                println("No TrackTV matching for $showId")
                return@forEach
            }
            val episodes = showsData[showId]!!
            for (episode in episodes) {
                if (!episode.watched) continue
                val (seasonNumberString, episodeNumberString) = episode.number.split("x")
                val season = seasonNumberString.toIntOrNull()
                val episodeNumber = episodeNumberString.toIntOrNull()
                if (season == null || episodeNumber == null) {
                    println("Invalid MyEpisodes episode number: ${episode.number}")
                    continue
                }
                val episodeResponse = authorizedHttpClient.get("/shows/$traktId/seasons/$season/episodes/$episodeNumber")
                if (episodeResponse.status != HttpStatusCode.OK) {
                    println("Error episode response: $episodeResponse")
                    continue
                }
                val traktTVEpisode = episodeResponse.body<TraktTVEpisode>()
                delay(delayDuration)
                val response =
                    authorizedHttpClient.post("/scrobble/stop") {
                        setBody(TraktTVScrobbleRequest(episode = traktTVEpisode))
                    }
                if (response.status != HttpStatusCode.Created) {
                    println("Error scrobble response: $response")
                }
                delay(delayDuration)
            }
        }
    }

    private suspend fun requestToken(): String {
        val code: String? = config[TraktTVSpec.code]
        if (code == null) {
            authorize()
            return ""
        }
        val response: TokenResponse =
            httpClient.post("/oauth/token") {
                setBody(
                    TokenRequest(
                        code = code,
                        clientID = config[TraktTVSpec.clientID],
                        clientSecret = config[TraktTVSpec.clientSecret],
                    ),
                )
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
