package com.saeed.overlayservice.helper

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

interface SimpleExoPlayerEventListener : Player.EventListener {
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

    override fun onPlayerError(error: ExoPlaybackException?) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onPositionDiscontinuity(p0: Int) {}

    override fun onSeekProcessed() {}

    override fun onShuffleModeEnabledChanged(p0: Boolean) {}

    override fun onTimelineChanged(p0: Timeline?, p1: Any?, p2: Int) { }
}