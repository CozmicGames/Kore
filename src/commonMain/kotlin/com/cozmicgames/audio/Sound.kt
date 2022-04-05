package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio

interface Sound {
    fun play(volume: Float = 1.0f, loop: Boolean = false) = Kore.audio.play(this, volume, loop)
}