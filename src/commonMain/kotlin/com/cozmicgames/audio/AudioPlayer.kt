package com.cozmicgames.audio

interface AudioPlayer {
    /**
     * Returns true if the audio player is currently paused.
     */
    var isPaused: Boolean

    /**
     * Sets the volume of the audio player.
     */
    var volume: Float

    /**
     * Stops the audio player.
     */
    fun stop()

    /**
     * Stops looping if it was specified by [Audio.play].
     */
    fun stopLooping()
}