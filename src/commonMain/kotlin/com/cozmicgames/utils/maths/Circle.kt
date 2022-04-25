package com.cozmicgames.utils.maths

import kotlin.math.max

open class Circle(var x: Float, var y: Float, var radius: Float) {
    constructor() : this(0.0f, 0.0f, Float.MAX_VALUE)

    fun infinite() {
        x = 0.0f
        y = 0.0f
        radius = Float.MAX_VALUE
    }

    operator fun contains(point: Vector2): Boolean {
        val distX = x - point.x
        val distY = y - point.y
        return (distX * distX + distY * distY) <= radius * radius
    }

    operator fun contains(circle: Circle): Boolean {
        val distX = x - circle.x
        val distY = y - circle.y
        return (distX * distX + distY * distY) <= (radius + circle.radius) * (radius + circle.radius)
    }

    fun merge(point: Vector2) {
        val length = Vector2(x - point.x, y - point.y).length
        radius = max(radius, length)
    }

    fun merge(circle: Circle) {
        val length = Vector2(x - circle.x, y - circle.y).length
        radius = max(radius, length + circle.radius)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Circle

        if (x != other.x) return false
        if (y != other.y) return false
        if (radius != other.radius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + radius.hashCode()
        return result
    }

    override fun toString(): String {
        return "Circle(x=$x, y=$y, radius=$radius)"
    }
}