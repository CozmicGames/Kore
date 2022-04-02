package com.gratedgames.utils.maths

import kotlin.math.*

typealias Quaternion = Vector4

fun Quaternion.setIdentity(): Quaternion {
    x = 0.0f
    y = 0.0f
    z = 0.0f
    w = 1.0f
    return this
}

fun Quaternion.setFromAxis(axisX: Float, axisY: Float, axisZ: Float, angle: Float): Quaternion {
    var d = length(axisX, axisY, axisZ)
    if (d == 0.0f)
        return setIdentity()

    d = 1.0f / d

    val ang: Float = if (angle < 0)
        TWO_PI - (-angle % TWO_PI)
    else
        angle % TWO_PI

    val sin = sin(ang / 2.0f)
    val cos = cos(ang / 2.0f)
    return set(d * x * sin, d * y * sin, d * z * sin, cos).normalize()
}

fun Quaternion.angle(): Float {
    val angle = 2.0f * acos(w)
    return if (angle <= PI)
        angle
    else
        PI * 2.0f - angle
}

fun Quaternion.rotate(angleX: Float, angleY: Float, angleZ: Float): Quaternion {
    val sx = sin(angleX * 0.5f)
    val cx = cos(angleX * 0.5f)
    val sy = sin(angleY * 0.5f)
    val cy = cos(angleY * 0.5f)
    val sz = sin(angleZ * 0.5f)
    val cz = cos(angleZ * 0.5f)

    val cycz = cy * cz
    val sysz = sy * sz
    val sycz = sy * cz
    val cysz = cy * sz
    val w = cx * cycz - sx * sysz
    val x = sx * cycz + cx * sysz
    val y = cx * sycz - sx * cysz
    val z = cx * cysz + sx * sycz

    this.x = this.w * x + this.x * w + this.y * z - this.z * y
    this.y = this.w * y - this.x * z + this.y * w + this.z * x
    this.z = this.w * z + this.x * y - this.y * x + this.z * w
    this.w = this.w * w - this.x * x - this.y * y - this.z * z

    return this
}

fun Quaternion.setToLookAlong(direction: Vector3, up: Vector3) = setToLookAlong(direction.x, direction.y, direction.z, up.x, up.y, up.z)

fun Quaternion.setToLookAlong(x: Float, y: Float, z: Float, upX: Float = 0.0f, upY: Float = 1.0f, upZ: Float = 0.0f): Quaternion {
    val invDirLength = 1.0f / sqrt(x * x + y * y + z * z)
    val dirnX = -x * invDirLength
    val dirnY = -y * invDirLength
    val dirnZ = -z * invDirLength

    var leftX = upY * dirnZ - upZ * dirnY
    var leftY = upZ * dirnX - upX * dirnZ
    var leftZ = upX * dirnY - upY * dirnX

    val invLeftLength = 1.0f / sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)
    leftX *= invLeftLength
    leftY *= invLeftLength
    leftZ *= invLeftLength

    val upnX = dirnY * leftZ - dirnZ * leftY
    val upnY = dirnZ * leftX - dirnX * leftZ
    val upnZ = dirnX * leftY - dirnY * leftX

    val x: Float
    val y: Float
    val z: Float
    val w: Float
    var t: Float
    val tr = leftX + upnY + dirnZ
    if (tr >= 0.0) {
        t = sqrt(tr + 1.0f)
        w = t * 0.5f
        t = 0.5f / t
        x = (dirnY - upnZ) * t
        y = (leftZ - dirnX) * t
        z = (upnX - leftY) * t
    } else {
        if (leftX > upnY && leftX > dirnZ) {
            t = sqrt(1.0f + leftX - upnY - dirnZ)
            x = t * 0.5f
            t = 0.5f / t
            y = (leftY + upnX) * t
            z = (dirnX + leftZ) * t
            w = (dirnY - upnZ) * t
        } else if (upnY > dirnZ) {
            t = sqrt(1.0f + upnY - leftX - dirnZ)
            y = t * 0.5f
            t = 0.5f / t
            x = (leftY + upnX) * t
            z = (upnZ + dirnY) * t
            w = (leftZ - dirnX) * t
        } else {
            t = sqrt(1.0f + dirnZ - leftX - upnY)
            z = t * 0.5f
            t = 0.5f / t
            x = (dirnX + leftZ) * t
            y = (upnZ + dirnY) * t
            w = (upnX - leftY) * t
        }
    }

    this.x = fma(this.w, x, fma(this.x, w, fma(this.y, z, -this.z * y)))
    this.y = fma(this.w, y, fma(-this.x, z, fma(this.y, w, this.z * x)))
    this.z = fma(this.w, z, fma(this.x, y, fma(-this.y, x, this.z * w)))
    this.w = fma(this.w, w, fma(-this.x, x, fma(-this.y, y, -this.z * z)))

    return this
}

fun Quaternion.transform(vector: Vector3) = transform(vector.x, vector.y, vector.z) { x, y, z ->
    vector.set(x, y, z)
}

inline fun <R> Quaternion.transform(x: Float, y: Float, z: Float, block: (Float, Float, Float) -> R): R {
    val xx = this.x * this.x
    val yy = this.y * this.y
    val zz = this.z * this.z
    val ww = this.w * this.w
    val xy = this.x * this.y
    val xz = this.x * this.z
    val yz = this.y * this.z
    val xw = this.x * this.w
    val zw = this.z * this.w
    val yw = this.y * this.w
    val k = 1.0f / (xx + yy + zz + ww)

    val nx = fma((xx - yy - zz + ww) * k, x, fma(2.0f * (xy - zw) * k, y, (2.0f * (xz + yw) * k) * z))
    val ny = fma(2.0f * (xy + zw) * k, x, fma((yy - xx - zz + ww) * k, y, (2.0f * (yz - xw) * k) * z))
    val nz = fma(2.0f * (xz - yw) * k, x, fma(2.0f * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z))

    return block(nx, ny, nz)
}

fun Quaternion.getEulerAngles(dest: Vector3 = Vector3()): Vector3 {
    dest.x = atan2(2.0f * (x * w - y * z), 1.0f - 2.0f * (x * x + y * y))
    dest.y = asin(2.0f * (x * z + y * w))
    dest.z = atan2(2.0f * (z * w - x * y), 1.0f - 2.0f * (y * y + z * z))
    return dest
}

fun Quaternion.setFromEulerAngles(angles: Vector3): Quaternion {
    val sx = sin(angles.x * 0.5f)
    val cx = cos(angles.x * 0.5f)
    val sy = sin(angles.y * 0.5f)
    val cy = cos(angles.y * 0.5f)
    val sz = sin(angles.z * 0.5f)
    val cz = cos(angles.z * 0.5f)

    val cycz = cy * cz
    val sysz = sy * sz
    val sycz = sy * cz
    val cysz = cy * sz
    w = cx * cycz - sx * sysz
    x = sx * cycz + cx * sysz
    y = cx * sycz - sx * cysz
    z = cx * cysz + sx * sycz

    return this
}
