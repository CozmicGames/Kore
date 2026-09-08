package com.cozmicgames.core.graphics

import com.cozmicgames.core.utils.Color
import com.cozmicgames.core.utils.maths.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Creates mipmaps from the gives [Image].
 *
 * @param image The image to create mipmaps from.
 * @param minSize The minimum size of a mip level.
 */
class MipMapImage(image: Image, minSize: Int = 1, private val filter: Filter = Filter.Mitchell) : Iterable<MipMapImage.Level> {
    interface Filter {
        fun drawFilteredImage(sourceImage: Image, destImage: Image)

        object Mitchell : Filter {
            override fun drawFilteredImage(sourceImage: Image, destImage: Image) {
                val scaleX = sourceImage.width.toFloat() / destImage.width
                val scaleY = sourceImage.height.toFloat() / destImage.height

                repeat(destImage.height) { y ->
                    val centerY = (y + 0.5f) * scaleY - 0.5f
                    val iy = centerY.toInt()

                    repeat(destImage.width) { x ->
                        val centerX = (x + 0.5f) * scaleX - 0.5f
                        val ix = centerX.toInt()

                        var r = 0.0f
                        var g = 0.0f
                        var b = 0.0f
                        var a = 0.0f
                        var weightSum = 0.0f

                        for (py in iy - 1..iy + 2) {
                            val wy = mitchell(centerY - py)

                            for (px in ix - 1..ix + 2) {
                                val weight = wy * mitchell(centerX - px)

                                if (weight == 0.0f)
                                    continue

                                val color = sourceImage[px, py]

                                r += color.r * weight
                                g += color.g * weight
                                b += color.b * weight
                                a += color.a * weight
                                weightSum += weight
                            }
                        }

                        val scale = 1.0f / weightSum
                        destImage[x, y] = Color(r * scale, g * scale, b * scale, a * scale)
                    }
                }
            }

            private fun mitchell(x: Float): Float {
                val ax = kotlin.math.abs(x)

                if (ax >= 2.0f)
                    return 0.0f

                val b = 1.0f / 3.0f
                val c = 1.0f / 3.0f

                return if (ax < 1.0f)
                    ((12 - 9 * b - 6 * c) * ax * ax * ax + (-18 + 12 * b + 6 * c) * ax * ax + (6 - 2 * b)) / 6.0f
                else
                    ((-b - 6 * c) * ax * ax * ax + (6 * b + 30 * c) * ax * ax + (-12 * b - 48 * c) * ax + (8 * b + 24 * c)) / 6.0f
            }
        }

        class Kaiser(private val radius: Float = 3.0f, private val beta: Float = 6.0f) : Filter {
            private val normalization = besselI0(beta)

            override fun drawFilteredImage(sourceImage: Image, destImage: Image) {
                val temp = Image(destImage.width, sourceImage.height)
                filterHorizontal(sourceImage, temp)
                filterVertical(temp, destImage)
            }

            private fun filterHorizontal(source: Image, dest: Image) {
                val scale = source.width.toFloat() / dest.width

                repeat(dest.height) { y ->
                    repeat(dest.width) { x ->
                        val center = (x + 0.5f) * scale - 0.5f
                        val filterRadius = maxOf(radius, radius * scale)
                        val start = floor(center - filterRadius).toInt()
                        val end = ceil(center + filterRadius).toInt()

                        var r = 0.0f
                        var g = 0.0f
                        var b = 0.0f
                        var a = 0.0f
                        var weightSum = 0.0f

                        for (sx in start..end) {
                            val weight = kernel(center - sx, scale, filterRadius)

                            if (weight == 0.0f)
                                continue

                            val color = source[sx, y]

                            r += color.r * weight
                            g += color.g * weight
                            b += color.b * weight
                            a += color.a * weight
                            weightSum += weight
                        }

                        val invWeight = if (weightSum != 0.0f) 1.0f / weightSum else 1.0f
                        dest[x, y] = Color(r * invWeight, g * invWeight, b * invWeight, a * invWeight)
                    }
                }
            }

            private fun filterVertical(source: Image, dest: Image) {
                val scale = source.height.toFloat() / dest.height

                repeat(dest.height) { y ->
                    repeat(dest.width) { x ->
                        val center = (y + 0.5f) * scale - 0.5f
                        val filterRadius = maxOf(radius, radius * scale)
                        val start = floor(center - filterRadius).toInt()
                        val end = ceil(center + filterRadius).toInt()

                        var r = 0.0f
                        var g = 0.0f
                        var b = 0.0f
                        var a = 0.0f
                        var weightSum = 0.0f

                        for (sy in start..end) {
                            val weight = kernel(center - sy, scale, filterRadius)

                            if (weight == 0.0f)
                                continue

                            val color = source[x, sy]

                            r += color.r * weight
                            g += color.g * weight
                            b += color.b * weight
                            a += color.a * weight
                            weightSum += weight
                        }

                        val invWeight = if (weightSum != 0.0f) 1.0f / weightSum else 1.0f
                        dest[x, y] = Color(r * invWeight, g * invWeight, b * invWeight, a * invWeight)
                    }
                }
            }

            private fun kernel(x: Float, scale: Float, filterRadius: Float): Float {
                val distance = abs(x)

                if (distance >= filterRadius)
                    return 0.0f

                val sinc = sinc(x / scale)
                val position = distance / filterRadius
                val window = besselI0(beta * sqrt(max(0.0f, 1.0f - position * position))) / normalization

                return sinc * window
            }

            private fun sinc(x: Float): Float {
                if (abs(x) < 0.000001f)
                    return 1.0f

                val p = PI * x
                return sin(p) / p
            }

            private fun besselI0(x: Float): Float {
                var sum = 1.0f
                var term = 1.0f
                val half = x * 0.5f

                for (k in 1..20) {
                    term *= (half / k) * (half / k)
                    sum += term

                    if (term < sum * 1e-8f)
                        break
                }

                return sum
            }
        }
    }

    /**
     * A single mip level.
     *
     * @param image The image for this level.
     * @param width The width of this level.
     * @param height The height of this level.
     */
    inner class Level(image: Image, width: Int, height: Int) {
        val image = Image(width, height)

        init {
            if (image.width == width && image.height == height)
                this.image.setImage(image)
            else
                filter.drawFilteredImage(image, this.image)
        }
    }

    /**
     * The number of levels.
     */
    val numLevels get() = levels.size

    private val levels: Array<Level>

    init {
        var size = min(image.width, image.height)
        var count = 1

        while (size > minSize) {
            count++
            size /= 2
        }

        val levelsList = arrayListOf<Level>()

        var width = image.width
        var height = image.height
        var lastImage = image

        repeat(count) {
            val level = Level(lastImage, width, height)
            levelsList += level
            lastImage = level.image

            width /= 2
            height /= 2
        }

        levels = levelsList.toTypedArray()
    }

    /**
     * Gets an iterator over the levels.
     *
     * @return An iterator over the levels.
     */
    override fun iterator() = levels.iterator()

    /**
     * Gets the level at the given index.
     *
     * @param level The index of the level to get.
     *
     * @return The level at the given index.
     */
    operator fun get(level: Int) = levels[level]
}
