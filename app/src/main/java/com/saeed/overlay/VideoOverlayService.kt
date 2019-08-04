package com.saeed.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ui.PlayerView
import com.saeed.overlayservice.PipOverlayService
import com.saeed.overlayservice.helper.VideoPlayerHandler
import com.saeed.overlayservice.toDp

class VideoOverlayService : PipOverlayService() {
    var videoUrl: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        videoUrl = intent?.getStringExtra(KEY_STRING_VIDEO_URL)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun getForegroundNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                packageName
            }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Video Overlay")
            .build()

        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun getInitialWindowSize(): Point {
        return Point(200.toDp(), 200.toDp())
    }

    override fun getCustomLayoutId(): Int {
        return R.layout.video_overlay_layout
    }

    override fun onServiceRun() {
        setOnEventListener(onFullscreen = {
            // Not implemented
        }, onClosed = {
            // Not implemented
        })

        playVideo()
    }

    private fun playVideo() {
        val playerView = getView().findViewById<PlayerView>(R.id.player)

        val exoPlayerHandler =
            VideoPlayerHandler.Builder(this).create()

        videoUrl?.let {
            exoPlayerHandler.startVideo(playerView, it)
        } ?: run {
            Toast.makeText(this, "No video url found", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val KEY_STRING_VIDEO_URL = "video_url"
    }

}