package com.cozmicgames.utils

import com.cozmicgames.utils.maths.FloatVector
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
Modified from https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/Color.java
 */

class Color(r: Float = 0.0f, g: Float = 0.0f, b: Float = 0.0f, a: Float = 0.0f) : FloatVector<Color>(4) {
    companion object {
        val HUE_COLORS = arrayOf(
            Color(1.0f, 0.0f, 0.0f, 1.0f),
            Color(1.0f, 1.0f, 0.0f, 1.0f),
            Color(0.0f, 1.0f, 0.0f, 1.0f),
            Color(0.0f, 1.0f, 1.0f, 1.0f),
            Color(0.0f, 0.0f, 1.0f, 1.0f),
            Color(1.0f, 0.0f, 1.0f, 1.0f)
        )

        val WHITE = Color(1.0f, 1.0f, 1.0f, 1.0f)
        val LIGHT_GRAY = Color(-0x40404001)
        val GRAY = Color(0x7f7f7fff)
        val DARK_GRAY = Color(0x3f3f3fff)
        val BLACK = Color(0.0f, 0.0f, 0.0f, 1.0f)
        val CLEAR = Color(0.0f, 0.0f, 0.0f, 0.0f)
        val BLUE = Color(0.0f, 0.0f, 1.0f, 1.0f)
        val NAVY = Color(0.0f, 0.0f, 0.5f, 1.0f)
        val ROYAL = Color(0x4169e1ff)
        val SLATE = Color(0x708090ff)
        val SKY = Color(-0x78311401)
        val CYAN = Color(0.0f, 1.0f, 1.0f, 1.0f)
        val TEAL = Color(0.0f, 0.5f, 0.5f, 1.0f)
        val GREEN = Color(0x00ff00ff)
        val CHARTREUSE = Color(0x7fff00ff)
        val LIME = Color(0x32cd32ff)
        val FOREST = Color(0x228b22ff)
        val OLIVE = Color(0x6b8e23ff)
        val YELLOW = Color(-0xff01)
        val GOLD = Color(-0x28ff01)
        val GOLDENROD = Color(-0x255adf01)
        val ORANGE = Color(-0x5aff01)
        val BROWN = Color(-0x74baec01)
        val TAN = Color(-0x2d4b7301)
        val FIREBRICK = Color(-0x4ddddd01)
        val RED = Color(-0xffff01)
        val SCARLET = Color(-0xcbe301)
        val CORAL = Color(-0x80af01)
        val SALMON = Color(-0x57f8d01)
        val PINK = Color(-0x964b01)
        val MAGENTA = Color(1.0f, 0.0f, 1.0f, 1.0f)
        val PURPLE = Color(-0x5fdf0f01)
        val VIOLET = Color(-0x117d1101)
        val MAROON = Color(-0x4fcf9f01)
        val TURQUOISE = Color(0x1ABC9CFF)
        val GREEN_SEA = Color(0x16A085FF)
        val SUN_FLOWER = Color(0xF1C40FFF.toInt())
        val EMERALD = Color(0x2ECC71FF)
        val NEPHRITIS = Color(0x27AE60FF)
        val CARROT = Color(0xE67E22FF.toInt())
        val PUMPKIN = Color(0xD35400FF.toInt())
        val PETER_RIVER = Color(0x3498DBFF)
        val BELIZE_HOLE = Color(0x2980B9FF)
        val ALIZARIN = Color(0xE74C3CFF.toInt())
        val POMEGRANATE = Color(0xC0392BFF.toInt())
        val AMETHYST = Color(0x9B59B6FF.toInt())
        val WISTERIA = Color(0x8E44ADFF.toInt())
        val CLOUDS = Color(0xECF0F1FF.toInt())
        val SILVER = Color(0xBDC3C7FF.toInt())
        val WET_ASPHALT = Color(0x34495EFF)
        val MIDNIGHT_BLUE = Color(0x2C3E50FF)
        val CONCRETE = Color(0x95A5A6FF.toInt())
        val ASBESTOS = Color(0x7F8C8DFF)

        fun toBits(r: Int, g: Int, b: Int, a: Int): Int {
            return a shl 24 or (b shl 16) or (g shl 8) or r
        }

        fun rgba(r: Float, g: Float, b: Float, a: Float): Int {
            return (r * 255).toInt() shl 24 or ((g * 255).toInt() shl 16) or ((b * 255).toInt() shl 8) or (a * 255).toInt()
        }

        fun rgba(color: Color): Int {
            return (color.r * 255).toInt() shl 24 or ((color.g * 255).toInt() shl 16) or ((color.b * 255).toInt() shl 8) or (color.a * 255).toInt()
        }

        fun setFromRGBA(color: Color, value: Int): Color {
            color.r = (value and -0x1000000 ushr 24) / 255f
            color.g = (value and 0x00ff0000 ushr 16) / 255f
            color.b = (value and 0x0000ff00 ushr 8) / 255f
            color.a = (value and 0x000000ff) / 255f
            return color
        }

        fun setFromARGB(color: Color, value: Int): Color {
            color.a = (value and -0x1000000 ushr 24) / 255f
            color.r = (value and 0x00ff0000 ushr 16) / 255f
            color.g = (value and 0x0000ff00 ushr 8) / 255f
            color.b = (value and 0x000000ff) / 255f
            return color
        }

        fun setFromABGR(color: Color, value: Int): Color {
            color.a = (value and -0x1000000 ushr 24) / 255f
            color.b = (value and 0x00ff0000 ushr 16) / 255f
            color.g = (value and 0x0000ff00 ushr 8) / 255f
            color.r = (value and 0x000000ff) / 255f
            return color
        }

        fun setFromHexString(color: Color, hex: String): Color {
            try {
                val values = Hex.decode(hex)
                color.r = values[0] / 255.0f
                color.g = values[1] / 255.0f
                color.b = values[2] / 255.0f
                color.a = if (values.size > 3)
                    values[3] / 255.0f
                else
                    1.0f
            } catch (e: Exception) {
            }

            return color
        }

        fun fromRGBA(value: Int) = setFromRGBA(Color(), value)

        fun fromARGB(value: Int) = setFromARGB(Color(), value)

        fun fromABGR(value: Int) = setFromABGR(Color(), value)

        fun fromHexString(hex: String) = setFromHexString(Color(), hex)

        fun random(random: Random = Random) = Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat())
    }

    var r by xComponent()

    var g by yComponent()

    var b by zComponent()

    var a by wComponent()

    constructor(rgba: Int) : this() {
        setFromRGBA(this, rgba)
    }

    constructor(color: Color) : this() {
        set(color)
    }

    init {
        set(r, g, b, a)
        clamp()
    }

    val bits get() = (0xFF * a).toInt() shl 24 or ((0xFF * b).toInt() shl 16) or ((0xFF * g).toInt() shl 8) or (0xFF * r).toInt()

    val abgrBits get() = (0xFF * r).toInt() shl 24 or ((0xFF * g).toInt() shl 16) or ((0xFF * b).toInt() shl 8) or (0xFF * a).toInt()

    private fun clamp(): Color {
        if (r < 0.0f) r = 0.0f else if (r > 1.0f) r = 1.0f
        if (g < 0.0f) g = 0.0f else if (g > 1.0f) g = 1.0f
        if (b < 0.0f) b = 0.0f else if (b > 1.0f) b = 1.0f
        if (a < 0.0f) a = 0.0f else if (a > 1.0f) a = 1.0f
        return this
    }

    fun set(r: Float, g: Float, b: Float, a: Float): Color {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
        return clamp()
    }

    fun set(rgba: Int): Color {
        setFromRGBA(this, rgba)
        return this
    }

    fun lerp(r: Float, g: Float, b: Float, a: Float, t: Float): Color {
        this.r += t * (r - this.r)
        this.g += t * (g - this.g)
        this.b += t * (b - this.b)
        this.a += t * (a - this.a)
        return clamp()
    }

    fun fromHSV(h: Float, s: Float, v: Float): Color {
        val x = (h / 60f + 6) % 6
        val i = x.toInt()
        val f = x - i
        val p = v * (1 - s)
        val q = v * (1 - s * f)
        val t = v * (1 - s * (1 - f))
        when (i) {
            0 -> {
                r = v
                g = t
                b = p
            }
            1 -> {
                r = q
                g = v
                b = p
            }
            2 -> {
                r = p
                g = v
                b = t
            }
            3 -> {
                r = p
                g = q
                b = v
            }
            4 -> {
                r = t
                g = p
                b = v
            }
            else -> {
                r = v
                g = p
                b = q
            }
        }
        return clamp()
    }

    fun fromHSV(hsv: FloatArray, offset: Int = 0): Color {
        require(hsv.size - offset >= 3)
        return fromHSV(hsv[offset], hsv[offset + 1], hsv[offset + 2])
    }

    fun toHSV(hsv: FloatArray = FloatArray(3)): FloatArray {
        require(hsv.size >= 3)
        val max: Float = max(max(r, g), b)
        val min: Float = min(min(r, g), b)
        val range = max - min
        if (range == 0f) {
            hsv[0] = 0.0f
        } else if (max == r) {
            hsv[0] = (60 * (g - b) / range + 360) % 360
        } else if (max == g) {
            hsv[0] = 60 * (b - r) / range + 120
        } else {
            hsv[0] = 60 * (r - g) / range + 240
        }
        if (max > 0) {
            hsv[1] = 1 - min / max
        } else {
            hsv[1] = 0.0f
        }
        hsv[2] = max
        return hsv
    }

    fun toHexString() = Hex.encodeUpper(byteArrayOf((r * 0xFF).toInt().toByte(), (g * 0xFF).toInt().toByte(), (b * 0xFF).toInt().toByte(), (a * 0xFF).toInt().toByte()))

    override fun copy() = Color(r, g, b, a)
}

val FloatArray.h get() = component1()
val FloatArray.s get() = component2()
val FloatArray.v get() = component3()
