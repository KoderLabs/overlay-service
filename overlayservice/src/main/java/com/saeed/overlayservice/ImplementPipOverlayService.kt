package com.saeed.overlayservice

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
import com.saeed.overlayservice.helper.VideoPlayerHandler

class ImplementPipOverlayService : PipOverlayService() {

    var videoUrl: String? = null
    var notificationTitle: String? = null
    var notificationDescription: String? = null
    var notificationIcon: Int? = null
    var closeButtonColor: Int? = null
    var closeButtonBg: Int? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        videoUrl = intent?.getStringExtra(KEY_STRING_VIDEO_URL)
        notificationTitle = intent?.getStringExtra(KEY_STRING_NOTIFICATION_TITLE)
        notificationDescription = intent?.getStringExtra(KEY_STRING_NOTIFICATION_DESCRIPTION)
        notificationIcon = intent?.getIntExtra(KEY_INT_NOTIFICATION_ICON, -1)
        closeButtonColor = intent?.getIntExtra(KEY_INT_CLOSE_BUTTON_COLOR, -1)
        closeButtonBg = intent?.getIntExtra(KEY_INT_CLOSE_BUTTON_BG_COLOR, -1)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun getForegroundNotification(): Notification {
        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("my_service", "My Background Service")
                } else {
                    packageName
                }
        var notificationBuilder = NotificationCompat.Builder(this, channelId)

        notificationBuilder = notificationTitle?.let {
            notificationBuilder.setContentTitle(it)
        } ?: run {
            notificationBuilder.setContentTitle("Title")
        }
        notificationDescription?.let {
            notificationBuilder = notificationBuilder.setContentText(it)
        }
        notificationIcon?.let {
            notificationBuilder = notificationBuilder.setSmallIcon(it)
        }

        val notification: Notification = notificationBuilder.build()
        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
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
        return R.layout.pip_layout
    }

    override fun onServiceRun() {
        setOnEventListener(onFullscreen = {
            // Not implemented
        }, onClosed = {
            // Not implemented
        })

        pipView.removeFullscreenButton()

        closeButtonColor?.let {
            pipView.getCloseButton().setColorFilter(it)
        }

        closeButtonBg?.let {
            pipView.getCloseButton().setBackgroundColor(it)
        }

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
        const val KEY_INT_CLOSE_BUTTON_COLOR = "close_button_color"
        const val KEY_INT_CLOSE_BUTTON_BG_COLOR = "close_button_background"
        const val KEY_STRING_NOTIFICATION_TITLE = "notification_title"
        const val KEY_STRING_NOTIFICATION_DESCRIPTION = "notification_description"
        const val KEY_INT_NOTIFICATION_ICON = "notification_icon"
    }
}