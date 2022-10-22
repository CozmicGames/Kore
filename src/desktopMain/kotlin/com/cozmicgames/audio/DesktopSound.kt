package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio

class DesktopSound internal constructor(internal val data: AudioData) : Sound {
    override fun play(volume: Float, loop: Boolean) = Kore.audio.play(this, volume, loop)

    override fun dispose() {
        data.dispose()
    }
}