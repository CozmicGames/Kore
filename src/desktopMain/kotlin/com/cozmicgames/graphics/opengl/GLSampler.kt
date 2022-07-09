package com.cozmicgames.graphics.opengl

import com.cozmicgames.graphics.gpu.Sampler
import com.cozmicgames.graphics.gpu.Texture

class GLSampler : Sampler {
    private val textures = arrayListOf<Texture>()

    override var minFilter = Texture.Filter.NEAREST
        set(value) {
            field = value
            setUpdated()
        }

    override var magFilter = Texture.Filter.NEAREST
        set(value) {
            field = value
            setUpdated()
        }

    override var mipFilter: Texture.Filter? = null
        set(value) {
            field = value
            setUpdated()
        }

    override var xWrap = Texture.Wrap.REPEAT
        set(value) {
            field = value
            setUpdated()
        }

    override var yWrap = Texture.Wrap.REPEAT
        set(value) {
            field = value
            setUpdated()
        }

    override var zWrap = Texture.Wrap.REPEAT
        set(value) {
            field = value
            setUpdated()
        }

    override var maxAnisotropy = 16.0f
        set(value) {
            field = value
            setUpdated()
        }

    override var minLOD = 0.0f
        set(value) {
            field = value
            setUpdated()
        }

    override var maxLOD = 1000.0f
        set(value) {
            field = value
            setUpdated()
        }

    override var lodBias = 0.0f
        set(value) {
            field = value
            setUpdated()
        }

    fun addTexture(texture: Texture) {
        textures += texture
    }

    fun removeTexture(texture: Texture) {
        textures += texture
    }

    private fun setUpdated() {
        textures.forEach {
            if (it is GLTexture2D)
                it.setSamplerUpdated()
            if (it is GLTextureCube)
                it.setSamplerUpdated()
            if (it is GLTexture3D)
                it.setSamplerUpdated()
        }
    }
}