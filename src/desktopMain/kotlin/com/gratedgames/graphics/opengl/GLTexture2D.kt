package com.gratedgames.graphics.opengl

import com.gratedgames.graphics.DesktopStatistics
import com.gratedgames.graphics.gpu.Texture
import com.gratedgames.graphics.gpu.Texture2D
import com.gratedgames.memory.Memory
import com.gratedgames.utils.extensions.clamp
import org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

open class GLTexture2D(format: Format) : Texture2D(format) {
    var handle = glGenTextures()

    override val width: Int
        get() = internalWidth

    override val height: Int
        get() = internalHeight

    private var internalWidth = 0
    private var internalHeight = 0

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
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter)
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
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, sWrap)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, tWrap)
        }
    }

    override fun setAnisotropy(anisotropy: Float) {
        if (!GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
            return

        tempBind {
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY, anisotropy.clamp(0.0f, glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY)))
        }
    }

    override fun setLOD(minLOD: Float, maxLOD: Float, bias: Float) {
        tempBind {
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, minLOD)
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, maxLOD)
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, bias)
        }
    }

    override fun setSize(width: Int, height: Int) {
        tempBind {
            glTexImage2D(GL_TEXTURE_2D, 0, format.toInternalGLFormat(), width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        internalWidth = width
        internalHeight = height
    }

    override fun setImage(width: Int, height: Int, data: Memory, dataFormat: Format, offset: Int, level: Int) {
        tempBind {
            nglTexImage2D(GL_TEXTURE_2D, level, format.toInternalGLFormat(), width, height, 0, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address)
        }

        internalWidth = width
        internalHeight = height
    }

    override fun setSubImage(x: Int, y: Int, width: Int, height: Int, data: Memory, dataFormat: Format, offset: Int, level: Int) {
        tempBind {
            nglTexSubImage2D(GL_TEXTURE_2D, level, x, y, width, height, dataFormat.toGLFormat(), dataFormat.toGLType(), data.address + offset)
        }
    }

    override fun getImage(data: Memory, offset: Int, format: Format, level: Int) {
        tempBind {
            nglGetTexImage(GL_TEXTURE_2D, level, format.toGLFormat(), format.toGLType(), data.address + offset)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is GLTexture2D)
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

fun GLTexture2D.tempBind(block: () -> Unit) {
    val unit = GLManager.activeTextureUnit
    val previous = GLManager.boundTextures2D[unit]
    GLManager.bindTexture2D(handle, unit)
    block()
    GLManager.bindTexture2D(previous, unit)
}