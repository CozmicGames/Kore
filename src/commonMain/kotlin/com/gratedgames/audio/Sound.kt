package com.gratedgames.audio

import com.gratedgames.Kore
import com.gratedgames.audio

interface Sound {
    fun play(volume: Float = 1.0f, loop: Boolean = false) = Kore.audio.play(this, volume, loop)
}