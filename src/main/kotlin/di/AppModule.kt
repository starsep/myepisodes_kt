package com.starsep.myepisodes_kt.di

import CONFIG_FILENAME
import com.starsep.myepisodes_kt.config.MyEpisodesSpec
import com.starsep.myepisodes_kt.config.OutputSpec
import com.starsep.myepisodes_kt.config.TraktTVSpec
import com.uchuhimo.konf.Config
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.http.userAgent
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(Apache) {
            expectSuccess = false
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }
            engine {
                followRedirects = true
            }
            defaultRequest {
                followRedirects = true
                userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36")
            }
        }
    }
    single {
        Config {
            addSpec(MyEpisodesSpec)
            addSpec(OutputSpec)
            addSpec(TraktTVSpec)
        }
            .from.properties.file(CONFIG_FILENAME)
    }
    single {
        Json { prettyPrint = true }
    }
}
