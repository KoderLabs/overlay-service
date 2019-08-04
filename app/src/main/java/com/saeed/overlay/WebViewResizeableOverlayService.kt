package com.saeed.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import com.saeed.overlayservice.ResizeableOverlayService
import com.saeed.overlayservice.gone
import com.saeed.overlayservice.toDp
import com.saeed.overlayservice.visible

class WebViewResizeableOverlayService : ResizeableOverlayService() {

    private lateinit var webView: WebView
    private lateinit var webToolBar: ConstraintLayout
    private lateinit var imageReloadButton: ImageView
    private lateinit var imageBackwardButton: ImageView
    private lateinit var imageForwardButton: ImageView
    private lateinit var imageCloseLinkButton: ImageView
    private lateinit var imageGoButton: ImageView
    private lateinit var textLink: TextView
    private lateinit var editLink: EditText
    private lateinit var webProgress: ProgressBar

    override fun getForegroundNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                packageName
            }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Resizeable Video Overlay")
            .setContentText("Resizeable Video Overlay Description")
            .build()

        return notification
    }

    override fun getMinimumWindowSize(): Point {
        return Point(150.toDp(), 150.toDp())
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
        return R.layout.resizeable_web_overlay_layout
    }

    override fun onServiceRun() {
        setOnEventListener(onFullscreen = {
            // Not implemented
        }, onClosed = {
            // Not implemented
        })

        webView = getView().findViewById(R.id.webView)

        webToolBar = getView().findViewById(R.id.web_tool_bar)
        imageReloadButton = getView().findViewById(R.id.image_reload)
        imageBackwardButton = getView().findViewById(R.id.image_backward)
        imageForwardButton = getView().findViewById(R.id.image_forward)
        imageCloseLinkButton = getView().findViewById(R.id.image_close_link)
        textLink = getView().findViewById(R.id.text_link)
        imageGoButton = getView().findViewById(R.id.image_go)
        editLink = getView().findViewById(R.id.edit_link)
        webProgress = getView().findViewById(R.id.web_progress_bar)

        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
        webView.apply {
            settings.javaScriptEnabled = true
        }
        textLink.text = "https://www.google.com"
        webView.loadUrl("https://www.google.com")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    webProgress.visible()
                    webProgress.progress = newProgress
                } else {
                    webProgress.gone()
                }
            }
        }

        setForwardPageButtonEnable(false)
        setBackwardPageButtonEnable(false)

        imageReloadButton.setOnClickListener {
            webView.reload()
        }

        imageBackwardButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
                textLink.text = webView.originalUrl
            }
            setForwardPageButtonEnable(true)
        }

        imageForwardButton.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
                textLink.text = webView.originalUrl
            }
            setBackwardPageButtonEnable(true)
        }

        imageCloseLinkButton.setOnClickListener {
            hideEditLink()
        }

        textLink.setOnClickListener {
            visibleEditLink()
            editLink.setText(textLink.text)
            editLink.requestFocus()
        }

        imageGoButton.setOnClickListener {
            setUrl(editLink.text.toString())
        }

        editLink.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                editLink.performClick()

                setUrl(editLink.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setUrl(url: String) {
        textLink.text = if (url.startsWith("https://") || url.startsWith("http://")) url else "https://$url"
        webView.loadUrl(url)
        hideEditLink()
        setBackwardPageButtonEnable(true)
    }

    fun setBackwardPageButtonEnable(enabled: Boolean) {
        imageBackwardButton.isEnabled = enabled
    }

    fun setForwardPageButtonEnable(enabled: Boolean) {
        imageForwardButton.isEnabled = enabled
    }

    fun visibleEditLink() {
        editLink.visible()
        imageCloseLinkButton.visible()
        imageGoButton.visible()
    }

    fun hideEditLink() {
        editLink.gone()
        imageCloseLinkButton.gone()
        imageGoButton.gone()
    }
}

fun Int.toDp(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}