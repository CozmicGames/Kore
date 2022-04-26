package com.cozmicgames.audio

interface AudioPlayer {
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