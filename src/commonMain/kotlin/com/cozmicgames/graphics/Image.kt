package com.cozmicgames.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.memory.Memory
import com.cozmicgames.memory.of
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.collections.Array2D
import com.cozmicgames.utils.extensions.clamp
import com.cozmicgames.utils.maths.fract
import com.cozmicgames.utils.maths.mix
import com.cozmicgames.utils.use
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Texture2D
import com.cozmicgames.utils.maths.forEachLinePoint

class Image(val width: Int, val height: Int, val pixels: PixelData = PixelData(width, height)) {
    fun getPixelsIndex(x: Int, y: Int) = x + y * width

    operator fun get(x: Int, y: Int): Color {
        val xx = x.clamp(0, width - 1)
        val yy = y.clamp(0, height - 1)
        return pixels[getPixelsIndex(xx, yy)]
    }

    operator fun set(x: Int, y: Int, color: Color) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return

        pixels[getPixelsIndex(x, y)] = color
    }

    fun fill(color: Color) = pixels.fill(color)

    fun flipX() {
        repeat(height) { y ->
            repeat(width) { x ->
                val temp = this[x, y]
                this[x, y] = this[width - x, y]
                this[width - x, y] = temp
            }
        }
    }

    fun flipY() {
        repeat(width) { x ->
            repeat(height) { y ->
                val temp = this[x, y]
                this[x, y] = this[x, height - y]
                this[x, height - y] = temp
            }
        }
    }

    fun sample(u: Float, v: Float): Color {
        val x = u * width
        val y = v * height
        val ix = x.toInt()
        val iy = y.toInt()

        val c00 = this[ix, iy]
        val c01 = this[ix, iy + 1]
        val c10 = this[ix + 1, iy]
        val c11 = this[ix + 1, iy + 1]

        val xAmount = fract(x)
        val yAmount = fract(y)

        val r0 = mix(c00.r, c10.r, xAmount)
        val g0 = mix(c00.g, c10.g, xAmount)
        val b0 = mix(c00.b, c10.b, xAmount)
        val a0 = mix(c00.a, c10.a, xAmount)

        val r1 = mix(c01.r, c11.r, xAmount)
        val g1 = mix(c01.g, c11.g, xAmount)
        val b1 = mix(c01.b, c11.b, xAmount)
        val a1 = mix(c01.a, c11.a, xAmount)

        val r = mix(r0, r1, yAmount)
        val g = mix(g0, g1, yAmount)
        val b = mix(b0, b1, yAmount)
        val a = mix(a0, a1, yAmount)

        return Color(r, g, b, a)
    }

    fun drawPixel(x: Int, y: Int, color: Color) {
        if (color.a == 1.0f)
            this[x, y] = color
        else {
            val destColor = this[x, y]

            val oneMinusSrcAlpha = 1.0f - color.a

            destColor.r = color.r * color.a + destColor.r * oneMinusSrcAlpha
            destColor.g = color.g * color.a + destColor.g * oneMinusSrcAlpha
            destColor.b = color.b * color.a + destColor.b * oneMinusSrcAlpha
            destColor.a = color.a * color.a + destColor.a * oneMinusSrcAlpha

            this[x, y] = destColor
        }
    }

    fun drawLine(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) = forEachLinePoint(x0, y0, x1, y1) { x, y ->
        drawPixel(x, y, color)
    }

    fun drawRectangle(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) {
        drawLine(x0, y0, x1 - 1, y0, color)
        drawLine(x1, y0, x1, y1 - 1, color)
        drawLine(x1, y1, x0 + 1, y1, color)
        drawLine(x0, y1, x0, y0 + 1, color)
    }

    fun drawRectangleFilled(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) {
        var ix = x0
        while (ix < x1) {
            if (ix >= 0) {
                if (ix >= width)
                    break
                var iy = y0
                while (iy < y1) {
                    if (iy >= 0) {
                        if (iy >= height)
                            break

                        drawPixel(ix, iy, color)
                    }
                    iy++
                }
            }
            ix++
        }
    }

    fun drawImage(image: Image, x: Int, y: Int, width: Int = image.width, height: Int = image.height, srcX: Int = 0, srcY: Int = 0, srcWidth: Int = image.width, srcHeight: Int = image.height) {
        val srcU0 = srcX / (image.width - 1.0f)
        val srcV0 = srcY / (image.height - 1.0f)
        val srcU1 = (srcX + srcWidth - 1.0f) / (image.width - 1.0f)
        val srcV1 = (srcY + srcHeight - 1.0f) / (image.height - 1.0f)

        val uRange = srcU1 - srcU0
        val vRange = srcV1 - srcV0

        val x1 = x + width
        val y1 = y + height

        val invWidth = 1.0f / width
        val invHeight = 1.0f / height

        for (xx in x until x1) {
            if (xx < 0 || xx >= this.width)
                continue

            val u = srcU0 + uRange * (xx - x) * invWidth

            for (yy in y until y1) {
                if (yy < 0 || yy >= this.height)
                    continue

                val v = srcV0 + vRange * (yy - y) * invHeight

                drawPixel(xx, yy, image.sample(u, v))
            }
        }
    }

    fun setImage(image: Image, x: Int, y: Int, width: Int = image.width, height: Int = image.height, srcX: Int = 0, srcY: Int = 0, srcWidth: Int = image.width, srcHeight: Int = image.height) {
        val srcU0 = srcX / (image.width - 1.0f)
        val srcV0 = srcY / (image.height - 1.0f)
        val srcU1 = (srcX + srcWidth - 1.0f) / (image.width - 1.0f)
        val srcV1 = (srcY + srcHeight - 1.0f) / (image.height - 1.0f)

        val uRange = srcU1 - srcU0
        val vRange = srcV1 - srcV0

        val x1 = x + width
        val y1 = y + height

        val invWidth = 1.0f / width
        val invHeight = 1.0f / height

        for (xx in x until x1) {
            if (xx < 0 || xx >= this.width)
                continue

            val u = srcU0 + uRange * (xx - x) * invWidth

            for (yy in y until y1) {
                if (yy < 0 || yy >= this.height)
                    continue

                val v = srcV0 + vRange * (yy - y) * invHeight

                this[xx, yy] = image.sample(u, v)
            }
        }
    }
}

fun Image.splitAlongX(count: Int) = Array(count) {
    val image = Image(width / count, height)
    image.setImage(this, 0, 0, image.width, image.height, width / count, height, width / count, height)
    image
}

fun Image.splitAlongY(count: Int) = Array(count) {
    val image = Image(width, height / count)
    image.setImage(this, 0, 0, image.width, image.height, 0, height / count, width, height / count)
    image
}

fun Image.split(columns: Int, rows: Int): Array2D<Image> {
    val imagesX = splitAlongX(columns)
    val images = Array(imagesX.size) {
        imagesX[it].splitAlongY(rows)
    }
    return Array2D(columns) { x, y -> images[x][y] }
}

fun Image.toTexture2D(format: Texture.Format = Texture.Format.RGBA8_UNORM, block: Texture2D.() -> Unit = {}) = Kore.graphics.createTexture2D(format, block).also {
    it.setImage(this)
}

fun Texture2D.toImage() = Image(width, height).also { image ->
    Memory(width * height * format.size).use {
        getImage(it, format = Texture.Format.RGBA8_UNORM)
        for (i in image.pixels.data.indices) {
            val r = it.getByte(i * 4) / 0xFF.toFloat()
            val g = it.getByte(i * 4 + 1) / 0xFF.toFloat()
            val b = it.getByte(i * 4 + 2) / 0xFF.toFloat()
            val a = it.getByte(i * 4 + 3) / 0xFF.toFloat()

            image.pixels.data[i].set(r, g, b, a)
        }
    }
}

fun Texture2D.setImage(image: Image, level: Int = 0) {
    Memory.of(*image.pixels.toByteArray()).use {
        setImage(image.width, image.height, it, level = level)
    }
}

fun Texture2D.setSubImage(image: Image, x: Int, y: Int, srcX: Int = 0, srcY: Int = 0, width: Int = image.width, height: Int = image.height) {
    Memory(width * height * 4).use {
        val x1 = x + width
        val y1 = y + height

        var ix = x
        while (ix < x1) {
            if (ix >= 0) {
                if (ix >= this.width)
                    break
                var iy = y
                while (iy < y1) {
                    if (iy >= 0) {
                        if (iy >= this.height)
                            break

                        val color = image[ix - x + srcX, iy - y + srcY]
                        it.setInt(((ix - x) + (iy - y) * width) * Memory.SIZEOF_INT, color.bits)
                    }
                    iy++
                }
            }
            ix++
        }
        setImage(width, height, it)
    }
}
