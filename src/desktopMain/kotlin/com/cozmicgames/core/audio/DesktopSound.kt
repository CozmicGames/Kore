package com.cozmicgames.core.audio

import com.cozmicgames.core.Kore
import com.cozmicgames.core.audio
import com.cozmicgames.core.audio.Sound

class DesktopSound internal constructor(internal val data: AudioData) : Sound {
    override fun play(volume: Float, loop: Boolean) = Kore.audio.play(this, volume, loop)

    override fun dispose() {
        data.dispose()
    }
}