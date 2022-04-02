package com.gratedgames.utils.maths

import com.gratedgames.utils.Time
import com.gratedgames.utils.extensions.clamp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

const val PI = 3.1415926536f
const val HALF_PI = PI * 0.5f
const val TWO_PI = PI * 2.0f
const val FOUR_PI = PI * 4.0f
const val INV_PI = 1.0f / PI
const val INV_TWO_PI = INV_PI * 0.5f
const val INV_FOUR_PI = INV_PI * 0.25f

val FLOAT_EPSILON by lazy {
    var p = 0
    var e = 1.0f
    while (e + 1.0f != 1.0f) {
        e *= 0.5f
        p--
    }
    2.0f.pow(p + 1)
}

private val random = Random(Time.current.toLong())

inline fun toDegrees(radians: Float) = radians * 180.0f / PI

inline fun toRadians(degrees: Float) = degrees * PI / 180.0f

inline fun Float.smoothstep(edge0: Float, edge1: Float): Float {
    val v = (this - edge0) / (edge1 - edge0)
    val step2 = v.clamp(0.0f, 1.0f)
    return step2 * step2 * (3 - 2 * step2)
}

inline fun Float.convertRange(minSrc: Float, maxSrc: Float, minDst: Float, maxDst: Float): Float = (((this - minSrc) / (maxSrc - minSrc)) * (maxDst - minDst)) + minDst

inline fun lerp(min: Float, max: Float, t: Float) = min + (max - min) * t

inline fun mix(a: Float, b: Float, x: Float) = a * (1.0f - x) + b * x

inline fun fract(v: Float) = v % 1

fun randomInt(range: IntRange? = null) = if (range == null) random.nextInt() else random.nextInt(range.first, range.last)

fun randomInt(until: Int) = random.nextInt(until)

fun randomFloat() = random.nextFloat()

fun randomBoolean() = random.nextBoolean()

fun randomBytes(array: ByteArray, offset: Int = 0, length: Int = array.size - offset) = random.nextBytes(array, offset, offset + length)

inline fun det(x0: Float, y0: Float, x1: Float, y1: Float) = x0 * y1 - y0 * x1

inline fun isCCW(p: Vector2, q: Vector2, r: Vector2) = det(q.x - p.x, q.y - p.y, r.x - p.x, r.y - p.y) >= 0.0f

inline fun areCollinear(p: Vector2, q: Vector2, r: Vector2) = abs(det(q.x - p.x, q.y - p.y, r.x - p.x, r.y - p.y)) <= 1e-32f

inline fun arePointsOnSameSide(a: Vector2, b: Vector2, c: Vector2, d: Vector2): Boolean {
    val px = d.x - c.x
    val py = d.y - c.y
    val l = det(px, py, a.x - c.x, a.y - c.y)
    val m = det(px, py, b.x - c.x, b.y - c.y)
    return l * m >= 0.0f
}

inline fun isPointInTriangle(p: Vector2, a: Vector2, b: Vector2, c: Vector2) = arePointsOnSameSide(p, a, b, c) && arePointsOnSameSide(p, b, a, c) && arePointsOnSameSide(p, c, a, b)

inline fun isAnyPointInTriangle(points: Iterable<Vector2>, a: Vector2, b: Vector2, c: Vector2): Boolean {
    for (point in points)
        if (point != a && point != b && point != c && isPointInTriangle(point, a, b, c))
            return true

    return false
}

inline fun isEar(points: Iterable<Vector2>, p: Vector2, q: Vector2, r: Vector2) = isCCW(p, q, r) && isAnyPointInTriangle(points, p, q, r)

inline fun fma(a: Float, b: Float, c: Float) = a * b + c

fun smoothDamp(current: Vector2, target: Vector2, currentVelocity: Vector2, smoothingTime: Float, maxSpeed: Float, deltaTime: Float, dest: Vector2 = Vector2()): Vector2 {
    var outputX = 0.0f
    var outputY = 0.0f
    val omega = 2.0f / max(FLOAT_EPSILON, smoothingTime)
    val x = omega * deltaTime
    val exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x)
    var changeX = current.x - target.x
    var changeY = current.y - target.y
    val maxChange = maxSpeed * smoothingTime
    val maxChangeSq = maxChange * maxChange
    val sqrmag: Float = changeX * changeX + changeY * changeY
    if (sqrmag > maxChangeSq) {
        val mag = sqrt(sqrmag)
        changeX = changeX / mag * maxChange
        changeY = changeY / mag * maxChange
    }
    val targetX = current.x - changeX
    val targetY = current.y - changeY

    val tempX = (currentVelocity.x + omega * changeX) * deltaTime
    val tempY = (currentVelocity.y + omega * changeY) * deltaTime

    currentVelocity.x = (currentVelocity.x - omega * tempX) * exp
    currentVelocity.y = (currentVelocity.y - omega * tempY) * exp

    outputX = targetX + (changeX + tempX) * exp
    outputY = targetY + (changeY + tempY) * exp

    val origMinusCurrentX = target.x - current.x
    val origMinusCurrentY = target.y - current.y
    val outMinusOrigX = outputX - target.x
    val outMinusOrigY = outputY - target.y

    if (origMinusCurrentX * outMinusOrigX + origMinusCurrentY * outMinusOrigY > 0.0f) {
        outputX = target.x
        outputY = target.y
        currentVelocity.x = (outputX - target.x) / deltaTime
        currentVelocity.y = (outputY - target.y) / deltaTime
    }

    return dest.set(outputX, outputY)
}

fun smoothDamp(current: Vector3, target: Vector3, currentVelocity: Vector3, smoothingTime: Float, maxSpeed: Float, deltaTime: Float, dest: Vector3 = Vector3()): Vector3 {
    var outputX = 0.0f
    var outputY = 0.0f
    var outputZ = 0.0f
    val omega = 2.0f / max(FLOAT_EPSILON, smoothingTime)
    val x = omega * deltaTime
    val exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x)
    var changeX = current.x - target.x
    var changeY = current.y - target.y
    var changeZ = current.z - target.z
    val maxChange = maxSpeed * smoothingTime
    val maxChangeSq = maxChange * maxChange
    val sqrmag: Float = changeX * changeX + changeY * changeY + changeZ * changeZ
    if (sqrmag > maxChangeSq) {
        val mag = sqrt(sqrmag)
        changeX = changeX / mag * maxChange
        changeY = changeY / mag * maxChange
        changeZ = changeZ / mag * maxChange
    }
    val targetX = current.x - changeX
    val targetY = current.y - changeY
    val targetZ = current.z - changeZ

    val tempX = (currentVelocity.x + omega * changeX) * deltaTime
    val tempY = (currentVelocity.y + omega * changeY) * deltaTime
    val tempZ = (currentVelocity.z + omega * changeZ) * deltaTime

    currentVelocity.x = (currentVelocity.x - omega * tempX) * exp
    currentVelocity.y = (currentVelocity.y - omega * tempY) * exp
    currentVelocity.z = (currentVelocity.z - omega * tempZ) * exp

    outputX = targetX + (changeX + tempX) * exp
    outputY = targetY + (changeY + tempY) * exp
    outputZ = targetZ + (changeZ + tempZ) * exp

    val origMinusCurrentX = target.x - current.x
    val origMinusCurrentY = target.y - current.y
    val origMinusCurrentZ = target.z - current.z
    val outMinusOrigX = outputX - target.x
    val outMinusOrigY = outputY - target.y
    val outMinusOrigZ = outputZ - target.z

    if (origMinusCurrentX * outMinusOrigX + origMinusCurrentY * outMinusOrigY + origMinusCurrentZ * outMinusOrigZ > 0.0f) {
        outputX = target.x
        outputY = target.y
        outputZ = target.z
        currentVelocity.x = (outputX - target.x) / deltaTime
        currentVelocity.y = (outputY - target.y) / deltaTime
        currentVelocity.z = (outputZ - target.z) / deltaTime
    }

    return dest.set(outputX, outputY, outputZ)
}

inline fun forEachLinePoint(x0: Int, y0: Int, x1: Int, y1: Int, block: (Int, Int) -> Unit) {
    var d = 0
    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    val dx2 = dx shl 1
    val dy2 = dy shl 1
    val ix = if (x0 < x1) 1 else -1
    val iy = if (y0 < y1) 1 else -1
    var xx = x0
    var yy = y0
    if (dy <= dx)
        while (true) {
            block(xx, yy)
            if (xx == x1)
                break
            xx += ix
            d += dy2
            if (d > dx) {
                yy += iy
                d -= dx2
            }
        }
    else
        while (true) {
            block(xx, yy)
            if (yy == y1)
                break
            yy += iy
            d += dx2
            if (d > dy) {
                xx += ix
                d -= dy2
            }
        }
}
