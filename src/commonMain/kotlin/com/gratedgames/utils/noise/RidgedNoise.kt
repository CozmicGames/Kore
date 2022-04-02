package com.gratedgames.utils.noise

import kotlin.math.abs

object RidgedNoise : NoiseModel {
    override fun noise(x: Float, y: Float): Float {
        return 1.0f - abs(SimplexNoise.noise(x, y))
    }

    override fun noise(x: Float, y: Float, z: Float): Float {
        return 1.0f - abs(SimplexNoise.noise(x, y, z))
    }
}