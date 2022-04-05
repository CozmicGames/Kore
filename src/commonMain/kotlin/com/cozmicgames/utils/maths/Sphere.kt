package com.cozmicgames.utils.maths

import kotlin.math.max

class Sphere(var x: Float, var y: Float, var z: Float, var radius: Float) {
    constructor() : this(0.0f, 0.0f, 0.0f, Float.MAX_VALUE)

    fun infinite() {
        x = 0.0f
        y = 0.0f
        z = 0.0f
        radius = Float.MAX_VALUE
    }

    inline operator fun contains(vector: Vector3): Boolean {
        val distX = x - vector.x
        val distY = y - vector.y
        val distZ = z - vector.z
        return (distX * distX + distY * distY + distZ * distZ) <= radius * radius
    }

    inline operator fun contains(sphere: Sphere): Boolean {
        val distX = x - sphere.x
        val distY = y - sphere.y
        val distZ = z - sphere.z
        return (distX * distX + distY * distY + distZ * distZ) <= (radius + sphere.radius) * (radius + sphere.radius)
    }

    inline fun merge(vector: Vector3) {
        val length = Vector3(x - vector.x, y - vector.y, z - vector.z).length
        radius = max(radius, length)
    }

    inline fun merge(sphere: Sphere) {
        val length = Vector3(x - sphere.x, y - sphere.y, z - sphere.z).length
        radius = max(radius, length + sphere.radius)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Sphere

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (radius != other.radius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + radius.hashCode()
        return result
    }

    override fun toString(): String {
        return "Sphere(x=$x, y=$y, z=$z, radius=$radius)"
    }
}