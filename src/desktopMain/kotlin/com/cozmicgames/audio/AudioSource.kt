package com.cozmicgames.audio

import com.cozmicgames.utils.Disposable
import org.lwjgl.openal.AL10.*

class AudioSource : Disposable {
    val handle: Int

    private var data: AudioData? = null

    init {
        val handle = alGenSources()
        this.handle = if (alGetError() == AL_NO_ERROR)
            handle
        else
            -1
    }

    fun setVolume(volume: Float) {
        alSourcef(handle, AL_GAIN, volume)
    }

    fun beginPlaying(data: AudioData, volume: Float, loop: Boolean) {
        this.data = data

        setVolume(volume)

        alSourcei(handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)

        data.begin(this)

        alSourcePlay(handle)
    }

    fun stopPlaying() {
        alSourceStop(handle)
    }

    fun stopLooping() {
        alSourcei(handle, AL_LOOPING, AL_FALSE)
    }

    fun pause() {
        alSourcePause(handle)
    }

    fun resume() {
        alSourcePlay(handle)
    }

    fun update(): Boolean {
        data?.update(this)

        if (alGetSourcei(handle, AL_SOURCE_STATE) != AL_PLAYING) {
            data?.end(this)
            return false
        }

        return true
    }

    override fun dispose() {
        if (handle != -1)
            alDeleteSources(handle)
    }
}