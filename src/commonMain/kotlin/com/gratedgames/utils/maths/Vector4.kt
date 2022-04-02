package com.gratedgames.utils.maths

class Vector4() : FloatVector<Vector4>(4) {
    companion object {
        val ZERO = Vector4(0.0f)
        val ONE = Vector4(1.0f)
    }

    constructor(x: Float, y: Float, z: Float, w: Float) : this() {
        set(x, y, z, w)
    }

    constructor(scalar: Float) : this() {
        set(scalar)
    }

    var x by xComponent()

    var y by yComponent()

    var z by zComponent()

    var w by wComponent()

    infix fun dot(vector: Vector4) = dot(x, y, z, w, vector.x, vector.y, vector.z, vector.w)

    infix fun cross(vector: Vector4) = cross(x, y, z, vector.x, vector.y, vector.z)

    infix fun distanceToSquared(vector: Vector4) = distanceSquared(x, y, z, w, vector.x, vector.y, vector.z, vector.w)

    infix fun distanceTo(vector: Vector4) = distance(x, y, z, w, vector.x, vector.y, vector.z, vector.w)

    fun set(x: Float, y: Float, z: Float, w: Float): Vector4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override fun copy() = Vector4(x, y, z, w)
}
