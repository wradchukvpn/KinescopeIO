package com.example.kinescopeio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


object Const {
    private const val VIDEO_ID = "2c017218-2bad-4ace-8077-24dec0262434"
    const val KINESCOPE_URL = "https://kinescope.io/$VIDEO_ID/master.mpd"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playVideo(this, Const.KINESCOPE_URL)

    }

    private fun playVideo(context: Context, url: String) {
        val playerView = findViewById<PlayerView>(R.id.exo_player_view)

        val drmSchemeUuid = C.PLAYREADY_UUID // The UUID for the Widevine DRM scheme

        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(drmSchemeUuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .setMultiSession(true)
            .build(mediaDrmCallback)

        val userAgent = "Android 13"
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)


        val mediaSourceFactory = DashMediaSource.Factory(dashChunkSourceFactory, dataSourceFactory)
            .setDrmSessionManager(drmSessionManager)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(url))

        val player = SimpleExoPlayer.Builder(context).build()
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

    private val drmEventListener = object : DrmSessionEventListener {
        override fun onDrmSessionAcquired(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            super.onDrmSessionAcquired(windowIndex, mediaPeriodId)
        }

        override fun onDrmSessionAcquired(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, state: Int) {
            super.onDrmSessionAcquired(windowIndex, mediaPeriodId, state)
        }

        override fun onDrmKeysLoaded(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            super.onDrmKeysLoaded(windowIndex, mediaPeriodId)
        }

        override fun onDrmSessionManagerError(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, error: Exception) {
            super.onDrmSessionManagerError(windowIndex, mediaPeriodId, error)
        }

        override fun onDrmKeysRestored(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            super.onDrmKeysRestored(windowIndex, mediaPeriodId)
        }

        override fun onDrmKeysRemoved(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            super.onDrmKeysRemoved(windowIndex, mediaPeriodId)
        }

        override fun onDrmSessionReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            super.onDrmSessionReleased(windowIndex, mediaPeriodId)
        }

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
            throw IOException("Key request failed with error code $responseCode")
        }

        val response = connection.inputStream.readBytes()
        connection.disconnect()
        return response
    }

}







