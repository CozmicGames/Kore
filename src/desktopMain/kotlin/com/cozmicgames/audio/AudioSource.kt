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

    fun setData(data: AudioData) {
        this.data = data

        data.begin(this)
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