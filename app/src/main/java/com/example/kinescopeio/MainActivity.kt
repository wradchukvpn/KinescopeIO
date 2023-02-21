package com.example.kinescopeio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


object Const {
    // https://kinescope.io/7b6f3aa3-8554-43c3-abd7-a3a17fa32f04/master.mpd

    private const val ID = "7b6f3aa3-8554-43c3-abd7-a3a17fa32f04"
    private const val VIDEO_ID_DEMO_PROJECT = "2c017218-2bad-4ace-8077-24dec0262434"
    private const val VIDEO_ID = VIDEO_ID_DEMO_PROJECT

    const val KINESCOPE_URL = "https://kinescope.io/$VIDEO_ID/master.mpd"
    const val LICENSE_SERVER = "https://license.kinescope.io/v1/vod/$VIDEO_ID/acquire/widevine"
    val DRM_SCHEME_UUID = C.CLEARKEY_UUID // The UUID for the Widevine DRM scheme
    const val USER_AGENT = "Android 13"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playVideo(this, Const.KINESCOPE_URL)

    }

    private fun playVideo(context: Context, url: String) {
        val playerView = findViewById<StyledPlayerView>(R.id.exo_player_view)

//        val drmSessionManager = DefaultDrmSessionManager.Builder()
//            .setUuidAndExoMediaDrmProvider(drmSchemeUuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
//            .setMultiSession(true)
//            .build(mediaDrmCallback)

        val userAgent = "Android 13"
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Const.USER_AGENT)
            .setAllowCrossProtocolRedirects(true)

        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)


        val mediaSourceFactory = DashMediaSource.Factory(dashChunkSourceFactory, dataSourceFactory)

       val mediaItem = MediaItem.Builder()
           .setUri(Uri.parse(url))
           .setMimeType("video/mp4")
           .setDrmConfiguration(
               MediaItem.DrmConfiguration.Builder(Const.DRM_SCHEME_UUID)
                   .setMultiSession(true)
                   .setScheme(Const.DRM_SCHEME_UUID)
                   //.setLicenseUri(Const.LICENSE_SERVER)
                   .build()
           )
           .build()
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        val player = ExoPlayer.Builder(context).build()
        player.addListener( object: Player.Listener {
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
        player.playWhenReady = false
        playerView.player = player
        player.setMediaSource(mediaSource)
        player.prepare()

    }

}