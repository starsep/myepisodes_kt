package com.starsep.myepisodes_kt

import com.starsep.myepisodes_kt.config.OutputSpec
import com.starsep.myepisodes_kt.di.appModule
import com.starsep.myepisodes_kt.model.Episode
import com.starsep.myepisodes_kt.model.Show
import com.uchuhimo.konf.Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import me.tongfei.progressbar.ProgressBar
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import java.io.File


class Runner : KoinComponent {
    private val config: Config by inject()
    private val json: Json by inject()
    private val myEpisodes = MyEpisodes()
    private val outputDirectory = File(config[OutputSpec.directory])

    suspend fun run() {
        myEpisodes.login()
        val shows = myEpisodes.listOfShows()
        outputDirectory.mkdirs()
        File(outputDirectory, "shows.json").writeText(
            json.stringify(Show.serializer().list, shows)
        )
        ProgressBar.wrap(shows, "Downloading show").forEach {
            val showData = myEpisodes.showData(it)
            delay(300) // TODO: configurable
            File(outputDirectory, "${it.id}.json").writeText(
                json.stringify(Episode.serializer().list, showData)
            )
        }
    }
}

fun main() {
    startKoin {
        modules(appModule)
    }
    runBlocking {
        Runner().run()
    }
}