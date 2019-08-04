package com.saeed.overlayservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast

abstract class PipOverlayService : Service(), View.OnTouchListener, OverlayService {

    protected lateinit var pipView: OverlayPipCustomView
    protected lateinit var wm: WindowManager

    private val ONGOING_NOTIFY_ID = 8989

    private var oldDraggableRawEventY: Float = 0f
    private var oldDraggableRawEventX: Float = 0f

    private var initialWindowSize: Point = Point(0, 0)

    var screenSize: Point = Point(0, 0)
        private set

    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    abstract override fun getForegroundNotification(): Notification

    abstract override fun getInitialWindowSize(): Point

    abstract override fun getCustomLayoutId(): Int

    fun getNotificationId() = ONGOING_NOTIFY_ID

    abstract override fun onServiceRun()

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notification: Notification = getForegroundNotification()
            startForeground(ONGOING_NOTIFY_ID, notification)
        }

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        pipView = OverlayPipCustomView(this)

        val display = wm.defaultDisplay
        display.getSize(screenSize)

        initialWindowSize = getInitialWindowSize()

        params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                    initialWindowSize.x,
                    initialWindowSize.y,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                    initialWindowSize.x,
                    initialWindowSize.y,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.START or Gravity.TOP
        params.x = 0
        params.y = 0
        wm.addView(pipView, params)

        pipView.getTouchView().setOnTouchListener(this)
        pipView.addCustomView(getCustomLayoutId())

        setOnEventListener(onFullscreen = {
            Toast.makeText(this, "Fullscreen", Toast.LENGTH_SHORT).show()
        }, onClosed = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(true)
            }
            stopSelf()
        })

        onServiceRun()

        return super.onStartCommand(intent, flags, startId)
    }

    fun setOnEventListener(onFullscreen: () -> Unit, onClosed: () -> Unit) {
        pipView.setOnEventActionListener(onFullscreen = onFullscreen, onClosed = {
            onClosed.invoke()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(true)
            }
            stopSelf()
        })
    }

    private fun setWindowSize(point: Point) {
        params.height = point.y
        params.width = point.x

        wm.updateViewLayout(pipView, params)
    }

    override fun getWindowView() = pipView
    override fun getWindowManager() = wm
    override fun getView() = pipView.getCustomView()

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!pipView.isDraggable) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {

                val changeDistanceY = (event.rawY - oldDraggableRawEventY)
                oldDraggableRawEventY = event.rawY
                var yPos: Float = params.y + changeDistanceY

                if (yPos < 0) {
                    yPos = 0f
                }
                if (screenSize.y < yPos + pipView.height) {
                    yPos = (screenSize.y - pipView.height).toFloat()
                }

                val changeDistanceX = (event.rawX - oldDraggableRawEventX)
                oldDraggableRawEventX = event.rawX
                var xPos: Float = params.x + changeDistanceX

                if (xPos < 0) {
                    xPos = 0f
                }
                if (screenSize.x < xPos + pipView.width) {
                    xPos = (screenSize.x - pipView.width).toFloat()
                }

                params.y = yPos.toInt()
                params.x = xPos.toInt()
                wm.updateViewLayout(pipView, params)
            }
            MotionEvent.ACTION_UP -> {
                pipView.isMoving = false
                pipView.restartHideAction()
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            MotionEvent.ACTION_DOWN -> {
                pipView.isMoving = true
                pipView.showActions()
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            else -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(pipView)
    }

}