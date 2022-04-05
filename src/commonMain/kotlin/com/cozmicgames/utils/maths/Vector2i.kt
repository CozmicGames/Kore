package com.cozmicgames.utils.maths

class Vector2i() : IntVector<Vector2i>(2) {
    companion object {
        val ZERO = Vector2i(0)
        val ONE = Vector2i(1)
    }

    constructor(x: Int, y: Int) : this() {
        set(x, y)
    }

    constructor(scalar: Int) : this() {
        set(scalar)
    }

    var x by xComponent()

    var y by yComponent()

    fun set(x: Int, y: Int): Vector2i {
        this.x = x
        this.y = y
        return this
    }

    fun perpendicular() = set(y, -x)

    override fun copy() = Vector2i(x, y)
}