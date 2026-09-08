package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUSampler
import org.lwjgl.opengl.GL46C.*

class GLSampler(private val device: GLDevice, builder: Builder) : GPUSampler {
    class Builder : GPUSampler.Builder {
        override var minFilter = GPUSampler.Filter.LINEAR
        override var magFilter = GPUSampler.Filter.LINEAR
        override var mipFilter: GPUSampler.Filter? = GPUSampler.Filter.LINEAR

        override var xWrap = GPUSampler.Wrap.REPEAT
        override var yWrap = GPUSampler.Wrap.REPEAT
        override var zWrap = GPUSampler.Wrap.REPEAT

        override var maxAnisotropy = 1.0f

        override var minLOD = -1000.0f
        override var maxLOD = 1000.0f
        override var lodBias = 0.0f
    }

    internal val handle get() = handleInternal
    private var handleInternal = glCreateSamplers()

    init {
        glSamplerParameteri(handleInternal, GL_TEXTURE_MIN_FILTER, when (builder.mipFilter) {
            null -> when (builder.minFilter) {
                GPUSampler.Filter.NEAREST -> GL_NEAREST
                GPUSampler.Filter.LINEAR -> GL_LINEAR
            }
            GPUSampler.Filter.NEAREST -> when (builder.minFilter) {
                GPUSampler.Filter.NEAREST -> GL_NEAREST_MIPMAP_NEAREST
                GPUSampler.Filter.LINEAR -> GL_LINEAR_MIPMAP_NEAREST
            }
            GPUSampler.Filter.LINEAR -> when (builder.minFilter) {
                GPUSampler.Filter.NEAREST -> GL_NEAREST_MIPMAP_LINEAR
                GPUSampler.Filter.LINEAR -> GL_LINEAR_MIPMAP_LINEAR
            }
        })

        glSamplerParameteri(handleInternal, GL_TEXTURE_MAG_FILTER, when (builder.magFilter) {
            GPUSampler.Filter.NEAREST -> GL_NEAREST
            GPUSampler.Filter.LINEAR -> GL_LINEAR
        })

        glSamplerParameteri(handleInternal, GL_TEXTURE_WRAP_S, builder.xWrap.toGL())
        glSamplerParameteri(handleInternal, GL_TEXTURE_WRAP_T, builder.yWrap.toGL())
        glSamplerParameteri(handleInternal, GL_TEXTURE_WRAP_R, builder.zWrap.toGL())

        glSamplerParameterf(handleInternal, GL_TEXTURE_MAX_ANISOTROPY, builder.maxAnisotropy)
        glSamplerParameterf(handleInternal, GL_TEXTURE_MIN_LOD, builder.minLOD)
        glSamplerParameterf(handleInternal, GL_TEXTURE_MAX_LOD, builder.maxLOD)
        glSamplerParameterf(handleInternal, GL_TEXTURE_LOD_BIAS, builder.lodBias)
    }

    override fun dispose() {
        if (handleInternal != 0) {
            glDeleteSamplers(handleInternal)
            handleInternal = 0
        }
    }

    private fun GPUSampler.Wrap.toGL(): Int = when (this) {
        GPUSampler.Wrap.CLAMP -> GL_CLAMP_TO_EDGE
        GPUSampler.Wrap.REPEAT -> GL_REPEAT
        GPUSampler.Wrap.MIRROR -> GL_MIRRORED_REPEAT
    }
}