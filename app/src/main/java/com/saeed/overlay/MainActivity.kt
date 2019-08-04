package com.saeed.overlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.saeed.overlayservice.ImplementPipOverlayService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_simple_pip.setOnClickListener {
            checkDrawOverlayPermission(IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE)
        }

        btn_custom_pip.setOnClickListener {
            checkDrawOverlayPermission(PIP_OVERLAY_REQUEST_CODE)
        }

        btn_overlay_video_resize.setOnClickListener {
            checkDrawOverlayPermission(RESIZEABLE_CUSTOM_VIDEO_OVERLAY_REQUEST_CODE)
        }
        btn_overlay_web_resize.setOnClickListener {
            checkDrawOverlayPermission(RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE)
        }
    }

    private fun checkDrawOverlayPermission(code: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE)
            } else {
                openFloatingWindow(code)
            }
        } else {
            openFloatingWindow(code)
        }
    }

    private fun openFloatingWindow(code: Int) {
        when (code) {
            IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE -> {
                val intent = Intent(this, ImplementPipOverlayService::class.java)
                val videoUrl =
                    "https://s3.amazonaws.com/data.development.momentpin.com/2019/7/3/1562152168485485-0661a550-9d83-11e9-9028-d7af09cf782e.mp4"
                val notificationTitle = "Pip Overlay"
                val notificationDescription = "Pip overlay description"
                val notificationIcon = R.drawable.ic_launcher_foreground
                val closeBtnColor = android.R.color.black
                val closeBtnBgColor = android.R.color.transparent
                intent.putExtra(ImplementPipOverlayService.KEY_STRING_VIDEO_URL, videoUrl)
                intent.putExtra(ImplementPipOverlayService.KEY_STRING_NOTIFICATION_DESCRIPTION, notificationDescription)
                intent.putExtra(ImplementPipOverlayService.KEY_STRING_NOTIFICATION_TITLE, notificationTitle)
                intent.putExtra(ImplementPipOverlayService.KEY_INT_NOTIFICATION_ICON, notificationIcon)
                intent.putExtra(ImplementPipOverlayService.KEY_INT_CLOSE_BUTTON_COLOR, closeBtnColor)
                intent.putExtra(ImplementPipOverlayService.KEY_INT_CLOSE_BUTTON_BG_COLOR, closeBtnBgColor)
                ContextCompat.startForegroundService(this, intent)
            }
            PIP_OVERLAY_REQUEST_CODE -> {
                val intent = Intent(this, VideoOverlayService::class.java)
                val videoUrl =
                    "https://s3.amazonaws.com/data.development.momentpin.com/2019/7/3/1562152168485485-0661a550-9d83-11e9-9028-d7af09cf782e.mp4"
                intent.putExtra(VideoOverlayService.KEY_STRING_VIDEO_URL, videoUrl)
                ContextCompat.startForegroundService(this, intent)
            }
            RESIZEABLE_CUSTOM_VIDEO_OVERLAY_REQUEST_CODE -> {
                val intent = Intent(this, VideoResizeableOverlayService::class.java)
                val videoUrl =
                    "https://s3.amazonaws.com/data.development.momentpin.com/2019/7/3/1562152168485485-0661a550-9d83-11e9-9028-d7af09cf782e.mp4"
                intent.putExtra(VideoResizeableOverlayService.KEY_STRING_VIDEO_URL, videoUrl)
                ContextCompat.startForegroundService(this, intent)
            }
            RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE -> {
                val intent = Intent(this, WebViewResizeableOverlayService::class.java)
                ContextCompat.startForegroundService(this, intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE, RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE,
            PIP_OVERLAY_REQUEST_CODE, RESIZEABLE_CUSTOM_VIDEO_OVERLAY_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        openFloatingWindow(requestCode)
                    }
                } else {
                    openFloatingWindow(requestCode)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE = 251
        const val PIP_OVERLAY_REQUEST_CODE = 252
        const val RESIZEABLE_CUSTOM_VIDEO_OVERLAY_REQUEST_CODE = 253
        const val RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE = 254
    }

}
