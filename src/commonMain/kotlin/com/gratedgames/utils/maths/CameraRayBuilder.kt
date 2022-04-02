package com.gratedgames.utils.maths

class CameraRayBuilder() {
    constructor(camera: Camera) : this() {
        update(camera)
    }

    constructor(matrix: Matrix4x4) : this() {
        update(matrix)
    }

    private var nxnyX = 0.0f
    private var nxnyY = 0.0f
    private var nxnyZ = 0.0f
    private var pxnyX = 0.0f
    private var pxnyY = 0.0f
    private var pxnyZ = 0.0f
    private var pxpyX = 0.0f
    private var pxpyY = 0.0f
    private var pxpyZ = 0.0f
    private var nxpyX = 0.0f
    private var nxpyY = 0.0f
    private var nxpyZ = 0.0f
    private var cx = 0.0f
    private var cy = 0.0f
    private var cz = 0.0f

    fun update(camera: Camera) = update(camera.projectionView)

    fun update(projectionViewMatrix: Matrix4x4) {
        val nxX = projectionViewMatrix.m03 + projectionViewMatrix.m00
        val nxY = projectionViewMatrix.m13 + projectionViewMatrix.m10
        val nxZ = projectionViewMatrix.m23 + projectionViewMatrix.m20
        val d1 = projectionViewMatrix.m33 + projectionViewMatrix.m30
        val pxX = projectionViewMatrix.m03 - projectionViewMatrix.m00
        val pxY = projectionViewMatrix.m13 - projectionViewMatrix.m10
        val pxZ = projectionViewMatrix.m23 - projectionViewMatrix.m20
        val d2 = projectionViewMatrix.m33 - projectionViewMatrix.m30
        val nyX = projectionViewMatrix.m03 + projectionViewMatrix.m01
        val nyY = projectionViewMatrix.m13 + projectionViewMatrix.m11
        val nyZ = projectionViewMatrix.m23 + projectionViewMatrix.m21
        val pyX = projectionViewMatrix.m03 - projectionViewMatrix.m01
        val pyY = projectionViewMatrix.m13 - projectionViewMatrix.m11
        val pyZ = projectionViewMatrix.m23 - projectionViewMatrix.m21
        val d3 = projectionViewMatrix.m33 - projectionViewMatrix.m31
        nxnyX = nyY * nxZ - nyZ * nxY
        nxnyY = nyZ * nxX - nyX * nxZ
        nxnyZ = nyX * nxY - nyY * nxX
        pxnyX = pxY * nyZ - pxZ * nyY
        pxnyY = pxZ * nyX - pxX * nyZ
        pxnyZ = pxX * nyY - pxY * nyX
        nxpyX = nxY * pyZ - nxZ * pyY
        nxpyY = nxZ * pyX - nxX * pyZ
        nxpyZ = nxX * pyY - nxY * pyX
        pxpyX = pyY * pxZ - pyZ * pxY
        pxpyY = pyZ * pxX - pyX * pxZ
        pxpyZ = pyX * pxY - pyY * pxX
        val pxnxX = pxY * nxZ - pxZ * nxY
        val pxnxY = pxZ * nxX - pxX * nxZ
        val pxnxZ = pxX * nxY - pxY * nxX
        val invDot = 1.0f / (nxX * pxpyX + nxY * pxpyY + nxZ * pxpyZ)
        cx = (-pxpyX * d1 - nxpyX * d2 - pxnxX * d3) * invDot
        cy = (-pxpyY * d1 - nxpyY * d2 - pxnxY * d3) * invDot
        cz = (-pxpyZ * d1 - nxpyZ * d2 - pxnxZ * d3) * invDot
    }

    fun getOrigin(origin: Vector3 = Vector3()): Vector3 {
        origin.x = cx
        origin.y = cy
        origin.z = cz
        return origin
    }

    fun getDirection(x: Float, y: Float, direction: Vector3 = Vector3()): Vector3 {
        val y1x = nxnyX + (nxpyX - nxnyX) * y
        val y1y = nxnyY + (nxpyY - nxnyY) * y
        val y1z = nxnyZ + (nxpyZ - nxnyZ) * y
        val y2x = pxnyX + (pxpyX - pxnyX) * y
        val y2y = pxnyY + (pxpyY - pxnyY) * y
        val y2z = pxnyZ + (pxpyZ - pxnyZ) * y
        direction.x = y1x + (y2x - y1x) * x
        direction.y = y1y + (y2y - y1y) * x
        direction.z = y1z + (y2z - y1z) * x
        return direction.normalize()
    }
}