package com.saeed.overlayservice

import android.app.Notification
import android.graphics.Point
import android.view.View
import android.view.WindowManager

interface OverlayService {

    fun getForegroundNotification(): Notification
    fun getInitialWindowSize(): Point

    fun getCustomLayoutId(): Int
    fun onServiceRun()

    fun getWindowView() : View
    fun getWindowManager() : WindowManager
    fun getView() : View
}