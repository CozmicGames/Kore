package com.cozmicgames.audio

class DesktopAudioPlayer internal constructor(private val source: AudioSource?, volume: Float) : AudioPlayer {
    override var isPaused = false
        set(value) {
            if (field == value)
                return

            if (value)
                source?.pause()
            else
                source?.resume()

            field = value
        }

    override var volume = volume
        set(value) {
            source?.setVolume(volume)
            field = value
        }

    override fun stop() {
        source?.stopPlaying()
    }

    override fun stopLooping() {
        source?.stopLooping()
    }
}