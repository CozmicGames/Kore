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

        data.begin(this, loop)
    }

    fun stopPlaying() {
        alSourceStop(handle)
        data?.end(this)
    }

    fun stopLooping() {
        data?.endLooping(this)
    }

    fun pause() {
        alSourcePause(handle)
    }

    fun resume() {
        alSourcePlay(handle)
    }

    fun update(): Boolean {
        if (alGetSourcei(handle, AL_SOURCE_STATE) != AL_PLAYING) {
            data?.let {
                it.end(this)
                if (it.isDisposed)
                    it.freeBuffers()
            }

            return false
        }

        data?.update(this)
        return true
    }

    override fun dispose() {
        if (handle != -1)
            alDeleteSources(handle)
    }
}