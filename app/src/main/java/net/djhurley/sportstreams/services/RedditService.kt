package net.djhurley.sportstreams.services

import com.beust.klaxon.JsonArray
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.beust.klaxon.JsonObject
import com.beust.klaxon.lookup
import net.djhurley.sportstreams.models.Stream
import java.util.regex.Pattern

/**
 * Created by DJHURLEY on 05/12/2017.
 */
class RedditService {

    fun getSoccerStreams() : List<Stream> {
        val (_, _, result) = "https://www.reddit.com/r/soccerstreams/top/.json".httpGet()
                .header(Pair("User-agent", "net.djhurley.sportstreams"))
                .responseJson()

        val parser = Parser()
        val stringBuilder = StringBuilder( result.get().content)
        val json = parser.parse(stringBuilder) as JsonObject

        val leagues = json.lookup<String>("data.children.data.link_flair_text")
        val alternativeLeagues = json.lookup<String>("data.children.data.selftext")
        val timesAndTitles = json.lookup<String>("data.children.data.title")
        val urls = json.lookup<String>("data.children.data.url")

        return leagues.indices.map { Stream(leagues.getOrNull(it) ?: alternativeLeagues[it],
                timesAndTitles[it].split("[")[1].split("] ")[0],
                timesAndTitles[it].split("[")[1].split("] ")[1],
                getImageUrl(leagues.getOrNull(it) ?: alternativeLeagues[it]),
                urls[it] + ".json") }
    }

    fun getSoccerStreamLinks(url: String) : List<String> {
        val links = ArrayList<String>()

        val (_, _, result) = url.httpGet()
                .header(Pair("User-agent", "net.djhurley.sportstreams"))
                .responseJson()

        val parser = Parser()
        val stringBuilder = StringBuilder( result.get().content)
        val jsonArray = parser.parse(stringBuilder) as JsonArray<*>
        val json = jsonArray[1] as JsonObject

        val bodies = json.lookup<String>("data.children.data.body")
        val filteredBodies = bodies.filter { it.contains("acestream://") }
        filteredBodies.forEach { body: String -> links.addAll("acestream://[a-z0-9]+".toRegex().find(body)?.groupValues as Collection<String>) }

        return links
    }

    private fun getImageUrl(league: String): String {
        return if(league.contains("premier", true) && league.contains("league", true)) {
            "https://pbs.twimg.com/media/C-Fmi52WsAAXE4S.jpg"
        } else if(league.contains("la", true) && league.contains("liga", true)) {
            "https://asset-sports.abs-cbn.com/web/dev/articles/1486607600_la-liga-logo.png"
        } else if(league.contains("fa", true) && league.contains("cup", true)) {
            "https://upload.wikimedia.org/wikipedia/en/thumb/b/b4/Fa_cup.svg/1200px-Fa_cup.svg.png"
        } else if(league.contains("primeira", true)) {
            "https://statics.sportskeeda.com/wp-content/uploads/2014/12/portguguese-league-1417503042.jpg"
        } else if(league.contains("Coupe", true) && league.contains("France", true)) {
            "http://cdn.caughtoffside.com/wp-content/uploads/2011/01/coupe1.jpg"
        } else {
            "https://images7.alphacoders.com/411/411202.jpg"
        }
    }
}