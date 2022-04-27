package com.cozmicgames.audio

import org.lwjgl.openal.AL10.*

class DesktopAudioPlayer internal constructor(private val source: Int, volume: Float) : AudioPlayer {
    override var volume = volume
        set(value) {
            if (source != -1)
                alSourcef(source, AL_GAIN, value)
            field = value
        }

    override fun stop() {
        if (source != -1)
            alSourceStop(source)
    }

    override fun stopLooping() {
        if (source != -1)
            alSourcei(source, AL_LOOPING, AL_FALSE)
    }
}