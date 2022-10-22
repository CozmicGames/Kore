package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.utils.Disposable

interface Sound: Disposable {
    /**
     * @see Audio.play
     */
    fun play(volume: Float = 1.0f, loop: Boolean = false) = Kore.audio.play(this, volume, loop)
}