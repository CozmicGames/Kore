package com.gratedgames.audio

interface AudioPlayer {
    var volume: Float
    fun stop()
    fun stopLooping()
}