package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.utils.Disposable

class DesktopSound internal constructor(internal val buffer: Int) : Sound, Disposable {
    override fun play(volume: Float, loop: Boolean) = Kore.audio.play(this, volume, loop)

    override fun dispose() {
        (Kore.audio as DesktopAudio).freeBufferHandle(buffer)
    }
}