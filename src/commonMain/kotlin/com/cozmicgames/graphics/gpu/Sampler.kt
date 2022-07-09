package com.cozmicgames.graphics.gpu

import com.cozmicgames.utils.Disposable

interface Sampler : Disposable {
    var minFilter: Texture.Filter
    var magFilter: Texture.Filter
    var mipFilter: Texture.Filter?

    var xWrap: Texture.Wrap
    var yWrap: Texture.Wrap
    var zWrap: Texture.Wrap

    var maxAnisotropy: Float

    var minLOD: Float
    var maxLOD: Float
    var lodBias: Float
}