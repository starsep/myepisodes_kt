package com.starsep.myepisodes_kt

import com.starsep.myepisodes_kt.di.appModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }
    val myEpisodes = MyEpisodes()
    runBlocking {
        myEpisodes.login()
        print(myEpisodes.listOfShows())
    }
}