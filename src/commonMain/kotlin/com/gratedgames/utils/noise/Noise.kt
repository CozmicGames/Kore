package com.gratedgames.utils.noise

fun noise(x: Float, y: Float, model: NoiseModel = SimplexNoise, settings: NoiseSettings = NoiseSettings()): Float {
    var frequency = settings.frequency
    var total = 0.0f
    var maxAmplitude = 0.0f
    var amplitude = settings.amplitude

    repeat(settings.octaves) {
        total += model.noise(x * frequency, y * frequency) * amplitude
        frequency *= 2.0f
        maxAmplitude += amplitude
        amplitude *= settings.persistence
    }

    return total / maxAmplitude
}

fun noise(x: Float, y: Float, z: Float, model: NoiseModel = SimplexNoise, settings: NoiseSettings = NoiseSettings()): Float {
    var frequency = settings.frequency
    var total = 0.0f
    var maxAmplitude = 0.0f
    var amplitude = settings.amplitude

    repeat(settings.octaves) {
        total += model.noise(x * frequency, y * frequency, z * frequency) * amplitude
        frequency *= 2.0f
        maxAmplitude += amplitude
        amplitude *= settings.persistence
    }

    return total / maxAmplitude
}
