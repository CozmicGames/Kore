package com.cozmicgames.utils.noise

import kotlin.experimental.and
import kotlin.math.floor
import kotlin.math.sqrt

object SimplexNoise : NoiseModel {
    private class Grad(var x: Float, var y: Float, var z: Float)

    private val grad3 = arrayOf(
        Grad(1.0f, 1.0f, 0.0f), Grad(-1.0f, 1.0f, 0.0f), Grad(1.0f, -1.0f, 0.0f), Grad(-1.0f, -1.0f, 0.0f),
        Grad(1.0f, 0.0f, 1.0f), Grad(-1.0f, 0.0f, 1.0f), Grad(1.0f, 0.0f, -1.0f), Grad(-1.0f, 0.0f, -1.0f),
        Grad(0.0f, 1.0f, 1.0f), Grad(0.0f, -1.0f, 1.0f), Grad(0.0f, 1.0f, -1.0f), Grad(0.0f, -1.0f, -1.0f)
    )

    private val p = shortArrayOf(
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
        8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117,
        35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134,
        139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46,
        245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200,
        196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123,
        5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
        223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
        251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
        49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
        138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    )

    private val perm = ShortArray(512)
    private val permMod12 = ShortArray(512)
    private val F2 = 0.5f * (sqrt(3.0f) - 1.0f)
    private val G2 = (3.0f - sqrt(3.0f)) / 6.0f
    private val F3 = 1.0f / 3.0f
    private val G3 = 1.0f / 6.0f

    init {
        var i = 0
        while (i < 512) {
            perm[i] = p[i and 0xFF]
            permMod12[i] = (perm[i] % 12).toShort()
            i++
        }
    }

    private fun dot(g: Grad, x: Float, y: Float): Float {
        return g.x * x + g.y * y
    }

    private fun dot(g: Grad, x: Float, y: Float, z: Float): Float {
        return g.x * x + g.y * y + g.z * z
    }

    override fun noise(x: Float, y: Float): Float {
        val n0: Float
        val n1: Float
        val n2: Float

        val s = (x + y) * F2
        val i = floor(x + s).toInt()
        val j = floor(y + s).toInt()
        val t = (i + j) * G2
        val x0 = x - (i - t)
        val y0 = y - (j - t)

        val i1: Int
        val j1: Int

        if (x0 > y0) {
            i1 = 1
            j1 = 0
        } else {
            i1 = 0
            j1 = 1
        }

        val x1 = x0 - i1 + G2
        val y1 = y0 - j1 + G2
        val x2 = x0 - 1.0f + 2.0f * G2
        val y2 = y0 - 1.0f + 2.0f * G2

        val ii = i and 0xFF
        val jj = j and 0xFF
        val gi0 = permMod12[ii + perm[jj]].toInt()
        val gi1 = permMod12[ii + i1 + perm[jj + j1].toInt()].toInt()
        val gi2 = permMod12[ii + 1 + perm[jj + 1].toInt()].toInt()

        var t0 = 0.5f - x0 * x0 - y0 * y0

        if (t0 < 0)
            n0 = 0.0f
        else {
            t0 *= t0
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0)
        }

        var t1 = 0.5f - x1 * x1 - y1 * y1

        if (t1 < 0)
            n1 = 0.0f
        else {
            t1 *= t1
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1)
        }

        var t2 = 0.5f - x2 * x2 - y2 * y2

        if (t2 < 0)
            n2 = 0.0f
        else {
            t2 *= t2
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2)
        }

        return 70.0f * (n0 + n1 + n2)
    }

    override fun noise(x: Float, y: Float, z: Float): Float {
        val n0: Float
        val n1: Float
        val n2: Float
        val n3: Float
        val s = (x + y + z) * F3
        val i = floor(x + s).toInt()
        val j = floor(y + s).toInt()
        val k = floor(z + s).toInt()
        val t = (i + j + k) * G3
        val X0 = i - t
        val Y0 = j - t
        val Z0 = k - t
        val x0 = x - X0
        val y0 = y - Y0
        val z0 = z - Z0
        val i1: Int
        val j1: Int
        val k1: Int
        val i2: Int
        val j2: Int
        val k2: Int

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1
                j1 = 0
                k1 = 0
                i2 = 1
                j2 = 1
                k2 = 0
            } else if (x0 >= z0) {
                i1 = 1
                j1 = 0
                k1 = 0
                i2 = 1
                j2 = 0
                k2 = 1
            } else {
                i1 = 0
                j1 = 0
                k1 = 1
                i2 = 1
                j2 = 0
                k2 = 1
            }
        } else {
            if (y0 < z0) {
                i1 = 0
                j1 = 0
                k1 = 1
                i2 = 0
                j2 = 1
                k2 = 1
            } else if (x0 < z0) {
                i1 = 0
                j1 = 1
                k1 = 0
                i2 = 0
                j2 = 1
                k2 = 1
            } else {
                i1 = 0
                j1 = 1
                k1 = 0
                i2 = 1
                j2 = 1
                k2 = 0
            }
        }

        val x1 = x0 - i1 + G3
        val y1 = y0 - j1 + G3
        val z1 = z0 - k1 + G3
        val x2 = x0 - i2 + 2.0f * G3
        val y2 = y0 - j2 + 2.0f * G3
        val z2 = z0 - k2 + 2.0f * G3
        val x3 = x0 - 1.0f + 3.0f * G3
        val y3 = y0 - 1.0f + 3.0f * G3
        val z3 = z0 - 1.0f + 3.0f * G3

        val ii = i and 255
        val jj = j and 255
        val kk = k and 255
        val gi0 = (permMod12[ii + perm[jj + perm[kk] and 0xFF] and 0xFF] and 0xFF).toInt()
        val gi1 = (permMod12[ii + i1 + perm[jj + j1 + perm[kk + k1] and 0xFF] and 0xFF] and 0xFF).toInt()
        val gi2 = (permMod12[ii + i2 + perm[jj + j2 + perm[kk + k2] and 0xFF] and 0xFF] and 0xFF).toInt()
        val gi3 = (permMod12[ii + 1 + perm[jj + 1 + perm[kk + 1] and 0xFF] and 0xFF] and 0xFF).toInt()

        var t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0
        if (t0 < 0.0f) n0 = 0.0f else {
            t0 *= t0
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0)
        }
        var t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1
        if (t1 < 0.0f) n1 = 0.0f else {
            t1 *= t1
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1)
        }
        var t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2
        if (t2 < 0.0f) n2 = 0.0f else {
            t2 *= t2
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2)
        }
        var t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3
        if (t3 < 0.0f) n3 = 0.0f else {
            t3 *= t3
            n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3)
        }
        return 32.0f * (n0 + n1 + n2 + n3)
    }
}