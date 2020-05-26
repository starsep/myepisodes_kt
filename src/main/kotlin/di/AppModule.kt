package com.starsep.myepisodes_kt.di

import com.starsep.myepisodes_kt.config.CredentialsSpec
import com.starsep.myepisodes_kt.config.OutputSpec
import com.uchuhimo.konf.Config
import org.koin.dsl.module
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.http.userAgent
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.net.InetSocketAddress
import java.net.Proxy

@UnstableDefault
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
                url {
                    host = "www.myepisodes.com"
                    protocol = URLProtocol.HTTPS
                }
                userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36")
            }
        }
    }
    single {
        Config {
            addSpec(CredentialsSpec)
            addSpec(OutputSpec)
        }
            .from.json.file("config.json")
    }
    single {
        Json(
            JsonConfiguration(prettyPrint = true)
        )
    }
}
