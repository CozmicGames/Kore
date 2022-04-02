package com.gratedgames.utils.maths

import com.gratedgames.utils.collections.Resettable
import com.gratedgames.utils.extensions.element
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class Matrix4x4 : Resettable {
    companion object {
        val IDENTITY = Matrix4x4().setIdentity()
    }

    val data = Array(16) { 0.0f }

    var m00 by data.element(0)
    var m01 by data.element(1)
    var m02 by data.element(2)
    var m03 by data.element(3)
    var m10 by data.element(4)
    var m11 by data.element(5)
    var m12 by data.element(6)
    var m13 by data.element(7)
    var m20 by data.element(8)
    var m21 by data.element(9)
    var m22 by data.element(10)
    var m23 by data.element(11)
    var m30 by data.element(12)
    var m31 by data.element(13)
    var m32 by data.element(14)
    var m33 by data.element(15)

    init {
        setIdentity()
    }

    fun set(matrix: Matrix4x4) = set(matrix.data)

    fun set(array: Array<Float>): Matrix4x4 {
        array.copyInto(data)
        return this
    }

    fun setIdentity(): Matrix4x4 {
        m00 = 1.0f
        m01 = 0.0f
        m02 = 0.0f
        m03 = 0.0f
        m10 = 0.0f
        m11 = 1.0f
        m12 = 0.0f
        m13 = 0.0f
        m20 = 0.0f
        m21 = 0.0f
        m22 = 1.0f
        m23 = 0.0f
        m30 = 0.0f
        m31 = 0.0f
        m32 = 0.0f
        m33 = 1.0f
        return this
    }

    fun setZero(): Matrix4x4 {
        data.fill(0.0f)
        return this
    }

    fun transpose(): Matrix4x4 {
        val nm00 = m00
        val nm01 = m10
        val nm02 = m20
        val nm03 = m30
        val nm10 = m01
        val nm11 = m11
        val nm12 = m21
        val nm13 = m31
        val nm20 = m02
        val nm21 = m12
        val nm22 = m22
        val nm23 = m32
        val nm30 = m03
        val nm31 = m13
        val nm32 = m23
        val nm33 = m33
        m00 = nm00
        m01 = nm01
        m02 = nm02
        m03 = nm03
        m10 = nm10
        m11 = nm11
        m12 = nm12
        m13 = nm13
        m20 = nm20
        m21 = nm21
        m22 = nm22
        m23 = nm23
        m30 = nm30
        m31 = nm31
        m32 = nm32
        m33 = nm33
        return this
    }

    fun invert(): Matrix4x4 {
        val a = m00 * m11 - m01 * m10
        val b = m00 * m12 - m02 * m10
        val c = m00 * m13 - m03 * m10
        val d = m01 * m12 - m02 * m11
        val e = m01 * m13 - m03 * m11
        val f = m02 * m13 - m03 * m12
        val g = m20 * m31 - m21 * m30
        val h = m20 * m32 - m22 * m30
        val i = m20 * m33 - m23 * m30
        val j = m21 * m32 - m22 * m31
        val k = m21 * m33 - m23 * m31
        val l = m22 * m33 - m23 * m32
        val det = 1.0f / (a * l - b * k + c * j + d * i - e * h + f * g)
        val nm00 = (m11 * l - m12 * k + m13 * j) * det
        val nm01 = (-m01 * l + m02 * k - m03 * j) * det
        val nm02 = (m31 * f - m32 * e + m33 * d) * det
        val nm03 = (-m21 * f + m22 * e - m23 * d) * det
        val nm10 = (-m10 * l + m12 * i - m13 * h) * det
        val nm11 = (m00 * l - m02 * i + m03 * h) * det
        val nm12 = (-m30 * f + m32 * c - m33 * b) * det
        val nm13 = (m20 * f - m22 * c + m23 * b) * det
        val nm20 = (m10 * k - m11 * i + m13 * g) * det
        val nm21 = (-m00 * k + m01 * i - m03 * g) * det
        val nm22 = (m30 * e - m31 * c + m33 * a) * det
        val nm23 = (-m20 * e + m21 * c - m23 * a) * det
        val nm30 = (-m10 * j + m11 * h - m12 * g) * det
        val nm31 = (m00 * j - m01 * h + m02 * g) * det
        val nm32 = (-m30 * d + m31 * b - m32 * a) * det
        val nm33 = (m20 * d - m21 * b + m22 * a) * det
        m00 = nm00
        m01 = nm01
        m02 = nm02
        m03 = nm03
        m10 = nm10
        m11 = nm11
        m12 = nm12
        m13 = nm13
        m20 = nm20
        m21 = nm21
        m22 = nm22
        m23 = nm23
        m30 = nm30
        m31 = nm31
        m32 = nm32
        m33 = nm33
        return this
    }

    fun mul(matrix: Matrix4x4): Matrix4x4 {
        val nm00 = this.m00 * matrix.m00 + this.m10 * matrix.m01 + this.m20 * matrix.m02 + this.m30 * matrix.m03
        val nm01 = this.m01 * matrix.m00 + this.m11 * matrix.m01 + this.m21 * matrix.m02 + this.m31 * matrix.m03
        val nm02 = this.m02 * matrix.m00 + this.m12 * matrix.m01 + this.m22 * matrix.m02 + this.m32 * matrix.m03
        val nm03 = this.m03 * matrix.m00 + this.m13 * matrix.m01 + this.m23 * matrix.m02 + this.m33 * matrix.m03
        val nm10 = this.m00 * matrix.m10 + this.m10 * matrix.m11 + this.m20 * matrix.m12 + this.m30 * matrix.m13
        val nm11 = this.m01 * matrix.m10 + this.m11 * matrix.m11 + this.m21 * matrix.m12 + this.m31 * matrix.m13
        val nm12 = this.m02 * matrix.m10 + this.m12 * matrix.m11 + this.m22 * matrix.m12 + this.m32 * matrix.m13
        val nm13 = this.m03 * matrix.m10 + this.m13 * matrix.m11 + this.m23 * matrix.m12 + this.m33 * matrix.m13
        val nm20 = this.m00 * matrix.m20 + this.m10 * matrix.m21 + this.m20 * matrix.m22 + this.m30 * matrix.m23
        val nm21 = this.m01 * matrix.m20 + this.m11 * matrix.m21 + this.m21 * matrix.m22 + this.m31 * matrix.m23
        val nm22 = this.m02 * matrix.m20 + this.m12 * matrix.m21 + this.m22 * matrix.m22 + this.m32 * matrix.m23
        val nm23 = this.m03 * matrix.m20 + this.m13 * matrix.m21 + this.m23 * matrix.m22 + this.m33 * matrix.m23
        val nm30 = this.m00 * matrix.m30 + this.m10 * matrix.m31 + this.m20 * matrix.m32 + this.m30 * matrix.m33
        val nm31 = this.m01 * matrix.m30 + this.m11 * matrix.m31 + this.m21 * matrix.m32 + this.m31 * matrix.m33
        val nm32 = this.m02 * matrix.m30 + this.m12 * matrix.m31 + this.m22 * matrix.m32 + this.m32 * matrix.m33
        val nm33 = this.m03 * matrix.m30 + this.m13 * matrix.m31 + this.m23 * matrix.m32 + this.m33 * matrix.m33
        m00 = nm00
        m01 = nm01
        m02 = nm02
        m03 = nm03
        m10 = nm10
        m11 = nm11
        m12 = nm12
        m13 = nm13
        m20 = nm20
        m21 = nm21
        m22 = nm22
        m23 = nm23
        m30 = nm30
        m31 = nm31
        m32 = nm32
        m33 = nm33
        return this
    }

    fun setToOrtho(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float): Matrix4x4 {
        setIdentity()
        m00 = 2.0f / (right - left)
        m11 = 2.0f / (top - bottom)
        m22 = 2.0f / (zNear - zFar)
        m30 = (right + left) / (left - right)
        m31 = (top + bottom) / (bottom - top)
        m32 = (zFar + zNear) / (zNear - zFar)
        return this
    }

    fun setToOrtho2D(left: Float, right: Float, bottom: Float, top: Float): Matrix4x4 {
        setIdentity()
        m00 = 2.0F / (right - left)
        m11 = 2.0F / (top - bottom)
        m22 = -1.0F
        m30 = (right + left) / (left - right)
        m31 = (top + bottom) / (bottom - top)
        return this
    }

    fun setToPerspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float): Matrix4x4 {
        setZero()
        val h = tan(fovy * 0.5f)
        m00 = 1.0f / (h * aspect)
        m11 = 1.0f / h
        val farInf = zFar > 0.0f && zFar.isInfinite()
        val nearInf = zNear > 0.0f && zNear.isInfinite()
        if (farInf) {
            val e = 1.0E-6f
            m22 = e - 1.0f
            m32 = (e - 2.0f) * zNear
        } else if (nearInf) {
            val e = 1.0E-6f
            m22 = (1.0f) - e
            m32 = ((2.0f) - e) * zFar
        } else {
            m22 = (zFar + zNear) / (zNear - zFar)
            m32 = (zFar + zFar) * zNear / (zNear - zFar)
        }

        m23 = -1.0f

        return this
    }

    fun setToTranslationRotationScaling(translation: Vector3, rotation: Quaternion, scale: Vector3) = setToTranslationRotationScaling(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z, rotation.w, scale.x, scale.y, scale.z)

    fun setToTranslationRotationScaling(translationX: Float, translationY: Float, translationZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float, rotationW: Float, scaleX: Float, scaleY: Float, scaleZ: Float): Matrix4x4 {
        val dqx = rotationX + rotationX
        val dqy = rotationY + rotationY
        val dqz = rotationZ + rotationZ
        val q00 = dqx * rotationX
        val q11 = dqy * rotationY
        val q22 = dqz * rotationZ
        val q01 = dqx * rotationY
        val q02 = dqx * rotationZ
        val q03 = dqx * rotationW
        val q12 = dqy * rotationZ
        val q13 = dqy * rotationW
        val q23 = dqz * rotationW
        m00 = scaleX - (q11 + q22) * scaleX
        m01 = (q01 + q23) * scaleX
        m02 = (q02 - q13) * scaleX
        m03 = 0.0f
        m10 = (q01 - q23) * scaleY
        m11 = scaleY - (q22 + q00) * scaleY
        m12 = (q12 + q03) * scaleY
        m13 = 0.0f
        m20 = (q02 + q13) * scaleZ
        m21 = (q12 - q03) * scaleZ
        m22 = scaleZ - (q11 + q00) * scaleZ
        m23 = 0.0f
        m30 = translationX
        m31 = translationY
        m32 = translationZ
        m33 = 1.0f
        return this
    }

    fun setToLookAt(eye: Vector3, center: Vector3, up: Vector3) = setToLookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)

    fun setToLookAt(eyeX: Float, eyeY: Float, eyeZ: Float, centerX: Float, centerY: Float, centerZ: Float, upX: Float, upY: Float, upZ: Float): Matrix4x4 {
        var dirX = eyeX - centerX
        var dirY = eyeY - centerY
        var dirZ = eyeZ - centerZ
        val invDirLength = 1.0f / sqrt((dirX * dirX + dirY * dirY + dirZ * dirZ))
        dirX *= invDirLength
        dirY *= invDirLength
        dirZ *= invDirLength
        var leftX = upY * dirZ - upZ * dirY
        var leftY = upZ * dirX - upX * dirZ
        var leftZ = upX * dirY - upY * dirX
        val invLeftLength = 1.0f / sqrt((leftX * leftX + leftY * leftY + leftZ * leftZ))
        leftX *= invLeftLength
        leftY *= invLeftLength
        leftZ *= invLeftLength
        val upnX = dirY * leftZ - dirZ * leftY
        val upnY = dirZ * leftX - dirX * leftZ
        val upnZ = dirX * leftY - dirY * leftX
        m00 = leftX
        m01 = upnX
        m02 = dirX
        m03 = 0.0f
        m10 = leftY
        m11 = upnY
        m12 = dirY
        m13 = 0.0f
        m20 = leftZ
        m21 = upnZ
        m22 = dirZ
        m23 = 0.0f
        m30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ)
        m31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ)
        m32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ)
        m33 = 1.0f
        return this
    }

    fun setToLookAlong(direction: Vector3, up: Vector3) = setToLookAlong(direction.x, direction.y, direction.z, up.x, up.y, up.z)

    fun setToLookAlong(directionX: Float, directionY: Float, directionZ: Float, upX: Float, upY: Float, upZ: Float): Matrix4x4 {
        var dirX = directionX
        var dirY = directionY
        var dirZ = directionZ
        val invDirLength = 1.0f / sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        dirX *= -invDirLength
        dirY *= -invDirLength
        dirZ *= -invDirLength
        var leftX = upY * dirZ - upZ * dirY
        var leftY = upZ * dirX - upX * dirZ
        var leftZ = upX * dirY - upY * dirX
        val invLeftLength = 1.0f / sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)
        leftX *= invLeftLength
        leftY *= invLeftLength
        leftZ *= invLeftLength
        val upnX = dirY * leftZ - dirZ * leftY
        val upnY = dirZ * leftX - dirX * leftZ
        val upnZ = dirX * leftY - dirY * leftX
        m00 = leftX
        m01 = upnX
        m02 = dirX
        m03 = 0.0f
        m10 = leftY
        m11 = upnY
        m12 = dirY
        m13 = 0.0f
        m20 = leftZ
        m21 = upnZ
        m22 = dirZ
        m23 = 0.0f
        m30 = 0.0f
        m31 = 0.0f
        m32 = 0.0f
        m33 = 1.0f
        return this
    }

    fun setToRotation(axis: Vector3, angle: Float) = setToRotation(axis.x, axis.y, axis.z, angle)

    fun setToRotation(x: Float, y: Float, z: Float, angle: Float): Matrix4x4 {
        setIdentity()
        val sin = sin(angle)
        val cos = cos(angle)
        val c = 1.0f - cos
        val xy = x * y
        val xz = x * z
        val yz = y * z
        m00 = cos + x * x * c
        m10 = xy * c - z * sin
        m20 = xz * c + y * sin
        m01 = xy * c + z * sin
        m11 = cos + y * y * c
        m21 = yz * c - x * sin
        m02 = xz * c - y * sin
        m12 = yz * c + x * sin
        m22 = cos + z * z * c
        return this
    }

    fun transform(vector: Vector4) = transform(vector.x, vector.y, vector.z, vector.w) { x, y, z, w -> vector.set(x, y, z, w) }

    inline fun <R> transform(x: Float, y: Float, z: Float, w: Float, block: (Float, Float, Float, Float) -> R): R {
        val rx = transformX(x, y, z, w)
        val ry = transformY(x, y, z, w)
        val rz = transformZ(x, y, z, w)
        val rw = transformW(x, y, z, w)
        return block(rx, ry, rz, rw)
    }

    inline fun transformX(x: Float, y: Float, z: Float, w: Float) = m00 * x + m10 * y + m20 * z + m30 * w

    inline fun transformY(x: Float, y: Float, z: Float, w: Float) = m01 * x + m11 * y + m21 * z + m31 * w

    inline fun transformZ(x: Float, y: Float, z: Float, w: Float) = m02 * x + m12 * y + m22 * z + m32 * w

    inline fun transformW(x: Float, y: Float, z: Float, w: Float) = m03 * x + m13 * y + m23 * z + m33 * w

    fun project(vector: Vector4) = project(vector.x, vector.y, vector.z, vector.w) { x, y, z, w -> vector.set(x, y, z, w) }

    inline fun <R> project(x: Float, y: Float, z: Float, w: Float, block: (Float, Float, Float, Float) -> R): R {
        val invW = 1.0f / (m03 * x + m13 * y + m23 * z + m33 * w)
        val rx = (m00 * x + m10 * y + m20 * z + m30) * invW
        val ry = (m01 * x + m11 * y + m21 * z + m31) * invW
        val rz = (m02 * x + m12 * y + m22 * z + m32) * invW
        return block(rx, ry, rz, 1.0f)
    }

    fun project(vector: Vector3) = project(vector.x, vector.y, vector.z) { x, y, z -> vector.set(x, y, z) }

    inline fun <R> project(x: Float, y: Float, z: Float, block: (Float, Float, Float) -> R): R {
        val invW = 1.0f / fma(m03, x, fma(m13, y, fma(m23, z, m33)))
        val rx = fma(m00, x, fma(m10, y, fma(m20, z, m30))) * invW
        val ry = fma(m01, x, fma(m11, y, fma(m21, z, m31))) * invW
        val rz = fma(m02, x, fma(m12, y, fma(m22, z, m32))) * invW
        return block(rx, ry, rz)
    }

    fun unproject(winX: Float, winY: Float, winZ: Float, viewport: IntArray, vector: Vector3 = Vector3()): Vector3 {
        val a = m00 * m11 - m01 * m10
        val b = m00 * m12 - m02 * m10
        val c = m00 * m13 - m03 * m10
        val d = m01 * m12 - m02 * m11
        val e = m01 * m13 - m03 * m11
        val f = m02 * m13 - m03 * m12
        val g = m20 * m31 - m21 * m30
        val h = m20 * m32 - m22 * m30
        val i = m20 * m33 - m23 * m30
        val j = m21 * m32 - m22 * m31
        val k = m21 * m33 - m23 * m31
        val l = m22 * m33 - m23 * m32
        val det = 1.0f / (a * l - b * k + c * j + d * i - e * h + f * g)
        val im00 = (m11 * l - m12 * k + m13 * j) * det
        val im01 = (-m01 * l + m02 * k - m03 * j) * det
        val im02 = (m31 * f - m32 * e + m33 * d) * det
        val im03 = (-m21 * f + m22 * e - m23 * d) * det
        val im10 = (-m10 * l + m12 * i - m13 * h) * det
        val im11 = (m00 * l - m02 * i + m03 * h) * det
        val im12 = (-m30 * f + m32 * c - m33 * b) * det
        val im13 = (m20 * f - m22 * c + m23 * b) * det
        val im20 = (m10 * k - m11 * i + m13 * g) * det
        val im21 = (-m00 * k + m01 * i - m03 * g) * det
        val im22 = (m30 * e - m31 * c + m33 * a) * det
        val im23 = (-m20 * e + m21 * c - m23 * a) * det
        val im30 = (-m10 * j + m11 * h - m12 * g) * det
        val im31 = (m00 * j - m01 * h + m02 * g) * det
        val im32 = (-m30 * d + m31 * b - m32 * a) * det
        val im33 = (m20 * d - m21 * b + m22 * a) * det
        val ndcX = (winX - viewport[0].toFloat()) / viewport[2].toFloat() * 2.0f - 1.0f
        val ndcY = (winY - viewport[1].toFloat()) / viewport[3].toFloat() * 2.0f - 1.0f
        val ndcZ = winZ + winZ - 1.0f
        val invW = 1.0f / (im03 * ndcX + im13 * ndcY + im23 * ndcZ + im33)
        vector.x = (im00 * ndcX + im10 * ndcY + im20 * ndcZ + im30) * invW
        vector.y = (im01 * ndcX + im11 * ndcY + im21 * ndcZ + im31) * invW
        vector.z = (im02 * ndcX + im12 * ndcY + im22 * ndcZ + im32) * invW
        return vector
    }

    fun lerp(matrix: Matrix4x4, t: Float) {
        m00 = m00 + matrix.m00 - m00 * t
        m01 = m01 + matrix.m01 - m01 * t
        m02 = m02 + matrix.m02 - m02 * t
        m03 = m03 + matrix.m03 - m03 * t
        m10 = m10 + matrix.m10 - m10 * t
        m11 = m11 + matrix.m11 - m11 * t
        m12 = m12 + matrix.m12 - m12 * t
        m13 = m13 + matrix.m13 - m13 * t
        m20 = m20 + matrix.m20 - m20 * t
        m21 = m21 + matrix.m21 - m21 * t
        m22 = m22 + matrix.m22 - m22 * t
        m23 = m23 + matrix.m23 - m23 * t
        m30 = m30 + matrix.m30 - m30 * t
        m31 = m31 + matrix.m31 - m31 * t
        m32 = m32 + matrix.m32 - m32 * t
        m33 = m33 + matrix.m33 - m33 * t
    }

    fun getTranslation(vector: Vector3 = Vector3()): Vector3 {
        vector.x = m30
        vector.y = m31
        vector.z = m32
        return vector
    }

    fun getRotation(quaternion: Quaternion = Quaternion()): Quaternion {
        val tr = m00 + m11 + m22

        if (tr >= 0.0f) {
            var t = sqrt(tr + 1.0f)
            quaternion.w = t * 0.5f
            t = 0.5f / t
            quaternion.x = (m12 - m21) * t
            quaternion.y = (m20 - m02) * t
            quaternion.z = (m01 - m10) * t
        } else {
            if (m00 >= m11 && m00 >= m22) {
                var t = sqrt(m00 - (m11 + m22) + 1.0f)
                quaternion.x = t * 0.5f
                t = 0.5f / t
                quaternion.y = (m10 + m01) * t
                quaternion.z = (m02 + m20) * t
                quaternion.w = (m12 - m21) * t
            } else if (m11 > m22) {
                var t = sqrt(m11 - (m22 + m00) + 1.0f)
                quaternion.y = t * 0.5f
                t = 0.5f / t
                quaternion.z = (m21 + m12) * t
                quaternion.x = (m10 + m01) * t
                quaternion.w = (m20 - m02) * t
            } else {
                var t = sqrt(m22 - (m00 + m11) + 1.0f)
                quaternion.z = t * 0.5f
                t = 0.5f / t
                quaternion.x = (m02 + m20) * t
                quaternion.y = (m21 + m12) * t
                quaternion.w = (m01 - m10) * t
            }
        }

        return quaternion
    }

    fun getScaling(vector: Vector3 = Vector3()): Vector3 {
        vector.x = sqrt((m00 * m00) + (m01 * m01) + (m02 * m02))
        vector.y = sqrt((m10 * m10) + (m11 * m11) + (m12 * m12))
        vector.z = sqrt((m20 * m20) + (m21 * m21) + (m22 * m22))
        return vector
    }

    fun setTranslation(translation: Vector3): Matrix4x4 {
        setIdentity()
        m30 = translation.x
        m31 = translation.y
        m32 = translation.z

        return this
    }

    fun setRotation(rotation: Quaternion): Matrix4x4 {
        val w2 = rotation.w * rotation.w
        val x2 = rotation.x * rotation.x
        val y2 = rotation.y * rotation.y
        val z2 = rotation.z * rotation.z
        val zw = rotation.z * rotation.w
        val dzw = zw + zw
        val xy = rotation.x * rotation.y
        val dxy = xy + xy
        val xz = rotation.x * rotation.z
        val dxz = xz + xz
        val yw = rotation.y * rotation.w
        val dyw = yw + yw
        val yz = rotation.y * rotation.z
        val dyz = yz + yz
        val xw = rotation.x * rotation.w
        val dxw = xw + xw

        setIdentity()
        m00 = w2 + x2 - z2 - y2
        m01 = dxy + dzw
        m02 = dxz - dyw
        m10 = -dzw + dxy
        m11 = y2 - z2 + w2 - x2
        m12 = dyz + dxw
        m20 = dyw + dxz
        m21 = dyz - dxw
        m22 = z2 - y2 - x2 + w2

        return this
    }

    fun setScale(scale: Vector3): Matrix4x4 {
        setIdentity()
        m00 = scale.x
        m11 = scale.y
        m22 = scale.z

        return this
    }

    fun setToPerspectiveFrustumSlice(projection: Matrix4x4, near: Float, far: Float): Matrix4x4 {
        val invOldNear = (projection.m23 + projection.m22) / projection.m32
        val invNearFar = 1.0f / (near - far)
        m00 = projection.m00 * invOldNear * near
        m01 = projection.m01
        m02 = projection.m02
        m03 = projection.m03
        m10 = projection.m10
        m11 = projection.m11 * invOldNear * near
        m12 = projection.m12
        m13 = projection.m13
        m20 = projection.m20
        m21 = projection.m21
        m22 = (far + near) * invNearFar
        m23 = projection.m23
        m30 = projection.m30
        m31 = projection.m31
        m32 = (far + far) * near * invNearFar
        m33 = projection.m33

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (other is Matrix4x4)
            return data.contentDeepEquals(other.data)
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return data.contentDeepHashCode()
    }

    override fun toString(): String {
        return data.contentDeepToString()
    }

    override fun reset() {
        setIdentity()
    }

    fun copy() = Matrix4x4().set(this)
}

fun identityMatrix() = Matrix4x4.IDENTITY

operator fun Matrix4x4.times(v: Vector3) = transform(Vector4(v.x, v.y, v.z, 1.0f))

operator fun Matrix4x4.times(v: Vector4) = transform(v.copy())

operator fun Matrix4x4.times(m: Matrix4x4) = copy().mul(m)

fun Matrix4x4.transposed() = copy().transpose()
