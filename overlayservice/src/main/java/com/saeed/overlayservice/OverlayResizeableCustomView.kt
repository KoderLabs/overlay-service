package com.saeed.overlayservice

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * A draggable viewResizeable which uses [SimpleExoPlayerView] to render video playback over main
 * application UI.
 *
 * @author Saeed
 */

class OverlayResizeableCustomView : FrameLayout {
    private lateinit var constraintsRoot: ConstraintLayout
    private lateinit var imageFullScreenButton: ImageView
    private lateinit var imageSmallScreenButton: ImageView
    private lateinit var imageCloseButton: ImageView
    private lateinit var imageResizeButton: ImageView
    private lateinit var customLayout: FrameLayout
    private lateinit var customView: View
    private lateinit var touchView: View

    private var playerViewSize: Int = 0
    private var sizeChangeable: Boolean = true
    private var playerType: Int = 0

    /**
     * Tracks if viewResizeable is fullscreen.
     */
    private var fullscreenOn: Boolean = false
    val isDraggable: Boolean
        get() {
            return !fullscreenOn
        }

    private var onFullscreen: () -> Unit = {}
    private var onSmallScreen: () -> Unit = {}
    private var onClosed: () -> Unit = {}

    private var canHideActionButtons = true

    constructor(ctx: Context) : super(ctx) {
        inflate(context, R.layout.layout_resizeable_custom_view, this)
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        setAttributes(attrs)
        inflate(context, R.layout.layout_resizeable_custom_view, this)
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle) {
        setAttributes(attrs)
        inflate(context, R.layout.layout_resizeable_custom_view, this)
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
    }

    private fun initView() {
        constraintsRoot = findViewById(R.id.constraints_root)
        imageFullScreenButton = findViewById(R.id.image_full_screen)
        imageSmallScreenButton = findViewById(R.id.image_small_screen)
        imageResizeButton = findViewById(R.id.image_resize)
        imageCloseButton = findViewById(R.id.image_close)
        customLayout = findViewById(R.id.custom_view)
        touchView = findViewById(R.id.touch_view)

        setListeners()
    }

    fun addCustomView(view: View) {
        customLayout.addView(view)
    }

    fun addCustomView(layoutId: Int) {
        customView = inflate(context, layoutId, null)
        customLayout.addView(customView)
    }

    fun getCloseButton() = imageCloseButton
    fun getConstraintsRoot() = constraintsRoot
    fun getCustomLayout() = customLayout
    fun getCustomView() = customView
    fun getFullscreenButton() = imageFullScreenButton
    fun getSmallScreenButton() = imageSmallScreenButton
    fun getImageResizeButton() = imageResizeButton
    fun getTouchView() = touchView

    private fun setListeners() {
        imageFullScreenButton.setOnClickListener {
            onFullscreen.invoke()
        }

        imageSmallScreenButton.setOnClickListener {
            onSmallScreen.invoke()
        }

        imageCloseButton.setOnClickListener {
            onClosed.invoke()
        }
    }

    fun setOnEventActionListener(
            onFullscreen: () -> Unit,
            onSmallScreen: () -> Unit,
            onClosed: () -> Unit
    ) {
        this.onFullscreen = onFullscreen
        this.onSmallScreen= onSmallScreen
        this.onClosed = onClosed
    }
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun Int.toDp(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}