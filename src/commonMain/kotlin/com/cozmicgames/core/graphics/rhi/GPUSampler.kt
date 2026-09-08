package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.utils.Disposable

interface GPUSampler : Disposable {
    enum class Filter {
        NEAREST,
        LINEAR
    }

    enum class Wrap {
        CLAMP,
        REPEAT,
        MIRROR
    }

    interface Builder {
        var minFilter: Filter
        var magFilter: Filter
        var mipFilter: Filter?

        var xWrap: Wrap
        var yWrap: Wrap
        var zWrap: Wrap

        var maxAnisotropy: Float

        var minLOD: Float
        var maxLOD: Float
        var lodBias: Float
    }
}