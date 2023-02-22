package com.example.kinescopeio

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


object Const {
    // https://kinescope.io/7b6f3aa3-8554-43c3-abd7-a3a17fa32f04/master.mpd

    private const val ID = "7b6f3aa3-8554-43c3-abd7-a3a17fa32f04" // todo видео предоставленное поддержкой
    private const val VIDEO_ID_DEMO_PROJECT = "2c017218-2bad-4ace-8077-24dec0262434" // todo Наше видео
    private const val VIDEO_ID = ID

    const val KINESCOPE_URL = "https://kinescope.io/$VIDEO_ID/master.mpd"
    const val LICENSE_SERVER = "https://license.kinescope.io/v1/vod/$VIDEO_ID/acquire/widevine"
    //const val LICENSE_SERVER = "https://license.kinescope.io"
    val DRM_SCHEME_UUID = C.WIDEVINE_UUID // The UUID for the Widevine DRM scheme
    const val USER_AGENT = "Android"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerView = findViewById<StyledPlayerView>(R.id.exo_player_view)
        val player = ExoPlayer.Builder(this).build()

        val showPlayer = findViewById<TextView>(R.id.showPlayer)

        showPlayer.setOnClickListener {
            playerView.visibility = View.VISIBLE
            player.playerListener()
            val mediaSource = getMediaSource()
            player.addMediaSource(playerView, mediaSource)
        }


    }



    /**
     * Тестовая функция
     */
    private fun getMediaSource(): MediaSource {

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Const.USER_AGENT)
            .setAllowCrossProtocolRedirects(true)

        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)

        val mediaSourceFactory = DashMediaSource.Factory(dashChunkSourceFactory, dataSourceFactory)

       val mediaItem = MediaItem.Builder()
           .setUri(Uri.parse(Const.KINESCOPE_URL))
           .setMimeType("video/mp4")
           .setDrmConfiguration(
               MediaItem.DrmConfiguration.Builder(Const.DRM_SCHEME_UUID)
                   .setMultiSession(true)
                   .setScheme(Const.DRM_SCHEME_UUID)
                   .setLicenseUri(Const.LICENSE_SERVER)
                   .build()
           )
           .build()

        return mediaSourceFactory.createMediaSource(mediaItem)

    }

    private fun ExoPlayer.playerListener() {
        this.addListener( object: Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                println("Loge onPlayerError: ${error.message}")
                super.onPlayerError(error)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                error?.let {
                    println("Loge onPlayerErrorChanged: ${it.message}")
                }

                super.onPlayerErrorChanged(error)
            }

        })
    }
    private fun ExoPlayer.addMediaSource(playerView: StyledPlayerView, mediaSource: MediaSource) {
        this.playWhenReady = false
        playerView.player = this
        this.setMediaSource(mediaSource)
        this.prepare()
    }

}