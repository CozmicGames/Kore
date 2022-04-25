package com.cozmicgames.utils.maths

class Vector4i() : IntVector<Vector4i>(4) {
    companion object {
        val ZERO = Vector4i(0)
        val ONE = Vector4i(1)
    }

    constructor(x: Int, y: Int, z: Int, w: Int) : this() {
        set(x, y, z, w)
    }

    constructor(scalar: Int) : this() {
        set(scalar)
    }

    var x by xComponent()

    var y by yComponent()

    var z by zComponent()

    var w by wComponent()

    fun set(x: Int, y: Int, z: Int, w: Int): Vector4i {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override fun copy() = Vector4i(x, y, z, w)
}