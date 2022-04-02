package com.gratedgames.audio

import com.gratedgames.Kore
import com.gratedgames.audio
import com.gratedgames.utils.Disposable

class DesktopSound internal constructor(internal val buffer: Int) : Sound, Disposable {
    override fun play(volume: Float, loop: Boolean) = Kore.audio.play(this, volume, loop)

    override fun dispose() {
        (Kore.audio as DesktopAudio).freeBufferHandle(buffer)
    }
}