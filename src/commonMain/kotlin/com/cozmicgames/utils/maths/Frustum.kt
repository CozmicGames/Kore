package com.cozmicgames.utils.maths

import kotlin.math.sqrt

class Frustum {
    private var nxX = 0.0f
    private var nxY = 0.0f
    private var nxZ = 0.0f
    private var nxW = 0.0f
    private var pxX = 0.0f
    private var pxY = 0.0f
    private var pxZ = 0.0f
    private var pxW = 0.0f
    private var nyX = 0.0f
    private var nyY = 0.0f
    private var nyZ = 0.0f
    private var nyW = 0.0f
    private var pyX = 0.0f
    private var pyY = 0.0f
    private var pyZ = 0.0f
    private var pyW = 0.0f
    private var nzX = 0.0f
    private var nzY = 0.0f
    private var nzZ = 0.0f
    private var nzW = 0.0f
    private var pzX = 0.0f
    private var pzY = 0.0f
    private var pzZ = 0.0f
    private var pzW = 0.0f

    fun update(m: Matrix4x4) {
        //nxX = m.m03 + m.m00
        //nxY = m.m13 + m.m10
        //nxZ = m.m23 + m.m20
        //nxW = m.m33 + m.m30
        //pxX = m.m03 - m.m00
        //pxY = m.m13 - m.m10
        //pxZ = m.m23 - m.m20
        //pxW = m.m33 - m.m30
        //nyX = m.m03 + m.m01
        //nyY = m.m13 + m.m11
        //nyZ = m.m23 + m.m21
        //nyW = m.m33 + m.m31
        //pyX = m.m03 - m.m01
        //pyY = m.m13 - m.m11
        //pyZ = m.m23 - m.m21
        //pyW = m.m33 - m.m31
        //nzX = m.m03 + m.m02
        //nzY = m.m13 + m.m12
        //nzZ = m.m23 + m.m22
        //nzW = m.m33 + m.m32
        //pzX = m.m03 - m.m02
        //pzY = m.m13 - m.m12
        //pzZ = m.m23 - m.m22
        //pzW = m.m33 - m.m32

        nxX = m.m03 + m.m00
        nxY = m.m13 + m.m10
        nxZ = m.m23 + m.m20
        nxW = m.m33 + m.m30
        var invl = 1.0f / sqrt(nxX * nxX + nxY * nxY + nxZ * nxZ)
        nxX *= invl
        nxY *= invl
        nxZ *= invl
        nxW *= invl
        pxX = m.m03 - m.m00
        pxY = m.m13 - m.m10
        pxZ = m.m23 - m.m20
        pxW = m.m33 - m.m30
        invl = 1.0f / sqrt(pxX * pxX + pxY * pxY + pxZ * pxZ)
        pxX *= invl
        pxY *= invl
        pxZ *= invl
        pxW *= invl
        nyX = m.m03 + m.m01
        nyY = m.m13 + m.m11
        nyZ = m.m23 + m.m21
        nyW = m.m33 + m.m31
        invl = 1.0f / sqrt(nyX * nyX + nyY * nyY + nyZ * nyZ)
        nyX *= invl
        nyY *= invl
        nyZ *= invl
        nyW *= invl
        pyX = m.m03 - m.m01
        pyY = m.m13 - m.m11
        pyZ = m.m23 - m.m21
        pyW = m.m33 - m.m31
        invl = 1.0f / sqrt(pyX * pyX + pyY * pyY + pyZ * pyZ)
        pyX *= invl
        pyY *= invl
        pyZ *= invl
        pyW *= invl
        nzX = m.m03 + m.m02
        nzY = m.m13 + m.m12
        nzZ = m.m23 + m.m22
        nzW = m.m33 + m.m32
        invl = 1.0f / sqrt(nzX * nzX + nzY * nzY + nzZ * nzZ)
        nzX *= invl
        nzY *= invl
        nzZ *= invl
        nzW *= invl
        pzX = m.m03 - m.m02
        pzY = m.m13 - m.m12
        pzZ = m.m23 - m.m22
        pzW = m.m33 - m.m32
        invl = 1.0f / sqrt(pzX * pzX + pzY * pzY + pzZ * pzZ)
        pzX *= invl
        pzY *= invl
        pzZ *= invl
        pzW *= invl
    }

    fun testBounds(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        return (nxX * (if (nxX < 0) minX else maxX) + nxY * (if (nxY < 0) minY else maxY) + nxZ * (if (nxZ < 0) minZ else maxZ)) >= -nxW &&
                (pxX * (if (pxX < 0) minX else maxX) + pxY * (if (pxY < 0) minY else maxY) + pxZ * (if (pxZ < 0) minZ else maxZ)) >= -pxW &&
                (nyX * (if (nyX < 0) minX else maxX) + nyY * (if (nyY < 0) minY else maxY) + nyZ * (if (nyZ < 0) minZ else maxZ)) >= -nyW &&
                (pyX * (if (pyX < 0) minX else maxX) + pyY * (if (pyY < 0) minY else maxY) + pyZ * (if (pyZ < 0) minZ else maxZ)) >= -pyW &&
                (nzX * (if (nzX < 0) minX else maxX) + nzY * (if (nzY < 0) minY else maxY) + nzZ * (if (nzZ < 0) minZ else maxZ)) >= -nzW &&
                (pzX * (if (pzX < 0) minX else maxX) + pzY * (if (pzY < 0) minY else maxY) + pzZ * (if (pzZ < 0) minZ else maxZ)) >= -pzW
    }

    fun testSphere(x: Float, y: Float, z: Float, r: Float): Boolean {
        return nxX * x + nxY * y + nxZ * z + nxW >= -r && pxX * x + pxY * y + pxZ * z + pxW >= -r && nyX * x + nyY * y + nyZ * z + nyW >= -r && pyX * x + pyY * y + pyZ * z + pyW >= -r && nzX * x + nzY * y + nzZ * z + nzW >= -r && pzX * x + pzY * y + pzZ * z + pzW >= -r
    }
}

fun Frustum.testBounds(box: BoundingBox) = testBounds(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
