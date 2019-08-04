package com.saeed.overlayservice

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout

/**
 * A draggable viewResizeable which uses [SimpleExoPlayerView] to render video playback over main
 * application UI.
 *
 * @author Saeed
 */

class OverlayPipCustomView : FrameLayout {
    private lateinit var constraintsRoot: ConstraintLayout
    private lateinit var imageFullscreenButton: ImageView
    private lateinit var imageCloseButton: ImageView
    private lateinit var customLayoutContent: FrameLayout
    private lateinit var customView: View
    private lateinit var touchView: View

    private var playerViewSize: Int = 0
    private var sizeChangeable: Boolean = true
    private var playerType: Int = 0

    private var haveFullscreen = true
    /**
     * Tracks if viewResizeable is fullscreen.
     */
    private var fullscreenOn: Boolean = false
    val isDraggable: Boolean
        get() {
            return !fullscreenOn
        }

    private var onFullscreen: () -> Unit = {}
    private var onClosed: () -> Unit = {}

    private var canHideActionButtons = true
    private val hideActionHandler = Handler()
    private val HIDE_ACTION_DURATION = 2000L
    private val hideActionRunnable = Runnable {
        if (!isMoving) {
            hideActions()
        }
    }

    var isMoving: Boolean = false

    constructor(ctx: Context) : super(ctx) {
        inflate(context, R.layout.layout_pip_custom_view, this)
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        setAttributes(attrs)
        inflate(context, R.layout.layout_pip_custom_view, this)
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle) {
        setAttributes(attrs)
        inflate(context, R.layout.layout_pip_custom_view, this)
        initView()
    }

    private fun setAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.OverlayCustomView,
                0, 0).apply {
            try {
                playerViewSize = getInteger(R.styleable.OverlayCustomView_ov_player_size, 0)
                playerType = getInteger(R.styleable.OverlayCustomView_ov_size_changeable, 0)
                sizeChangeable = getBoolean(R.styleable.OverlayCustomView_ov_size_changeable, true)
            } finally {
                recycle()
            }
        }

        doOnLayout {
            startHideAction()
        }
    }

    private fun initView() {
        constraintsRoot = findViewById(R.id.constraints_root)
        imageFullscreenButton = findViewById(R.id.image_screen_action)
        imageCloseButton = findViewById(R.id.image_close)
        customLayoutContent = findViewById(R.id.custom_view)
        touchView = findViewById(R.id.touch_view)

        setListeners()
    }

    fun addCustomView(view: View) {
        customLayoutContent.addView(view)
    }

    fun addCustomView(layoutId: Int) {
        customView = inflate(context, layoutId, null)
        customLayoutContent.addView(customView)
    }

    fun getCloseButton() = imageCloseButton
    fun getConstraintsRoot() = constraintsRoot
    fun getCustomLayoutContent() = customLayoutContent
    fun getCustomView() = customView
    fun getFullscreenButton() = imageFullscreenButton
    fun getTouchView() = touchView

    fun removeFullscreenButton() {
        haveFullscreen = false
        imageFullscreenButton.invisible()
    }

    private fun setListeners() {
        imageFullscreenButton.setOnClickListener {
            onFullscreen.invoke()
        }

        imageCloseButton.setOnClickListener {
            onClosed.invoke()
        }
    }

    fun setOnEventActionListener(
            onFullscreen: () -> Unit,
            onClosed: () -> Unit
    ) {
        this.onFullscreen = onFullscreen
        this.onClosed = onClosed
    }

    private fun startHideAction() {
        if (canHideActionButtons) {
            hideActionHandler.postDelayed(hideActionRunnable, HIDE_ACTION_DURATION)
        }
    }

    fun restartHideAction() {
        hideActionHandler.removeCallbacks(hideActionRunnable)
        if (canHideActionButtons) {
            hideActionHandler.postDelayed(hideActionRunnable, HIDE_ACTION_DURATION)
        }
    }

    fun hideActions() {
        if (canHideActionButtons) {
            imageCloseButton.invisible()
            if (haveFullscreen) {
                imageFullscreenButton.invisible()
            }
        }
    }

    fun showActions() {
        imageCloseButton.visible()
        if (haveFullscreen) {
            imageFullscreenButton.visible()
        }
    }
}
