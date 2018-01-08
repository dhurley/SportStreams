package net.djhurley.sportstreams.models

import java.io.Serializable

/**
 * Created by DJHURLEY on 11/12/2017.
 */
data class Stream(val league: String, val time: String, val title: String, val imageUrl: String, val url: String): Serializable