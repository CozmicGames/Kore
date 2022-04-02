package com.gratedgames.utils.noise

interface NoiseModel {
    fun noise(x: Float, y: Float): Float

    fun noise(x: Float, y: Float, z: Float): Float
}