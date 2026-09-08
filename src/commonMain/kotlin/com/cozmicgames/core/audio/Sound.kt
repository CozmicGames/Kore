package com.cozmicgames.core.audio

import com.cozmicgames.core.Kore
import com.cozmicgames.core.audio
import com.cozmicgames.core.utils.Disposable

interface Sound: Disposable {
    /**
     * @see Audio.play
     */
    fun play(volume: Float = 1.0f, loop: Boolean = false) = Kore.audio.play(this, volume, loop)
}