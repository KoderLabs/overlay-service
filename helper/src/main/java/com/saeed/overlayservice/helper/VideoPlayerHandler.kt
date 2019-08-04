package com.saeed.overlayservice.helper

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.saeed.overlayservice.helper.SimpleActivityLifecycleCallback
import java.io.File

typealias PlaybackListenerType = (isPlaying: Boolean, isLoading: Boolean) -> Unit

class VideoPlayerHandler private constructor(private val context: Context) : SimpleActivityLifecycleCallback {

    private lateinit var player: ExoPlayer

    private val playbackListeners = mutableListOf<PlaybackListenerType>()

    private val dataSourceFactory by lazy {
        CacheDataSourceFactory(
                context,
                100 * 1024 * 1024, 10 * 5 * 1024 * 1024
        )
    }

    private val extractorsFactory by lazy {
        DefaultExtractorsFactory()
    }

    private lateinit var eventListener: Player.EventListener

    private var localEventListener: Player.EventListener = object : SimpleExoPlayerEventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_ENDED -> {
                    player.playWhenReady = false
                    player.seekTo(0L)
                }
                Player.STATE_BUFFERING -> {
                    playbackListeners.forEach {
                        it.invoke(true, true)
                    }
                }
                Player.STATE_READY -> {
                    if (playWhenReady) {
                        playbackListeners.forEach {
                            it.invoke(true, false)
                        }
                    } else {
                        playbackListeners.forEach {
                            it.invoke(false, false)
                        }
                    }
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            player.stop()
        }
    }

    private fun initVideoPlayer() {
        try {
            if (!::player.isInitialized) {
                val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
                val trackSelector: TrackSelector =
                        DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
                player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
            }
            player.addListener(localEventListener)
            if(::eventListener.isInitialized) {
                player.addListener(eventListener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addPlaybackListener(listener: PlaybackListenerType) {
        playbackListeners.add(listener)
    }

    internal fun setEventListener(eventListener: Player.EventListener) {
        this.eventListener = eventListener
    }

    fun removePlayBackListener(listener: PlaybackListenerType) {
        playbackListeners.remove(listener)
    }

    fun startVideo(exoPlayerView: PlayerView, url: String) {
        initVideoPlayer()
        exoPlayerView.player = player
        changeTrack(url)
    }

    fun embedVideoPlayer(exoPlayerView: PlayerView) {
        exoPlayerView.player = player
        player.playWhenReady = true
    }

    private fun changeTrack(url: String) {
        val mediaSource = ExtractorMediaSource(
                Uri.parse(url),
                dataSourceFactory,
                extractorsFactory,
                null,
                null
        )

        player.prepare(mediaSource)
        player.playWhenReady = true
    }

    fun pauseVideo() {
        player.playWhenReady = false
    }

    fun resumeVideo() {
        player.playWhenReady = false
    }

    @Suppress("unused")
    fun stopVideo() {
        player.stop()
    }

    fun releaseVideo() {
        player.release()
    }

    override fun onActivityPaused(activity: Activity?) {
        pauseVideo()
    }

    override fun onActivityStarted(activity: Activity?) {
        resumeVideo()
    }

    override fun onActivityDestroyed(activity: Activity?) {
        if (activity?.isTaskRoot == true) {
            releaseVideo()
        }
    }

    class Builder(context: Context) {

        private val INSTANCE: VideoPlayerHandler = VideoPlayerHandler(context)

        fun setExoPlayer(player: ExoPlayer): Builder {
            INSTANCE.player = player
            return this
        }

        fun setEventListener(listener: Player.EventListener): Builder {
            INSTANCE.setEventListener(listener)
            return this
        }

        fun create(): VideoPlayerHandler {
            INSTANCE.initVideoPlayer()
            return INSTANCE
        }
    }
}

class CacheDataSourceFactory(
        private val context: Context,
        private val maxCacheSize: Long,
        private val maxFileSize: Long
) : DataSource.Factory {

    private val defaultDataSourceFactory: DefaultDataSourceFactory

    init {
        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val bandwidthMeter = DefaultBandwidthMeter()
        defaultDataSourceFactory = DefaultDataSourceFactory(
                this.context,
                bandwidthMeter,
                DefaultHttpDataSourceFactory(userAgent, bandwidthMeter)
        )
    }

    @Suppress("SpellCheckingInspection")
    override fun createDataSource(): DataSource {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        val simpleCache = SimpleCache(File(context.cacheDir, "media"), evictor)
        return CacheDataSource(
                simpleCache,
                defaultDataSourceFactory.createDataSource(),
                FileDataSource(),
                CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                null
        )
    }
}