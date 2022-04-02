package com.gratedgames.utils.maths

import kotlin.math.max
import kotlin.math.min

open class Rectangle(var x: Float, var y: Float, var width: Float, var height: Float) {
    constructor() : this(0.0f, 0.0f, Float.MAX_VALUE, Float.MAX_VALUE)

    var minX
        get() = x
        set(value) {
            x = value
        }

    var minY
        get() = y
        set(value) {
            y = value
        }

    var maxX
        get() = x + width
        set(value) {
            width = value - minX
        }

    var maxY
        get() = y + height
        set(value) {
            height = value - minY
        }

    var centerX
        get() = minX + width * 0.5f
        set(value) {
            val half = width * 0.5f
            minX = value - half
            maxX = value + half
        }

    var centerY
        get() = minY + height * 0.5f
        set(value) {
            val half = height * 0.5f
            minY = value - half
            maxY = value + half
        }

    val area get() = width * height

    fun infinite() {
        x = Float.MAX_VALUE
        y = Float.MAX_VALUE
        width = -Float.MAX_VALUE
        height = -Float.MAX_VALUE
    }

    inline operator fun contains(point: Vector2) = minX <= point.x && minY <= point.y && maxX >= point.x && maxY >= point.y

    inline operator fun contains(rectangle: Rectangle) = minX <= rectangle.minX && minY <= rectangle.minY && maxX >= rectangle.maxX && maxY >= rectangle.maxY

    inline infix fun intersects(rectangle: Rectangle) = intersectRectRect(minX, minY, maxX, maxY, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY)

    inline fun merge(point: Vector2) = merge(point.x, point.y)

    inline fun merge(x: Float, y: Float) {
        val minX = min(minX, x)
        val maxX = max(maxX, x)
        width = maxX - minX
        this.x = minX
        val minY = min(minY, y)
        val maxY = max(maxY, y)
        height = maxY - minY
        this.y = minY
    }

    inline fun merge(rectangle: Rectangle) {
        val minX = min(minX, rectangle.minX)
        val maxX = max(maxX, rectangle.maxX)
        width = maxX - minX
        x = minX
        val minY = min(minY, rectangle.minY)
        val maxY = max(maxY, rectangle.maxY)
        height = maxY - minY
        y = minY
    }

    fun set(rectangle: Rectangle) {
        x = rectangle.x
        y = rectangle.y
        width = rectangle.width
        height = rectangle.height
    }

    fun expand(amount: Float) = expand(amount, amount)

    fun expand(amountX: Float, amountY: Float) {
        minX -= amountX
        maxX += amountX
        minY -= amountY
        maxY += amountY
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Rectangle

        if (x != other.x) return false
        if (y != other.y) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + height.hashCode()
        return result
    }

    override fun toString(): String {
        return "Rectangle(x=$x, y=$y, width=$width, height=$height)"
    }
}
