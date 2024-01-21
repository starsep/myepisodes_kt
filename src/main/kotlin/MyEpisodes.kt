package com.starsep.myepisodeskt

import com.starsep.myepisodeskt.config.MyEpisodesSpec
import com.starsep.myepisodeskt.model.Episode
import com.starsep.myepisodeskt.model.Show
import com.uchuhimo.konf.Config
import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class MyEpisodes : KoinComponent {
    private val httpClient =
        get<HttpClient>().config {
            defaultRequest {
                url {
                    host = "www.myepisodes.com"
                    protocol = URLProtocol.HTTPS
                }
            }
        }
    private val config: Config by inject()

    suspend fun login() {
        val username = config[MyEpisodesSpec.username]
        val response =
            httpClient.post("login.php") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("username", username)
                            append("password", config[MyEpisodesSpec.password])
                            append("action", "Login")
                        },
                    ),
                )
            }.bodyAsText()
        assert(username in response)
    }

    suspend fun listOfShows(): List<Show> =
        httpClient.get("life_wasted.php").bodyAsText().toDocument()
            .select("a")
            .filter { it.attr("href").startsWith("/epsbyshow/") }
            .map {
                Show(it.attr("href"), it.text())
            }

    suspend fun showData(show: Show): List<Episode> {
        val document =
            httpClient.post("/ajax/service.php") {
                parameter("mode", "view_epsbyshow")
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("showid", show.id)
                        },
                    ),
                )
            }.bodyAsText().toDocument()
        return document.select("tr")
            .filter { "odd" in it.classNames() || "even" in it.classNames() }
            .map {
                val statuses = it.select("td.status")
                Episode(
                    date = it.selectFirst("td.date")!!.child(0).text(),
                    showName = show.name,
                    showId = show.id,
                    number = it.selectFirst("td.longnumber")!!.text(),
                    name = it.selectFirst("td.epname")!!.text(),
                    acquired = statuses[0].child(0).hasAttr("checked"),
                    watched = statuses[1].child(0).hasAttr("checked"),
                )
            }
    }

    private fun String.toDocument(): Document = Jsoup.parse(this)
}
