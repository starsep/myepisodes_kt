package com.starsep.myepisodes_kt.di

import com.starsep.myepisodes_kt.config.CredentialsSpec
import com.uchuhimo.konf.Config
import org.koin.dsl.module
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.http.userAgent

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
        Config { addSpec(CredentialsSpec) }
            .from.json.file("credentials.json")
    }
}
