package com.cozmicgames.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.Sampler
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

/**
 * An image is a 2D array of pixels.
 *
 * @param width Width of the image in pixels.
 * @param height Height of the image in pixels.
 * @param pixels Optional [PixelData] to initialize the image with. Defaults to creating a new [PixelData] instance.
 */
class Image(val width: Int, val height: Int, val pixels: PixelData = PixelData(width, height)) {
    /**
     * Gets the pixel index for the given x and y coordinates.
     *
     * @param x X coordinate of the pixel.
     * @param y Y coordinate of the pixel.
     *
     * @return The pixel index.
     */
    fun getPixelsIndex(x: Int, y: Int) = x + y * width

    /**
     * Gets the color of the pixel at the given x and y coordinates.
     * X and Y coordinates are clamped to the image size.
     *
     * @param x X coordinate of the pixel.
     * @param y Y coordinate of the pixel.
     *
     * @return The color of the pixel.
     */
    operator fun get(x: Int, y: Int): Color {
        val xx = x.clamp(0, width - 1)
        val yy = y.clamp(0, height - 1)
        return pixels[getPixelsIndex(xx, yy)]
    }

    /**
     * Sets the color of the pixel at the given x and y coordinates.
     * If the x and y coordinates are outside the image size, nothing happens.
     *
     * @param x X coordinate of the pixel.
     * @param y Y coordinate of the pixel.
     * @param color The color to set the pixel to.
     */
    operator fun set(x: Int, y: Int, color: Color) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return

        pixels[getPixelsIndex(x, y)] = color
    }

    /**
     * Fills the image with the given color.
     *
     * @param color The color to fill the image with.
     */
    fun fill(color: Color) = pixels.fill(color)

    /**
     * Flips the image vertically.
     */
    fun flipX() {
        repeat(height) { y ->
            repeat(width) { x ->
                val temp = this[x, y]
                this[x, y] = this[width - x, y]
                this[width - x, y] = temp
            }
        }
    }

    /**
     * Flips the image horizontally.
     */
    fun flipY() {
        repeat(width) { x ->
            repeat(height) { y ->
                val temp = this[x, y]
                this[x, y] = this[x, height - y]
                this[x, height - y] = temp
            }
        }
    }

    /**
     * Samples the image at the given u and v coordinates.
     * U and V coordinates are in the range 0 to 1.
     * The image is sampled using bilinear interpolation.
     *
     * @param u U coordinate of the pixel.
     * @param v V coordinate of the pixel.
     *
     * @return The sampled color.
     */
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

    /**
     * Draws the specified [color] to the image at the given x and y coordinates.
     * If the x and y coordinates are outside the image size, nothing happens.
     * Draws using alpha blending if the colors' alpha is not 1.
     *
     * @param x X coordinate of the pixel.
     * @param y Y coordinate of the pixel.
     * @param color The color to draw.
     */
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

    /**
     * Draws a line from the given start x and y coordinates to the given end x and y coordinates with the specified [color].
     * Draws using alpha blending if the colors' alpha is not 1.
     *
     * @param x0 X coordinate of the start point.
     * @param y0 Y coordinate of the start point.
     * @param x1 X coordinate of the end point.
     * @param y1 Y coordinate of the end point.
     * @param color The color to draw the line with.
     */
    fun drawLine(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) = forEachLinePoint(x0, y0, x1, y1) { x, y ->
        drawPixel(x, y, color)
    }

    /**
     * Draws a rectangle with the specified [color] from the given start x and y coordinates to the given end x and y coordinates.
     * Draws using alpha blending if the colors' alpha is not 1.
     *
     * @param x0 X coordinate of the start point.
     * @param y0 Y coordinate of the start point.
     * @param x1 X coordinate of the end point.
     * @param y1 Y coordinate of the end point.
     * @param color The color to draw the rectangle with.
     */
    fun drawRectangle(x0: Int, y0: Int, x1: Int, y1: Int, color: Color) {
        drawLine(x0, y0, x1 - 1, y0, color)
        drawLine(x1, y0, x1, y1 - 1, color)
        drawLine(x1, y1, x0 + 1, y1, color)
        drawLine(x0, y1, x0, y0 + 1, color)
    }

    /**
     * Draws a filled rectangle with the specified [color] from the given start x and y coordinates to the given end x and y coordinates.
     * Draws using alpha blending if the colors' alpha is not 1.
     *
     * @param x0 X coordinate of the start point.
     * @param y0 Y coordinate of the start point.
     * @param x1 X coordinate of the end point.
     * @param y1 Y coordinate of the end point.
     * @param color The color to draw the rectangle with.
     */
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

    /**
     * Draws the specified [image] to the image at the given x and y coordinates.
     * Draws using alpha blending if the colors' alpha is not 1.
     *
     * @param image The image to draw.
     * @param x X coordinate to draw the image at.
     * @param y Y coordinate to draw the image at.
     * @param width The width to draw the image with. Defaults to the image's width.
     * @param height The height to draw the image with. Defaults to the image's height.
     * @param srcX The x coordinate of the source image to draw from. Defaults to 0.
     * @param srcY The y coordinate of the source image to draw from. Defaults to 0.
     * @param srcWidth The width of the source image to draw to. Defaults to the image's width.
     * @param srcHeight The height of the source image to draw to. Defaults to the image's height.
     */
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

    /**
     * Draws the specified [image] to the image at the given x and y coordinates.
     * This does not use any blending and thus is much faster than [drawImage].
     *
     * @param image The image to draw.
     * @param x X coordinate to draw the image at.
     * @param y Y coordinate to draw the image at.
     * @param width The width to draw the image with. Defaults to the image's width.
     * @param height The height to draw the image with. Defaults to the image's height.
     * @param srcX The x coordinate of the source image to draw from. Defaults to 0.
     * @param srcY The y coordinate of the source image to draw from. Defaults to 0.
     * @param srcWidth The width of the source image to draw to. Defaults to the image's width.
     * @param srcHeight The height of the source image to draw to. Defaults to the image's height.
     */
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

    /**
     * Copies this image.
     */
    fun copy(): Image {
        return Image(width, height, pixels.copy())
    }
}

/**
 * Splits the image along the x-axis [count] times.
 *
 * @param count The number of times to split the image.
 *
 * @return An array of the split images.
 */
fun Image.splitAlongX(count: Int) = Array(count) {
    val image = Image(width / count, height)
    image.setImage(this, 0, 0, image.width, image.height, width / count, height, width / count, height)
    image
}

/**
 * Splits the image along the y-axis [count] times.
 *
 * @param count The number of times to split the image.
 *
 * @return An array of the split images.
 */
fun Image.splitAlongY(count: Int) = Array(count) {
    val image = Image(width, height / count)
    image.setImage(this, 0, 0, image.width, image.height, 0, height / count, width, height / count)
    image
}

/**
 * Splits the image into a grid of [columns] x [rows] images.
 *
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 *
 * @return An [Array2D] of the split images.
 */
fun Image.split(columns: Int, rows: Int): Array2D<Image> {
    val imagesX = splitAlongX(columns)
    val images = Array(imagesX.size) {
        imagesX[it].splitAlongY(rows)
    }
    return Array2D(columns) { x, y -> images[x][y] }
}

/**
 * Creates a [Texture2D] from the image.
 * @see [Graphics.createTexture2D]
 *
 * @param format The format of the texture. Defaults to [Texture.Format.RGBA8_UNORM].
 * @param block The block to execute to configure the texture.
 *
 * @return The created texture.
 */
fun Image.toTexture2D(sampler: Sampler, format: Texture.Format = Texture.Format.RGBA8_UNORM, block: Texture2D.() -> Unit = {}) = Kore.graphics.createTexture2D(format, sampler, block).also {
    it.setImage(this)
}

/**
 * Reads back the texture data into an [Image].
 *
 * @return The image.
 */
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

/**
 * Sets the image data to the texture at the specified [level].
 *
 * @param image The image to set the texture data to.
 * @param level The mipmap level to set the image data to.
 */
fun Texture2D.setImage(image: Image, level: Int = 0) {
    Memory.of(*image.pixels.toByteArray()).use {
        setImage(image.width, image.height, it, level = level)
    }
}
