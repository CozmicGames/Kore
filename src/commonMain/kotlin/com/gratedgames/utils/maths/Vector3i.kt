package com.gratedgames.utils.maths

class Vector3i() : IntVector<Vector3i>(3) {
    companion object {
        val ZERO = Vector3i(0)
        val ONE = Vector3i(1)
    }

    constructor(x: Int, y: Int, z: Int) : this() {
        set(x, y, z)
    }

    constructor(scalar: Int) : this() {
        set(scalar)
    }

    var x by xComponent()

    var y by yComponent()

    var z by zComponent()

    fun set(x: Int, y: Int, z: Int): Vector3i {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override fun copy() = Vector3i(x, y, z)
}