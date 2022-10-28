package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.utils.Disposable
import org.lwjgl.openal.AL10.*
import org.lwjgl.system.MemoryStack

class AudioDataSource : Disposable {
    val handle: Int

    private var data: AudioData? = null
    private var source: AudioObject? = null

    init {
        val handle = alGenSources()
        this.handle = if (alGetError() == AL_NO_ERROR)
            handle
        else
            -1
    }

    private fun updateSource(delta: Float) {
        val source = this.source.also {
            it?.update(delta)
        } ?: Kore.audio.listener

        MemoryStack.stackPush().use {
            alSourcefv(handle, AL_POSITION, it.floats(source.position.x, source.position.y, source.position.z))
            alSourcefv(handle, AL_ORIENTATION, it.floats(source.direction.x, source.direction.y, source.direction.z, source.up.x, source.up.y, source.up.z))
            alSourcefv(handle, AL_VELOCITY, it.floats(source.velocity.x, source.velocity.y, source.velocity.z))
        }
    }

    fun setVolume(volume: Float) {
        alSourcef(handle, AL_GAIN, volume)
    }

    fun beginPlaying(data: AudioData, volume: Float, loop: Boolean, source: AudioObject?) {
        this.data = data
        this.source = source

        setVolume(volume)
        updateSource(0.0f)

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

    fun update(delta: Float): Boolean {
        updateSource(delta)

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