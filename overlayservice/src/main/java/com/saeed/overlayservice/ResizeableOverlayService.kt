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

abstract class ResizeableOverlayService : Service(), View.OnTouchListener, OverlayService {

    protected lateinit var viewResizeable: OverlayResizeableCustomView
    protected lateinit var wm: WindowManager

    private val ONGOING_NOTIFY_ID = 8989

    private var oldDraggableRawEventY: Float = 0f
    private var oldDraggableRawEventX: Float = 0f

    private var oldResizeRawEventY: Float = 0f
    private var oldResizeRawEventX: Float = 0f

    private var initialWindowSize: Point = Point(0, 0)
    private var minimumWindowSize: Point = Point(0, 0)
    private var maximumWindowSize: Point = Point(2000, 4000)

    var screenSize: Point = Point(0, 0)
        private set

    lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    abstract override fun getForegroundNotification(): Notification

    abstract override fun getInitialWindowSize(): Point
    open fun getMinimumWindowSize(): Point {
        return Point(150.toDp(), 150.toDp())
    }

    open fun getMaximumWindowSize(): Point {
        return Point(screenSize.x, screenSize.y)
    }

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
        viewResizeable = OverlayResizeableCustomView(this)

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
        wm.addView(viewResizeable, params)

        viewResizeable.getTouchView().setOnTouchListener(this)

        viewResizeable.addCustomView(getCustomLayoutId())

        viewResizeable.getImageResizeButton().setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val changeDistanceY = (event.rawY - oldResizeRawEventY)
                    oldResizeRawEventY = event.rawY
                    val height = if (params.height > 0) {
                        params.height
                    } else {
                        viewResizeable.measuredHeight
                    }
                    var heightResize: Float = height + changeDistanceY

                    if (heightResize < minimumWindowSize.y) {
                        heightResize = minimumWindowSize.y.toFloat()
                    }
                    if (maximumWindowSize.y < heightResize) {
                        heightResize = maximumWindowSize.y.toFloat()
                    }
                    if (screenSize.y < heightResize) {
                        heightResize = screenSize.y.toFloat()
                    }

                    val changeDistanceX = (event.rawX - oldResizeRawEventX)
                    oldResizeRawEventX = event.rawX
                    val width = if (params.height > 0) {
                        params.width
                    } else {
                        viewResizeable.measuredWidth
                    }
                    var widthResize: Float = width + changeDistanceX

                    if (widthResize < minimumWindowSize.x) {
                        widthResize = minimumWindowSize.x.toFloat()
                    }
                    if (maximumWindowSize.x < widthResize) {
                        widthResize = maximumWindowSize.x.toFloat()
                    }
                    if (screenSize.x < widthResize) {
                        widthResize = screenSize.x.toFloat()
                    }

                    params.height = heightResize.toInt()
                    params.width = widthResize.toInt()
                    val constraintsParams = viewResizeable.getConstraintsRoot().layoutParams
                    constraintsParams.height = heightResize.toInt()
                    constraintsParams.width = widthResize.toInt()
                    viewResizeable.getConstraintsRoot().layoutParams = constraintsParams

                    wm.updateViewLayout(viewResizeable, params)
                }
                MotionEvent.ACTION_UP -> {
                    oldResizeRawEventY = event.rawY
                    oldResizeRawEventX = event.rawX
                }
                MotionEvent.ACTION_DOWN -> {
                    oldResizeRawEventY = event.rawY
                    oldResizeRawEventX = event.rawX
                }
                else -> {
                    oldResizeRawEventY = event.rawY
                    oldResizeRawEventX = event.rawX
                }
            }
            return@setOnTouchListener true
        }

        setOnEventListener(onFullscreen = {
            Toast.makeText(this, "Fullscreen", Toast.LENGTH_SHORT).show()
        }, onClosed = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(true)
            }
            stopSelf()
        })

        onServiceRun()

        minimumWindowSize = getMinimumWindowSize()
        maximumWindowSize = getMaximumWindowSize()

        return super.onStartCommand(intent, flags, startId)
    }

    fun setOnEventListener(
        onFullscreen: () -> Unit,
        onSmallScreen: () -> Unit = {
            val constraintsParams = viewResizeable.getConstraintsRoot().layoutParams
            constraintsParams.height = minimumWindowSize.y
            constraintsParams.width = minimumWindowSize.x

            params.height = minimumWindowSize.y
            params.width = minimumWindowSize.x

            oldResizeRawEventX = minimumWindowSize.x.toFloat()
            oldResizeRawEventY = minimumWindowSize.y.toFloat()

            viewResizeable.getConstraintsRoot().layoutParams = constraintsParams
            wm.updateViewLayout(viewResizeable, params)
        },
        onClosed: () -> Unit) {

        viewResizeable.setOnEventActionListener(onFullscreen = onFullscreen, onClosed = {
            onClosed.invoke()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(true)
            }
            stopSelf()
        }, onSmallScreen = onSmallScreen)
    }

    override fun getWindowView() = viewResizeable
    override fun getWindowManager() = wm
    override fun getView() = viewResizeable.getCustomView()

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!viewResizeable.isDraggable) {
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
                if (screenSize.y < yPos + viewResizeable.height) {
                    yPos = (screenSize.y - viewResizeable.height).toFloat()
                }

                val changeDistanceX = (event.rawX - oldDraggableRawEventX)
                oldDraggableRawEventX = event.rawX
                var xPos: Float = params.x + changeDistanceX

                if (xPos < 0) {
                    xPos = 0f
                }
                if (screenSize.x < xPos + viewResizeable.width) {
                    xPos = (screenSize.x - viewResizeable.width).toFloat()
                }

                params.y = yPos.toInt()
                params.x = xPos.toInt()
                wm.updateViewLayout(viewResizeable, params)
            }
            MotionEvent.ACTION_UP -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            MotionEvent.ACTION_DOWN -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
            else -> {
                oldDraggableRawEventY = event.rawY
                oldDraggableRawEventX = event.rawX
            }
        }
        viewResizeable.onTouchEvent(event)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(viewResizeable)
    }

}