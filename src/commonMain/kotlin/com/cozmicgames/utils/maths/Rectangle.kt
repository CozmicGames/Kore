package com.cozmicgames.utils.maths

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

    val isInfinite get() = width < 0.0f || height < 0.0f

    fun infinite() {
        x = 0.0f
        y = 0.0f
        width = -1.0f
        height = -1.0f
    }

    operator fun contains(point: Vector2) = if (isInfinite) true else minX <= point.x && minY <= point.y && maxX >= point.x && maxY >= point.y

    operator fun contains(rectangle: Rectangle) = if (isInfinite || rectangle.isInfinite) true else minX <= rectangle.minX && minY <= rectangle.minY && maxX >= rectangle.maxX && maxY >= rectangle.maxY

    infix fun intersects(rectangle: Rectangle) = if (isInfinite || rectangle.isInfinite) true else intersectRectRect(minX, minY, maxX, maxY, rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY)

    fun merge(point: Vector2) = merge(point.x, point.y)

    fun merge(x: Float, y: Float): Rectangle {
        if (isInfinite)
            return this

        val minX = min(minX, x)
        val maxX = max(maxX, x)
        width = maxX - minX
        this.x = minX
        val minY = min(minY, y)
        val maxY = max(maxY, y)
        height = maxY - minY
        this.y = minY

        return this
    }

    fun merge(rectangle: Rectangle): Rectangle {
        if (isInfinite)
            return this

        val minX = min(minX, rectangle.minX)
        val maxX = max(maxX, rectangle.maxX)
        width = maxX - minX
        x = minX
        val minY = min(minY, rectangle.minY)
        val maxY = max(maxY, rectangle.maxY)
        height = maxY - minY
        y = minY

        return this
    }

    fun set(rectangle: Rectangle): Rectangle {
        x = rectangle.x
        y = rectangle.y
        width = rectangle.width
        height = rectangle.height

        return this
    }

    fun expand(amount: Float) = expand(amount, amount)

    fun expand(amountX: Float, amountY: Float): Rectangle {
        if (isInfinite)
            return this

        minX -= amountX
        maxX += amountX
        minY -= amountY
        maxY += amountY

        return this
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
        return if (isInfinite) "Rectangle(infinite)" else "Rectangle(x=$x, y=$y, width=$width, height=$height)"
    }
}
