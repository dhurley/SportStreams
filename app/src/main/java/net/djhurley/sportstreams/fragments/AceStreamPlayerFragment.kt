package net.djhurley.sportstreams.fragments

import android.os.Bundle
import android.support.v17.leanback.app.VideoSupportFragment
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost
import android.support.v17.leanback.media.MediaPlayerGlue
import android.support.v17.leanback.media.PlaybackGlue
import net.djhurley.sportstreams.activities.StreamDetailsActivity
import net.djhurley.sportstreams.models.Stream

/**
 * Created by DJHURLEY on 07/01/2018.
 */
class AceStreamPlayerFragment : VideoSupportFragment() {

    private lateinit var mTransportControlGlue: MediaPlayerGlue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stream = activity?.intent?.getSerializableExtra(StreamDetailsActivity.STREAM) as Stream

        val glueHost = VideoSupportFragmentGlueHost(this@AceStreamPlayerFragment)
        mTransportControlGlue = MediaPlayerGlue(activity)
        mTransportControlGlue.setMode(MediaPlayerGlue.NO_REPEAT)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                glue?.let {
                    if (it.isPrepared) {
                        it.play()
                    }
                }
            }
        })
        mTransportControlGlue.setTitle(stream.title)
        mTransportControlGlue.setArtist(stream.league)
        mTransportControlGlue.setVideoUrl(stream.url)
    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }
}