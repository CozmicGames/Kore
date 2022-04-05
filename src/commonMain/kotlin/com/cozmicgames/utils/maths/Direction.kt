package com.cozmicgames.utils.maths

enum class Direction(val vector: Vector3) {
    UP(Vector3(0.0f, 1.0f, 0.0f)),
    DOWN(Vector3(0.0f, -1.0f, 0.0f)),
    RIGHT(Vector3(1.0f, 0.0f, 0.0f)),
    LEFT(Vector3(-1.0f, 0.0f, 0.0f)),
    FRONT(Vector3(0.0f, 0.0f, 1.0f)),
    BACK(Vector3(0.0f, 0.0f, -1.0f));

    companion object {
        val POSITIVE_X = RIGHT
        val NEGATIVE_X = LEFT
        val POSITIVE_Y = UP
        val NEGATIVE_Y = DOWN
        val POSITIVE_Z = FRONT
        val NEGATIVE_Z = BACK
    }
}