package com.gratedgames.utils.maths

class Ray(val origin: Vector3, val direction: Vector3) {
    constructor(builder: CameraRayBuilder, x: Float, y: Float) : this(builder.getOrigin(), builder.getDirection(x, y))

    constructor(camera: Camera, x: Float, y: Float) : this(CameraRayBuilder(camera), x, y)

    constructor(matrix: Matrix4x4, x: Float, y: Float) : this(CameraRayBuilder(matrix), x, y)

    fun getPosition(distance: Float, position: Vector3 = Vector3()) = getPosition(distance) { x, y, z -> position.set(x, y, z) }

    inline fun getPosition(distance: Float, block: (Float, Float, Float) -> Unit) {
        val x = origin.x + direction.x * distance
        val y = origin.y + direction.y * distance
        val z = origin.z + direction.z * distance
        block(x, y, z)
    }
}