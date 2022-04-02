package com.gratedgames.graphics.opengl

import com.gratedgames.graphics.DesktopStatistics
import com.gratedgames.graphics.gpu.CubemapSides
import com.gratedgames.graphics.gpu.Texture
import com.gratedgames.graphics.gpu.TextureCube
import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.clamp
import com.gratedgames.utils.maths.Direction
import org.lwjgl.opengl.ARBTextureFilterAnisotropic
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*

open class GLTextureCube(format: Format) : Comparable<Texture>, TextureCube(format) {
    val handle = glGenTextures()

    init {
        DesktopStatistics.numTextures++
    }

    override fun setFilter(min: Filter, mag: Filter, mip: Filter?) {
        val minFilter = when (mip) {
            null -> when (min) {
                Filter.NEAREST -> GL_NEAREST
                Filter.LINEAR -> GL_LINEAR
            }
            Filter.NEAREST -> when (min) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_NEAREST
                Filter.LINEAR -> GL_LINEAR_MIPMAP_NEAREST
            }
            Filter.LINEAR -> when (min) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_LINEAR
                Filter.LINEAR -> GL_LINEAR_MIPMAP_LINEAR
            }
        }

        val magFilter = when (mip) {
            null -> when (mag) {
                Filter.NEAREST -> GL_NEAREST
                Filter.LINEAR -> GL_LINEAR
            }
            Filter.NEAREST -> when (mag) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_NEAREST
                Filter.LINEAR -> GL_LINEAR_MIPMAP_NEAREST
            }
            Filter.LINEAR -> when (mag) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_LINEAR
                Filter.LINEAR -> GL_LINEAR_MIPMAP_LINEAR
            }
        }

        tempBind {
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, minFilter)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, magFilter)
        }
    }

    override fun setWrap(s: Wrap, t: Wrap) {
        val sWrap = when (s) {
            Wrap.CLAMP -> GL_CLAMP_TO_EDGE
            Wrap.MIRROR -> GL_MIRRORED_REPEAT
            Wrap.REPEAT -> GL_REPEAT
        }

        val tWrap = when (s) {
            Wrap.CLAMP -> GL_CLAMP_TO_EDGE
            Wrap.MIRROR -> GL_MIRRORED_REPEAT
            Wrap.REPEAT -> GL_REPEAT
        }

        tempBind {
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, sWrap)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, tWrap)
        }
    }

    override fun setAnisotropy(anisotropy: Float) {
        if (!GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
            return

        tempBind {
            glTexParameterf(GL_TEXTURE_2D, ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY, anisotropy.clamp(0.0f, glGetFloat(ARBTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY)))
        }
    }

    override fun setLOD(minLOD: Float, maxLOD: Float, bias: Float) {
        tempBind {
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, minLOD)
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, maxLOD)
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, bias)

        }
    }

    override fun setImages(width: Int, height: Int, format: Format, block: CubemapSides.() -> Unit) {
        val sides = CubemapSides()
        block(sides)
        sides.negativeX?.let { setDirectionImage(Direction.NEGATIVE_X, width, height, it, format, sides.offsetNegativeX) }
        sides.positiveX?.let { setDirectionImage(Direction.POSITIVE_X, width, height, it, format, sides.offsetPositiveX) }
        sides.negativeY?.let { setDirectionImage(Direction.NEGATIVE_Y, width, height, it, format, sides.offsetNegativeY) }
        sides.positiveY?.let { setDirectionImage(Direction.POSITIVE_Y, width, height, it, format, sides.offsetPositiveY) }
        sides.negativeZ?.let { setDirectionImage(Direction.NEGATIVE_Z, width, height, it, format, sides.offsetNegativeZ) }
        sides.positiveZ?.let { setDirectionImage(Direction.POSITIVE_Z, width, height, it, format, sides.offsetPositiveZ) }
    }

    override fun setDirectionImage(direction: Direction, width: Int, height: Int, data: Memory, format: Format, offset: Int) {
        val glDirection = when (direction) {
            Direction.NEGATIVE_X -> GL_TEXTURE_CUBE_MAP_NEGATIVE_X
            Direction.POSITIVE_X -> GL_TEXTURE_CUBE_MAP_POSITIVE_X
            Direction.NEGATIVE_Y -> GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
            Direction.POSITIVE_Y -> GL_TEXTURE_CUBE_MAP_POSITIVE_Y
            Direction.NEGATIVE_Z -> GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
            Direction.POSITIVE_Z -> GL_TEXTURE_CUBE_MAP_POSITIVE_Z
            else -> throw Exception("Unreachable")
        }

        tempBind {
            glTexImage2D(glDirection, 0, this.format.toInternalGLFormat(), width, height, 0, format.toGLFormat(), format.toGLType(), data.address + offset)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is GLTextureCube)
            return handle == other.handle
        return super.equals(other)
    }

    override fun compareTo(other: Texture): Int {
        return when (other) {
            is GLTexture2D -> other.handle - handle
            is GLTexture3D -> other.handle - handle
            is GLTextureCube -> other.handle - handle
            else -> 0
        }
    }

    override fun dispose() {
        glDeleteTextures(handle)

        DesktopStatistics.numTextures--
    }
}

fun GLTextureCube.tempBind(block: () -> Unit) {
    val unit = GLManager.activeTextureUnit
    val previous = GLManager.boundTexturesCube[unit]
    GLManager.bindTextureCube(handle, unit)
    block()
    GLManager.bindTextureCube(previous, unit)
}