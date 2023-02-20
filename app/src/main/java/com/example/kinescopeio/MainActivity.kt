package com.example.kinescopeio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import java.io.IOException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


object Const {
    private const val VIDEO_ID = "2c017218-2bad-4ace-8077-24dec0262434"
    const val KINESCOPE_URL = "https://kinescope.io/$VIDEO_ID/master.mpd"
    val DRM_SCHEME_UUID = C.WIDEVINE_UUID // The UUID for the Widevine DRM scheme
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playVideo(this, Const.KINESCOPE_URL)

    }

    private fun playVideo(context: Context, url: String) {
        val playerView = findViewById<PlayerView>(R.id.exo_player_view)

//        val drmSessionManager = DefaultDrmSessionManager.Builder()
//            .setUuidAndExoMediaDrmProvider(drmSchemeUuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
//            .setMultiSession(true)
//            .build(mediaDrmCallback)

        val userAgent = "Android 13"
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)


        val mediaSourceFactory = DashMediaSource.Factory(dashChunkSourceFactory, dataSourceFactory)

           // .setDrmSessionManager(drmSessionManager)

       val mediaItem = MediaItem.Builder()
           .setUri(Uri.parse(url))
          // .setMimeType("video/mp4")
           .setDrmConfiguration(
               MediaItem.DrmConfiguration.Builder(Const.DRM_SCHEME_UUID)
                   .setMultiSession(true)
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

    private val mediaDrmCallback = object : MediaDrmCallback {
        override fun executeProvisionRequest(uuid: UUID, request: ExoMediaDrm.ProvisionRequest): ByteArray {
            println("Loge executeProvisionRequest: $uuid")
            println("Loge executeProvisionRequest: ${request.defaultUrl}")
            return executeProvisionRequest(uuid, request)
        }

        override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
            println("Loge executeKeyRequest: $uuid")
            println("Loge executeKeyRequest: ${request.licenseServerUrl}")

            // Return the key response to the MediaDrm instance
            //return sendKeyRequest(request.data)
            return executeKeyRequest(uuid, request)
        }

    }

    private fun sendKeyRequest(request: ByteArray): ByteArray {
        val url = "https://license.kinescope.io/v1/vod/2c017218-2bad-4ace-8077-24dec0262434/acquire/clearkey?token=e2719d58-a985-b3c9-781a-b030af78d30e"
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.setRequestProperty("Content-Length", request.size.toString())
        connection.outputStream.write(request)

        val responseCode = connection.responseCode
        if (responseCode != HttpsURLConnection.HTTP_OK) {
            throw IOException("Key request failed with error code $responseCode, url = ${connection.url}")
        }

        val response = connection.inputStream.readBytes()
        connection.disconnect()
        return response
    }

}







