package com.cozmicgames.audio

import com.cozmicgames.utils.maths.Vector3

open class AudioListener {
    val position = Vector3(0.0f, 0.0f, 0.0f)
    val velocity = Vector3(0.0f, 0.0f, 0.0f)
    val direction = Vector3(0.0f, 0.0f, 1.0f)
    val up = Vector3(0.0f, 1.0f, 0.0f)
}
