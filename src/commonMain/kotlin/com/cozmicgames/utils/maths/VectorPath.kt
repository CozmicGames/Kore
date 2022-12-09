package com.cozmicgames.utils.maths

import com.cozmicgames.utils.collections.Pool
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class VectorPath : Iterable<Vector2> {
    companion object {
        private val pointsPool = Pool(supplier = { Vector2() }, reset = { it.setZero() })
    }

    private val points = arrayListOf<Vector2>()

    private val lastPoint get() = points.getOrElse(points.size - 1) { Vector2.ZERO }

    val count get() = points.size

    var minX = -Float.MAX_VALUE
        private set

    var minY = -Float.MAX_VALUE
        private set

    var maxX = Float.MAX_VALUE
        private set

    var maxY = Float.MAX_VALUE
        private set

    val isConvex: Boolean
        get() {
            if (count <= 3)
                return true

            if (!isCCW(this[0], this[1], this[2]))
                return false

            for (i in (2 until count - 1)) {
                if (!isCCW(this[i - 1], this[i], this[i + 1 % count]))
                    return false
            }

            return true
        }

    override fun iterator(): Iterator<Vector2> = points.iterator()

    operator fun get(index: Int) = points[index]

    fun clear() {
        points.forEach {
            pointsPool.free(it)
        }
        points.clear()
        minX = Float.POSITIVE_INFINITY
        minY = Float.POSITIVE_INFINITY
        maxX = Float.NEGATIVE_INFINITY
        maxY = Float.NEGATIVE_INFINITY
    }

    fun addPoint(block: Vector2.() -> Unit) {
        val point = pointsPool.obtain()
        block(point)

        if (point.x < minX)
            minX = point.x

        if (point.x > maxX)
            maxX = point.x

        if (point.y < minY)
            minY = point.y

        if (point.y > maxY)
            maxY = point.y

        points.add(point)
    }

    fun add(x: Float, y: Float) {
        addPoint {
            this.x = x
            this.y = y
        }
    }

    fun arc(x: Float, y: Float, radius: Float, angleMin: Float, angleMax: Float, segmentCount: Int = 16) {
        if (radius == 0.0f) {
            addPoint {
                this.x = x
                this.y = y
            }
            return
        }

        repeat(segmentCount) {
            val angle = angleMin + (it.toFloat() / segmentCount) * (angleMax - angleMin)
            addPoint {
                this.x = x + cos(angle) * radius
                this.y = y + sin(angle) * radius
            }
        }
    }

    fun bezier(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        fun bezierCasteljau(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, level: Int) {
            val dx = x3 - x0
            val dy = y3 - y0
            var d2 = ((x1 - x3) * dy - (y1 - y3) * dx)
            var d3 = ((x2 - x3) * dy - (y2 - y3) * dx)
            d2 = if (d2 >= 0) d2 else -d2
            d3 = if (d3 >= 0) d3 else -d3
            if ((d2 + d3) * (d2 + d3) < 0.0f * (dx * dx + dy * dy))
                addPoint {
                    this.x = x3
                    this.y = y3
                }
            else if (level < 10) {
                val x12 = (x0 + x1) * 0.5f
                val y12 = (y0 + y1) * 0.5f
                val x23 = (x1 + x2) * 0.5f
                val y23 = (y1 + y2) * 0.5f
                val x34 = (x2 + x3) * 0.5f
                val y34 = (y2 + y3) * 0.5f
                val x123 = (x12 + x23) * 0.5f
                val y123 = (y12 + y23) * 0.5f
                val x234 = (x23 + x34) * 0.5f
                val y234 = (y23 + y34) * 0.5f
                val x1234 = (x123 + x234) * 0.5f
                val y1234 = (y123 + y234) * 0.5f
                bezierCasteljau(x0, y0, x12, y12, x123, y123, x1234, y1234, level + 1)
                bezierCasteljau(x1234, y1234, x234, y234, x34, y34, x3, y3, level + 1)
            }
        }

        val (x0, y0) = lastPoint

        bezierCasteljau(x0, y0, x1, y1, x2, y2, x3, y3, 0)
    }

    fun line(x0: Float, y0: Float, x1: Float, y1: Float) {
        add(x0, y0)
        add(x1, y1)
    }

    fun circle(x: Float, y: Float, radius: Float, segmentCount: Int = 32) {
        val maxAngle = PI.toFloat() * 2.0f * ((segmentCount - 1.0f) / segmentCount.toFloat())
        arc(x, y, radius, 0.0f, maxAngle, segmentCount)
    }

    fun rect(rectangle: Rectangle) = rect(rectangle.minX, rectangle.minY, rectangle.width, rectangle.height)

    fun rect(x: Float, y: Float, width: Float, height: Float) {
        add(x, y)
        add(x + width, y)
        add(x + width, y + height)
        add(x, y + height)
    }

    fun roundedRect(rectangle: Rectangle, roundingRadius: Float, vararg corners: Corner) = roundedRect(rectangle.minX, rectangle.minY, rectangle.width, rectangle.height, roundingRadius, *corners)

    fun roundedRect(x: Float, y: Float, width: Float, height: Float, roundingRadius: Float, vararg corners: Corner) = roundedRect(x, y, width, height, roundingRadius, Corners.combine(*corners))

    fun roundedRect(rectangle: Rectangle, roundingRadius: Float, roundingFlags: Int = Corners.ALL) = roundedRect(rectangle.minX, rectangle.minY, rectangle.width, rectangle.height, roundingRadius, roundingFlags)

    fun roundedRect(x: Float, y: Float, width: Float, height: Float, roundingRadius: Float, roundingFlags: Int = Corners.ALL) {
        val roundingUpperLeft = if (Corners.UPPER_LEFT in roundingFlags) roundingRadius else 0.0f
        val roundingUpperRight = if (Corners.UPPER_RIGHT in roundingFlags) roundingRadius else 0.0f
        val roundingLowerLeft = if (Corners.LOWER_LEFT in roundingFlags) roundingRadius else 0.0f
        val roundingLowerRight = if (Corners.LOWER_RIGHT in roundingFlags) roundingRadius else 0.0f

        arc(x + roundingUpperLeft, y + roundingUpperLeft, roundingUpperLeft, toRadians(180.0f), toRadians(270.0f))
        arc(x + width - roundingUpperRight, y + roundingUpperRight, roundingUpperRight, toRadians(270.0f), toRadians(360.0f))
        arc(x + width - roundingLowerRight, y + height - roundingLowerRight, roundingLowerRight, toRadians(0.0f), toRadians(90.0f))
        arc(x + roundingLowerLeft, y + height - roundingLowerLeft, roundingLowerLeft, toRadians(90.0f), toRadians(180.0f))
    }

    fun squircle(x: Float, y: Float, width: Float, height: Float, roundingRadius: Float) {
        val minSide = min(width, height)
        val radius = min(roundingRadius, minSide * 0.5f)

        val corner0X = x
        val corner0Y = y

        val corner1X = x + width
        val corner1Y = y

        val corner2X = x
        val corner2Y = y + height

        val corner3X = x + width
        val corner3Y = y + height

        val p0x = x + radius
        val p0y = y

        val p1x = x + width - radius
        val p1y = y

        val p2x = x + width
        val p2y = y + radius

        val p3x = x + width
        val p3y = y + height - radius

        val p4x = x + width - radius
        val p4y = y + height

        val p5x = x + radius
        val p5y = y + height

        val p6x = x
        val p6y = y + height - radius

        val p7x = x
        val p7y = y + radius

        add(p0x, p0y)
        add(p1x, p1y)
        bezier(p2x, p2y, corner1X, corner1Y, corner1X, corner1Y)
        add(p3x, p3y)
        bezier(p4x, p4y, corner3X, corner3Y, corner3X, corner3Y)
        add(p5x, p5y)
        bezier(p6x, p6y, corner2X, corner2Y, corner2X, corner2Y)
        add(p7x, p7y)
        bezier(p0x, p0y, corner0X, corner0Y, corner0X, corner0Y)
    }

    operator fun contains(point: Vector2) = contains(point.x, point.y)

    fun contains(x: Float, y: Float, onlyBounds: Boolean = false): Boolean {
        if (x < minX || y < minY || x > maxX || y > maxY)
            return false

        if (onlyBounds)
            return true

        var intersects = 0

        repeat(count) {
            val (x0, y0) = points[it]
            val (x1, y1) = points[if (it + 1 == count) 0 else it + 1]

            if (((y0 <= y && y < y1) || (y1 <= y && y < y0)) && x < ((x1 - x0) / (y1 - y0) * (y - y0) + x0))
                intersects++
        }

        return (intersects and 1) == 1
    }
}