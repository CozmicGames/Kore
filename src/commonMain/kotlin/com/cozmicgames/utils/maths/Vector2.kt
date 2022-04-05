package com.cozmicgames.utils.maths

import kotlin.math.atan2

class Vector2() : FloatVector<Vector2>(2) {
    companion object {
        val ZERO = Vector2(0.0f)
        val ONE = Vector2(1.0f)
    }

    constructor(x: Float, y: Float) : this() {
        set(x, y)
    }

    constructor(scalar: Float) : this() {
        set(scalar)
    }

    var x by xComponent()

    var y by yComponent()

    infix fun dot(vector: Vector2) = dot(x, y, vector.x, vector.y)

    infix fun cross(scalar: Float) = cross(x, y, scalar)

    infix fun cross(vector: Vector2) = cross(x, y, vector.x, vector.y)

    infix fun distanceToSquared(vector: Vector2) = distanceSquared(x, y, vector.x, vector.y)

    infix fun distanceTo(vector: Vector2) = distance(x, y, vector.x, vector.y)

    fun set(x: Float, y: Float): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    fun perpendicular() = set(y, -x)

    infix fun angle(vector: Vector2): Float {
        val dot = x * vector.x + y * vector.y
        val det = x * vector.y - y * vector.x
        return atan2(det, dot)
    }

    override fun copy() = Vector2(x, y)
}