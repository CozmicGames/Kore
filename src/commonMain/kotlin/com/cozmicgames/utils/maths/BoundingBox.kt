package com.cozmicgames.utils.maths

import kotlin.math.max
import kotlin.math.min

open class BoundingBox(var x: Float, var y: Float, var z: Float, var width: Float, var height: Float, var depth: Float) {
    constructor() : this(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

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

    var minZ
        get() = z
        set(value) {
            z = value
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

    var maxZ
        get() = z + depth
        set(value) {
            depth = value - minZ
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

    var centerZ
        get() = minZ + depth * 0.5f
        set(value) {
            val half = depth * 0.5f
            minZ = value - half
            maxZ = value + half
        }

    val volume get() = width * height * depth

    fun infinite() {
        x = -Float.MAX_VALUE * 0.5f
        y = -Float.MAX_VALUE * 0.5f
        z = -Float.MAX_VALUE * 0.5f
        width = Float.MAX_VALUE
        height = Float.MAX_VALUE
        depth = Float.MAX_VALUE
    }

    inline operator fun contains(point: Vector3) = minX <= point.x && minY <= point.y && minZ <= point.z && maxX >= point.x && maxY >= point.y && maxZ >= point.z

    inline operator fun contains(box: BoundingBox) = minX <= box.minX && minY <= box.minY && minZ <= box.minZ && maxX >= box.maxX && maxY >= box.maxY && maxZ >= box.maxZ

    inline infix fun intersects(box: BoundingBox) = intersectAabbAabb(minX, minY, minZ, maxX, maxY, maxZ, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)

    inline fun merge(point: Vector3) {
        val minX = min(minX, point.x)
        val maxX = max(maxX, point.x)
        width = maxX - minX
        x = minX

        val minY = min(minY, point.y)
        val maxY = max(maxY, point.y)
        height = maxY - minY
        y = minY

        val minZ = min(minZ, point.z)
        val maxZ = max(maxZ, point.z)
        depth = maxZ - minZ
        z = minZ
    }

    inline fun merge(box: BoundingBox): BoundingBox {
        if (box in this)
            return this

        val minX = min(minX, box.minX)
        val maxX = max(maxX, box.maxX)
        width = maxX - minX
        x = minX

        val minY = min(minY, box.minY)
        val maxY = max(maxY, box.maxY)
        height = maxY - minY
        y = minY

        val minZ = min(minZ, box.minZ)
        val maxZ = max(maxZ, box.maxZ)
        depth = maxZ - minZ
        z = minZ

        return this
    }

    fun set(box: BoundingBox): BoundingBox {
        x = box.x
        y = box.y
        z = box.z
        width = box.width
        height = box.height
        depth = box.depth
        return this
    }

    fun intersectsRay(origin: Vector3, direction: Vector3, result: Vector2? = null) = intersectsRay(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, result)

    fun intersectsRay(originX: Float, originY: Float, originZ: Float, directionX: Float, directionY: Float, directionZ: Float, result: Vector2? = null): Boolean {
        val invDirX = 1.0f / directionX
        val invDirY = 1.0f / directionY
        val invDirZ = 1.0f / directionZ
        var tNear: Float
        var tFar: Float
        val tymin: Float
        val tymax: Float
        val tzmin: Float
        val tzmax: Float

        if (invDirX >= 0.0f) {
            tNear = (minX - originX) * invDirX
            tFar = (maxX - originX) * invDirX
        } else {
            tNear = (maxX - originX) * invDirX
            tFar = (minX - originX) * invDirX
        }

        if (invDirY >= 0.0f) {
            tymin = (minY - originY) * invDirY
            tymax = (maxY - originY) * invDirY
        } else {
            tymin = (maxY - originY) * invDirY
            tymax = (minY - originY) * invDirY
        }

        if (tNear > tymax || tymin > tFar)
            return false

        if (invDirZ >= 0.0f) {
            tzmin = (minZ - originZ) * invDirZ
            tzmax = (maxZ - originZ) * invDirZ
        } else {
            tzmin = (maxZ - originZ) * invDirZ
            tzmax = (minZ - originZ) * invDirZ
        }

        if (tNear > tzmax || tzmin > tFar)
            return false

        tNear = if (tymin > tNear || tNear.isNaN()) tymin else tNear
        tFar = if (tymax < tFar || tFar.isNaN()) tymax else tFar
        tNear = if (tzmin > tNear) tzmin else tNear
        tFar = if (tzmax < tFar) tzmax else tFar

        return if (tNear < tFar && tFar >= 0.0f) {
            result?.x = tNear
            result?.y = tFar
            true
        } else
            false
    }

    fun expand(amount: Float) = expand(amount, amount, amount)

    fun expand(amountX: Float, amountY: Float, amountZ: Float) {
        minX -= amountX
        maxX += amountX
        minY -= amountY
        maxY += amountY
        minZ -= amountZ
        maxZ += amountZ
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BoundingBox

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (depth != other.depth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + depth.hashCode()
        return result
    }

    override fun toString(): String {
        return "BoundingBox(x=$x, y=$y, z=$z, width=$width, height=$height, depth=$depth)"
    }
}