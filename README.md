# Window Overlay Service
An easy way to create PIP(Picture in picture) and resizeable overlay window Android.

<!-- <img src="https://raw.githubusercontent.com/afollestad/material-camera/master/art/showcase1.png" width="800px" /> -->

---

# Gradle Dependency

Add Jitpack to your repositories.

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add this in your app module's `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.saeed-younus.overlay-service:overlayservice:1.13'
    implementation 'com.github.saeed-younus.overlay-service:helper:1.13'
}
```
# Basics

## Simple Picture in Picture Service

<img src="https://i.imgur.com/RXWT6ZX.gif" data-canonical-src="https://i.imgur.com/RXWT6ZX.gif" width="200" height="400" />

### Android Manifest

First, you have to register services Activities from your app's `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application    
    ...>
        
    <service android:name="com.saeed.overlayservice.ImplementPipOverlayService"/>    
</application>
```

### Code for Open Overlay Service

In main Activity you need permission to use overlay service.

```kotlin
class MainActivity : AppCompatActivity() {
    val IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE = 251
    
    fun checkDrawOverlayPermission(code: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE)
            } else {
                openFloatingWindow()
            }
        } else {
            openFloatingWindow()
        }
    }
    
    fun openFloatingWindow(code: Int) {
        val intent = Intent(this, ImplementPipOverlayService::class.java)
                val videoUrl = "video url"
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
    
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        openFloatingWindow()
                    }
                } else {
                    openFloatingWindow()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}  
```

---
## Custom Picture in Picture Service

<img src="https://i.imgur.com/MBWG3az.gif" data-canonical-src="https://i.imgur.com/MBWG3az.gif" width="200" height="400" />

### Create Custom Service

Create layout for your service. I named the layout `video_overlay_layout` :

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000"
    android:orientation="vertical">

    <com.google.android.exoplayer2.ui.PlayerView
        android:id="@+id/player"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:use_controller="false" />

</LinearLayout>
```

**Note** You can create your own player but in PIP you can not access your media controller so you need to hide or remove your media controller.

### Create Custom Service

Create service inherit from `PipOverlayService`

```kotlin
class VideoOverlayService : PipOverlayService() {
    var videoUrl: String? = null

    override fun getCustomLayoutId(): Int {
        return R.layout.video_overlay_layout
    }
    
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
```

### Android Manifest

Then you have to register service in `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application    
    ...>
        
    <service android:name=".VideoOverlayService"/>
</application>
```

### Code for open Overlay Service

In main Activity you need permission to use overlay service.

```kotlin
class MainActivity : AppCompatActivity() {
    val PIP_OVERLAY_REQUEST_CODE = 251
    
    fun checkDrawOverlayPermission(code: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, IMPLEMENTED_PIP_OVERLAY_REQUEST_CODE)
            } else {
                openFloatingWindow()
            }
        } else {
            openFloatingWindow()
        }
    }
    
    fun openFloatingWindow(code: Int) {
        val intent = Intent(this, ImplementPipOverlayService::class.java)
                val videoUrl = "video url"
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
    
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PIP_OVERLAY_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        openFloatingWindow()
                    }
                } else {
                    openFloatingWindow()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}  
```
---
## Custom Resizeable Overlay Service

<img src="https://i.imgur.com/MsPmifA.gif" data-canonical-src="https://i.imgur.com/MsPmifA.gif" width="200" height="400" />

### Create Custom Service

Create layout for your service. I named the layout `resizeable_web_overlay` :

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/constraints_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <WebView
        android:id="@+id/webView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:transitionName="player"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/web_tool_bar" />

    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/web_tool_bar"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:background="#cdcdcd"
        android:elevation="2dp"
        android:visibility="visible"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">

        <ProgressBar
            android:id="@+id/web_progress_bar"
            style="?android:attr/progressBarStyleHorizontal"
            android:layout_width="0dp"
            android:layout_height="3dp"
            android:max="100"
            android:progress="70"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent" />

        <EditText
            android:id="@+id/edit_link"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:background="#cdcdcd"
            android:elevation="3dp"
            android:imeOptions="actionDone"
            android:maxLines="1"
            android:padding="4dp"
            android:singleLine="true"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toStartOf="@id/image_go"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="https://www." />

        <ImageView
            android:id="@+id/image_go"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:layout_margin="4dp"
            android:background="#cdcdcd"
            android:elevation="3dp"
            android:src="@drawable/ic_go"
            android:tint="#1e1e1e"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toStartOf="@id/image_close_link"
            app:layout_constraintTop_toTopOf="parent" />

        <ImageView
            android:id="@+id/image_close_link"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:background="#cdcdcd"
            android:elevation="3dp"
            android:padding="4dp"
            android:src="@drawable/ic_close"
            android:tint="#1e1e1e"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <TextView
            android:id="@+id/text_link"
            android:layout_width="0dp"
            android:layout_height="48dp"
            android:layout_margin="4dp"
            android:gravity="center_vertical"
            android:maxLines="1"
            android:singleLine="true"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toStartOf="@id/image_backward"
            app:layout_constraintHorizontal_bias="0.0"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="https://www.google.com.pk" />

        <View
            android:id="@+id/line"
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_marginBottom="4dp"
            android:background="#1e1e1e"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="@id/text_link"
            app:layout_constraintStart_toStartOf="@id/text_link" />

        <ImageView
            android:id="@+id/image_reload"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:padding="4dp"
            android:src="@drawable/ic_reload"
            android:tint="#1e1e1e"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <ImageView
            android:id="@+id/image_forward"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:padding="4dp"
            android:src="@drawable/ic_forward"
            android:tint="#1e1e1e"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toStartOf="@id/image_reload"
            app:layout_constraintTop_toTopOf="parent" />

        <ImageView
            android:id="@+id/image_backward"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:padding="4dp"
            android:src="@drawable/ic_backward"
            android:tint="#1e1e1e"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toStartOf="@id/image_forward"
            app:layout_constraintTop_toTopOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Note** You can create your own custom layout the above is just for example.

### Create Custom PIP Service

Create service inherit from `ResizeableOverlayService`

```kotlin
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
```

### Android Manifest

Then you have to register service in `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application    
    ...>
        
    <service android:name=".WebViewResizeableOverlayService"/>
</application>
```

### Code for open Overlay Service

In main Activity you also need permission to use overlay service.

```kotlin
class MainActivity : AppCompatActivity() {
    val RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE = 254
    
    fun checkDrawOverlayPermission(code: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE)
            } else {
                openFloatingWindow()
            }
        } else {
            openFloatingWindow()
        }
    }
    
    fun openFloatingWindow(code: Int) {
        val intent = Intent(this, WebViewResizeableOverlayService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
    
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESIZEABLE_CUSTOM_WEB_OVERLAY_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        openFloatingWindow()
                    }
                } else {
                    openFloatingWindow()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}  
```
---

## Attributes

You can over write dimen and colors of icons.

**For `dimen.xml`**

| Attribute name | Description |
| :--: | :-- |
| touch_bar_height | Touch bar size for resizeable overlay only. |
| close_btn_size | close button icon size for all overlay |
| close_btn_padding | close button padding for all overlay |
| fullscreen_btn_size | fullscreen button icon size for custom pip overlay and resizeable overlay |
| fullscreen_btn_padding | fullscreen button padding for custom pip overlay and resizeable overlay |
| smallscreen_btn_size | small screen / fulscreen exit button icon size for custom pip overlay and resizeable overlay  |
| smallscreen_btn_padding | small screen / fulscreen exit button padding for custom pip overlay and resizeable overlay |
| resize_btn_size | Resize button size for resizeable overlay only. |
| resize_btn_padding | Resize button padding for resizeable overlay only. |


**For `colors.xml`**

| Attribute name | Description |
| :--: | :-- |
| touch_bar_color | Touch bar color |
| close_btn_bg_color | close button icon background color |
| close_btn_tint_color | close button icon color |
| fullscreen_btn_bg_color | fullscreen button icon background color |
| fullscreen_btn_tint_color | fullscreen button icon color |
| smallscreen_btn_bg_color | small screen / fulscreen exit button icon background color |
| smallscreen_btn_tint_color | small screen / fulscreen exit button icon color |
| resize_btn_bg_color | Resize button icon background color |
| resize_btn_tint_color | Resize button icon color |
