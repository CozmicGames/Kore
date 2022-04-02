package com.gratedgames.utils.maths

class Vector3() : FloatVector<Vector3>(3) {
    companion object {
        val ZERO = Vector3(0.0f)
        val ONE = Vector3(1.0f)
    }

    constructor(x: Float, y: Float, z: Float) : this() {
        set(x, y, z)
    }

    constructor(scalar: Float) : this() {
        set(scalar)
    }

    var x by xComponent()

    var y by yComponent()

    var z by zComponent()

    infix fun dot(vector: Vector3) = dot(x, y, z, vector.x, vector.y, vector.z)

    infix fun cross(vector: Vector3) = cross(x, y, z, vector.x, vector.y, vector.z)

    infix fun distanceToSquared(vector: Vector3) = distanceSquared(x, y, z, vector.x, vector.y, vector.z)

    infix fun distanceTo(vector: Vector3) = distance(x, y, z, vector.x, vector.y, vector.z)

    fun set(x: Float, y: Float, z: Float): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun mul(matrix: Matrix4x4): Vector3 {
        val nx = x * matrix.m00 + y * matrix.m01 + z * matrix.m02 + matrix.m03
        val ny = x * matrix.m10 + y * matrix.m11 + z * matrix.m12 + matrix.m13
        val nz = x * matrix.m20 + y * matrix.m21 + z * matrix.m22 + matrix.m23
        return set(nx, ny, nz)
    }


    fun rotate(axis: Vector3, angle: Float, tempMatrix: Matrix4x4 = Matrix4x4()) = mul(tempMatrix.setToRotation(axis.x, axis.y, axis.z, angle))

    fun rotate(axisX: Float, axisY: Float, axisZ: Float, angle: Float, tempMatrix: Matrix4x4 = Matrix4x4()) = mul(tempMatrix.setToRotation(axisX, axisY, axisZ, angle))

    override fun copy() = Vector3(x, y, z)
}