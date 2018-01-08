package net.djhurley.sportstreams.services

import org.junit.Assert
import org.junit.Test

/**
 * Created by DJHURLEY on 05/12/2017.
 */
class RedditServiceTest {

    @Test
    fun getSoccerStreams() {
        val streams = RedditService().getSoccerStreams()
        Assert.assertNotNull(streams)
    }

    @Test
    fun getSoccerStream() {
        val url = "https://www.reddit.com/r/soccerstreams/comments/7or6oc/1730_gmt_athletic_club_vs_deportivo_alaves/.json"
        val links = RedditService().getSoccerStreamLinks(url)
        Assert.assertNotNull(links)
    }
}