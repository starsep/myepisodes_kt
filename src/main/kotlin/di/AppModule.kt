package com.starsep.myepisodeskt.di

import CONFIG_FILENAME
import com.starsep.myepisodeskt.config.MyEpisodesSpec
import com.starsep.myepisodeskt.config.OutputSpec
import com.starsep.myepisodeskt.config.TraktTVSpec
import com.uchuhimo.konf.Config
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.userAgent
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule =
    module {
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
