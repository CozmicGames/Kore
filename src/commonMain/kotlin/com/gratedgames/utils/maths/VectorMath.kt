package com.gratedgames.utils.maths

import kotlin.math.sqrt

inline fun dot(x0: Float, y0: Float, x1: Float, y1: Float) = x0 * x1 + y0 * y1

inline fun dot(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) = x0 * x1 + y0 * y1 + z0 * z1

inline fun dot(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float) = x0 * x1 + y0 * y1 + z0 * z1 + w0 * w1

inline fun lengthSquared(x: Float, y: Float) = dot(x, y, x, y)

inline fun lengthSquared(x: Float, y: Float, z: Float) = dot(x, y, z, x, y, z)

inline fun lengthSquared(x: Float, y: Float, z: Float, w: Float) = dot(x, y, z, w, x, y, z, w)

inline fun length(x: Float, y: Float) = sqrt(lengthSquared(x, y))

inline fun length(x: Float, y: Float, z: Float) = sqrt(lengthSquared(x, y, z))

inline fun length(x: Float, y: Float, z: Float, w: Float) = sqrt(lengthSquared(x, y, z, w))

inline fun distanceSquared(x0: Float, y0: Float, x1: Float, y1: Float) = lengthSquared(x1 - x0, y1 - y0)

inline fun distanceSquared(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) = lengthSquared(x1 - x0, y1 - y0, z1 - z0)

inline fun distanceSquared(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float) = lengthSquared(x1 - x0, y1 - y0, z1 - z0, w1 - w0)

inline fun distance(x0: Float, y0: Float, x1: Float, y1: Float) = sqrt(distanceSquared(x0, y0, x1, y1))

inline fun distance(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) = sqrt(distanceSquared(x0, y0, z0, x1, y1, z1))

inline fun distance(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float) = sqrt(distanceSquared(x0, y0, z0, w0, x1, y1, z1, w1))

inline fun <R> perpendicular(x: Float, y: Float, block: (Float, Float) -> R): R {
    val negx = -x
    return block(y, negx)
}

inline fun perpendicular(x: Float, y: Float) = perpendicular(x, y) { nx, ny -> Vector2(nx, ny) }

inline fun <R> cross(x: Float, y: Float, s: Float, block: (Float, Float) -> R): R {
    val nx = -s * y
    val ny = s * x
    return block(nx, ny)
}

inline fun cross(v: Vector2, s: Float, dest: Vector2 = Vector2()) = cross(v.x, v.y, s, dest)

inline fun cross(x: Float, y: Float, s: Float, dest: Vector2 = Vector2()) = cross(x, y, s) { nx, ny -> dest.set(nx, ny) }

inline fun cross(x0: Float, y0: Float, x1: Float, y1: Float) = x0 * y1 - y0 * x1

inline fun <R> cross(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val nx = y0 * z1 - z0 * y1
    val ny = z0 * x1 - x0 * z1
    val nz = x0 * y1 - y0 * x1
    return block(nx, ny, nz)
}

inline fun cross(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = cross(x0, y0, z0, x1, y1, z1) { nx, ny, nz -> dest.set(nx, ny, nz) }

inline fun lengthSquared(v: Vector2) = lengthSquared(v.x, v.y)

inline fun lengthSquared(v: Vector3) = lengthSquared(v.x, v.y, v.z)

inline fun lengthSquared(v: Vector4) = lengthSquared(v.x, v.y, v.z, v.w)

inline fun lengthSquared(x: Int, y: Int) = x * x + y * y

inline fun lengthSquared(x: Int, y: Int, z: Int) = x * x + y * y + z * z

inline fun lengthSquared(x: Int, y: Int, z: Int, w: Int) = x * x + y * y + z * z + w * w

inline fun length(v: Vector2) = length(v.x, v.y)

inline fun length(v: Vector3) = length(v.x, v.y, v.z)

inline fun length(v: Vector4) = length(v.x, v.y, v.z, v.w)

inline fun length(x: Int, y: Int) = sqrt(lengthSquared(x, y).toFloat()).toInt()

inline fun length(x: Int, y: Int, z: Int) = sqrt(lengthSquared(x, y, z).toFloat()).toInt()

inline fun length(x: Int, y: Int, z: Int, w: Int) = sqrt(lengthSquared(x, y, z, w).toFloat()).toInt()

inline fun distanceSquared(v0: Vector2, v1: Vector2) = distanceSquared(v0.x, v0.y, v1.x, v1.y)

inline fun distanceSquared(v0: Vector3, v1: Vector3) = distanceSquared(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z)

inline fun distanceSquared(v0: Vector4, v1: Vector4) = distanceSquared(v0.x, v0.y, v0.z, v0.w, v1.x, v1.y, v1.z, v1.w)

inline fun distanceSquared(x0: Int, y0: Int, x1: Int, y1: Int) = lengthSquared(x1 - x0, y1 - y0)

inline fun distanceSquared(x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int) = lengthSquared(x1 - x0, y1 - y0, z1 - z0)

inline fun distanceSquared(x0: Int, y0: Int, z0: Int, w0: Int, x1: Int, y1: Int, z1: Int, w1: Int) = lengthSquared(x1 - x0, y1 - y0, z1 - z0, w1 - w0)

inline fun distance(v0: Vector2, v1: Vector2) = distance(v0.x, v0.y, v1.x, v1.y)

inline fun distance(v0: Vector3, v1: Vector3) = distance(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z)

inline fun distance(v0: Vector4, v1: Vector4) = distance(v0.x, v0.y, v0.z, v0.w, v1.x, v1.y, v1.z, v1.w)

inline fun distance(x0: Int, y0: Int, x1: Int, y1: Int) = sqrt(distanceSquared(x0, y0, x1, y1).toFloat()).toInt()

inline fun distance(x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int) = sqrt(distanceSquared(x0, y0, z0, x1, y1, z1).toFloat()).toInt()

inline fun distance(x0: Int, y0: Int, z0: Int, w0: Int, x1: Int, y1: Int, z1: Int, w1: Int) = sqrt(distanceSquared(x0, y0, z0, w0, x1, y1, z1, w1).toFloat()).toInt()

inline fun <R> normalized(x: Float, y: Float, block: (Float, Float) -> R): R {
    val factor = 1.0f / length(x, y)
    return block(x * factor, y * factor)
}

inline fun <R> normalized(x: Float, y: Float, z: Float, block: (Float, Float, Float) -> R): R {
    val factor = 1.0f / length(x, y, z)
    return block(x * factor, y * factor, z * factor)
}

inline fun <R> normalized(x: Float, y: Float, z: Float, w: Float, block: (Float, Float, Float, Float) -> R): R {
    val factor = 1.0f / length(x, y, z, w)
    return block(x * factor, y * factor, z * factor, w * factor)
}

inline fun plus(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = plus(a.x, a.y, b.x, b.y, dest)

inline fun plus(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = plus(a.x, a.y, a.z, b.x, b.y, b.z, dest)

inline fun plus(a: Vector4, b: Vector4, dest: Vector4 = Vector4()) = plus(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w, dest)

inline fun plus(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = plus(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

inline fun plus(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = plus(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

inline fun plus(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, dest: Vector4 = Vector4()) = plus(x0, y0, z0, w0, x1, y1, z1, w1) { x, y, z, w -> dest.set(x, y, z, w) }

inline fun <R> plus(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x1 + x0
    val y = y1 + y0
    return block(x, y)
}

inline fun <R> plus(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x1 + x0
    val y = y1 + y0
    val z = z1 + z0
    return block(x, y, z)
}

inline fun <R> plus(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x1 + x0
    val y = y1 + y0
    val z = z1 + z0
    val w = w1 + w0
    return block(x, y, z, w)
}

inline fun sub(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = sub(a.x, a.y, b.x, b.y, dest)

inline fun sub(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = sub(a.x, a.y, a.z, b.x, b.y, b.z, dest)

inline fun sub(a: Vector4, b: Vector4, dest: Vector4 = Vector4()) = sub(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w, dest)

inline fun sub(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = sub(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

inline fun sub(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = sub(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

inline fun sub(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, dest: Vector4 = Vector4()) = sub(x0, y0, z0, w0, x1, y1, z1, w1) { x, y, z, w -> dest.set(x, y, z, w) }

inline fun <R> sub(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x1 - x0
    val y = y1 - y0
    return block(x, y)
}

inline fun <R> sub(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x1 - x0
    val y = y1 - y0
    val z = z1 - z0
    return block(x, y, z)
}

inline fun <R> sub(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x1 - x0
    val y = y1 - y0
    val z = z1 - z0
    val w = w1 - w0
    return block(x, y, z, w)
}

inline fun times(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = times(a.x, a.y, b.x, b.y, dest)

inline fun times(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = times(a.x, a.y, a.z, b.x, b.y, b.z, dest)

inline fun times(a: Vector4, b: Vector4, dest: Vector4 = Vector4()) = times(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w, dest)

inline fun times(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = times(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

inline fun times(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = times(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

inline fun times(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, dest: Vector4 = Vector4()) = times(x0, y0, z0, w0, x1, y1, z1, w1) { x, y, z, w -> dest.set(x, y, z, w) }

inline fun <R> times(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x1 * x0
    val y = y1 * y0
    return block(x, y)
}

inline fun <R> times(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x1 * x0
    val y = y1 * y0
    val z = z1 * z0
    return block(x, y, z)
}

inline fun <R> times(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x1 * x0
    val y = y1 * y0
    val z = z1 * z0
    val w = w1 * w0
    return block(x, y, z, w)
}

inline fun div(a: Vector2, b: Vector2, dest: Vector2 = Vector2()) = div(a.x, a.y, b.x, b.y, dest)

inline fun div(a: Vector3, b: Vector3, dest: Vector3 = Vector3()) = div(a.x, a.y, a.z, b.x, b.y, b.z, dest)

inline fun div(a: Vector4, b: Vector4, dest: Vector4 = Vector4()) = div(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w, dest)

inline fun div(x0: Float, y0: Float, x1: Float, y1: Float, dest: Vector2 = Vector2()) = div(x0, y0, x1, y1) { x, y -> dest.set(x, y) }

inline fun div(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, dest: Vector3 = Vector3()) = div(x0, y0, z0, x1, y1, z1) { x, y, z -> dest.set(x, y, z) }

inline fun div(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, dest: Vector4 = Vector4()) = div(x0, y0, z0, w0, x1, y1, z1, w1) { x, y, z, w -> dest.set(x, y, z, w) }

inline fun <R> div(x0: Float, y0: Float, x1: Float, y1: Float, block: (Float, Float) -> R): R {
    val x = x0 / x1
    val y = y0 / y1
    return block(x, y)
}

inline fun <R> div(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, block: (Float, Float, Float) -> R): R {
    val x = x0 / x1
    val y = y0 / y1
    val z = z0 / z1
    return block(x, y, z)
}

inline fun <R> div(x0: Float, y0: Float, z0: Float, w0: Float, x1: Float, y1: Float, z1: Float, w1: Float, block: (Float, Float, Float, Float) -> R): R {
    val x = x0 / x1
    val y = y0 / y1
    val z = z0 / z1
    val w = w0 / w1
    return block(x, y, z, w)
}
