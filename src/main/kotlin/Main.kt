package com.starsep.myepisodes_kt

import com.starsep.myepisodes_kt.config.MyEpisodesSpec
import com.starsep.myepisodes_kt.config.OutputSpec
import com.starsep.myepisodes_kt.di.appModule
import com.starsep.myepisodes_kt.model.Episode
import com.starsep.myepisodes_kt.model.MyEpisodesTraktMatching
import com.starsep.myepisodes_kt.model.Show
import com.uchuhimo.konf.Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tongfei.progressbar.ProgressBar
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.io.File


class Runner : KoinComponent {
    private val config: Config by inject()
    private val json: Json by inject()
    private val myEpisodes = MyEpisodes()
    private val traktTV = TraktTV()
    private val outputDirectory = File(config[OutputSpec.directory])
    private val matchingFile = File(outputDirectory, "matching.json")

    suspend fun run() {
        val (shows, showsData) = runMyEpisodes()
        traktTV.run(shows, showsData, matchingFile)
    }

    private suspend fun runMyEpisodes(): Pair<List<Show>, Map<String, List<Episode>>> {
        myEpisodes.login()
        val shows = myEpisodes.listOfShows()
        outputDirectory.mkdirs()
        File(outputDirectory, "shows.json").writeText(
            json.encodeToString(ListSerializer(Show.serializer()), shows)
        )
        val showsData = mutableMapOf<String, List<Episode>>()
        ProgressBar.wrap(shows, "Downloading show").forEach {
            val showFile = File(outputDirectory, "${it.id}.json")
            if (config[MyEpisodesSpec.cached] && showFile.exists()) {
                showsData[it.id] = json.decodeFromString(showFile.readText())
                return@forEach
            }
            val showData = myEpisodes.showData(it)
            delay(config[MyEpisodesSpec.delay])
            File(outputDirectory, "${it.id}.json").writeText(
                json.encodeToString(ListSerializer(Episode.serializer()), showData)
            )
        }
        return shows to showsData
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
