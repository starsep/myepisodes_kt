package com.starsep.myepisodes_kt

import com.starsep.myepisodes_kt.config.CredentialsSpec
import com.starsep.myepisodes_kt.model.Show
import com.uchuhimo.konf.Config
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.koin.core.KoinComponent
import org.koin.core.inject

class MyEpisodes : KoinComponent {
    private val httpClient: HttpClient by inject()
    private val config: Config by inject()

    suspend fun login() {
        val username = config[CredentialsSpec.username]
        val response = httpClient.post<String>("login.php") {
            body = MultiPartFormDataContent(
                formData {
                    append("username", username)
                    append("password", config[CredentialsSpec.password])
                    append("action", "Login")
                }
            )
        }
        assert(username in response)
    }

    suspend fun listOfShows(): List<Show> = call("life_wasted.php")
        .select("a")
        .filter {
            it.attr("href").startsWith("/epsbyshow/")
        }
        .map {
            Show(it.attr("href"), it.text())
        }

    private suspend fun call(url: String): Document {
        val data = httpClient.get<String>(url)
        return Jsoup.parse(data)
    }
}