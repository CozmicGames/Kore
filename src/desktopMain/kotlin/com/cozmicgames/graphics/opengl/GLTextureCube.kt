package com.cozmicgames.graphics.opengl

import com.cozmicgames.graphics.DesktopStatistics
import com.cozmicgames.graphics.gpu.CubemapSides
import com.cozmicgames.graphics.gpu.Sampler
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.TextureCube
import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.extensions.clamp
import com.cozmicgames.utils.maths.Direction
import org.lwjgl.opengl.ARBTextureFilterAnisotropic
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*

open class GLTextureCube(format: Format, private var sampler: Sampler) : Comparable<Texture>, TextureCube(format) {
    val handle = glGenTextures()

    private var isSamplerUpdated = true

    init {
        DesktopStatistics.numTextures++
        (sampler as GLSampler).addTexture(this)
    }

    override fun setSampler(sampler: Sampler) {
        (this.sampler as? GLSampler)?.removeTexture(this)
        this.sampler = sampler
        (sampler as GLSampler).addTexture(this)
        setSamplerUpdated()
    }

    fun setSamplerUpdated() {
        isSamplerUpdated = true
    }

    fun updateSamplerState() {
        if (!isSamplerUpdated)
            return

        isSamplerUpdated = false

        val minFilter = when (sampler.mipFilter) {
            null -> when (sampler.minFilter) {
                Filter.NEAREST -> GL_NEAREST
                Filter.LINEAR -> GL_LINEAR
            }
            Filter.NEAREST -> when (sampler.minFilter) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_NEAREST
                Filter.LINEAR -> GL_LINEAR_MIPMAP_NEAREST
            }
            Filter.LINEAR -> when (sampler.minFilter) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_LINEAR
                Filter.LINEAR -> GL_LINEAR_MIPMAP_LINEAR
            }
        }

        val magFilter = when (sampler.mipFilter) {
            null -> when (sampler.magFilter) {
                Filter.NEAREST -> GL_NEAREST
                Filter.LINEAR -> GL_LINEAR
            }
            Filter.NEAREST -> when (sampler.magFilter) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_NEAREST
                Filter.LINEAR -> GL_LINEAR_MIPMAP_NEAREST
            }
            Filter.LINEAR -> when (sampler.magFilter) {
                Filter.NEAREST -> GL_NEAREST_MIPMAP_LINEAR
                Filter.LINEAR -> GL_LINEAR_MIPMAP_LINEAR
            }
        }

        val sWrap = when (sampler.xWrap) {
            Wrap.CLAMP -> GL_CLAMP_TO_EDGE
            Wrap.MIRROR -> GL_MIRRORED_REPEAT
            Wrap.REPEAT -> GL_REPEAT
        }

        val tWrap = when (sampler.yWrap) {
            Wrap.CLAMP -> GL_CLAMP_TO_EDGE
            Wrap.MIRROR -> GL_MIRRORED_REPEAT
            Wrap.REPEAT -> GL_REPEAT
        }

        val rWrap = when (sampler.zWrap) {
            Wrap.CLAMP -> GL_CLAMP_TO_EDGE
            Wrap.MIRROR -> GL_MIRRORED_REPEAT
            Wrap.REPEAT -> GL_REPEAT
        }

        tempBind {
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, minFilter)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, magFilter)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, sWrap)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, tWrap)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, rWrap)
            glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_LOD, sampler.minLOD)
            glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LOD, sampler.maxLOD)
            glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, sampler.lodBias)

            if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
                glTexParameterf(GL_TEXTURE_CUBE_MAP, ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY, sampler.maxAnisotropy.clamp(0.0f, glGetFloat(ARBTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY)))
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
        GLManager.deleteTextureCube(handle)

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