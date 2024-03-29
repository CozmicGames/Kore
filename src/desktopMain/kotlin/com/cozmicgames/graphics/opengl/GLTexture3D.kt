package com.cozmicgames.graphics.opengl

import com.cozmicgames.graphics.DesktopStatistics
import com.cozmicgames.graphics.gpu.Sampler
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Texture3D
import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.extensions.clamp
import org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

open class GLTexture3D(format: Format, private var sampler: Sampler) : Comparable<Texture>, Texture3D(format) {
    var handle = glGenTextures()

    override val width: Int
        get() = internalWidth

    override val height: Int
        get() = internalHeight

    override val depth: Int
        get() = internalDepth

    private var internalWidth = 0
    private var internalHeight = 0
    private var internalDepth = 0

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
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, minFilter)
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, magFilter)
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, sWrap)
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, tWrap)
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, rWrap)
            glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MIN_LOD, sampler.minLOD)
            glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAX_LOD, sampler.maxLOD)
            glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_LOD_BIAS, sampler.lodBias)

            if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
                glTexParameterf(GL_TEXTURE_3D, GL_TEXTURE_MAX_ANISOTROPY, sampler.maxAnisotropy.clamp(0.0f, glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY)))
        }
    }

    override fun setSize(width: Int, height: Int, depth: Int) {
        tempBind {
            GLManager.checkErrors {
                glTexImage3D(GL_TEXTURE_3D, 0, format.toInternalGLFormat(), width, height, depth, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
            }
        }

        internalWidth = width
        internalHeight = height
    }

    override fun setImage(width: Int, height: Int, depth: Int, data: Memory, dataFormat: Format, offset: Int, level: Int) {
        tempBind {
            GLManager.checkErrors {
                nglTexImage3D(GL_TEXTURE_3D, level, format.toInternalGLFormat(), width, height, depth, 0, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address)
            }
        }

        internalWidth = width
        internalHeight = height
    }

    override fun setSubImage(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, data: Memory, dataFormat: Format, offset: Int, level: Int) {
        tempBind {
            GLManager.checkErrors {
                nglTexSubImage3D(GL_TEXTURE_3D, level, x, y, z, width, height, depth, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address + offset)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is GLTexture3D)
            return handle == other.handle
        return super.equals(other)
    }

    override fun hashCode() = handle

    override fun compareTo(other: Texture): Int {
        return when (other) {
            is GLTexture2D -> other.handle - handle
            is GLTexture3D -> other.handle - handle
            is GLTextureCube -> other.handle - handle
            else -> 0
        }
    }

    override fun dispose() {
        GLManager.deleteTexture3D(handle)

        DesktopStatistics.numTextures--
    }
}

fun GLTexture3D.tempBind(block: () -> Unit) {
    val unit = GLManager.activeTextureUnit
    val previous = GLManager.boundTextures3D[unit]
    GLManager.bindTexture3D(handle, unit)
    block()
    GLManager.bindTexture3D(previous, unit)
}