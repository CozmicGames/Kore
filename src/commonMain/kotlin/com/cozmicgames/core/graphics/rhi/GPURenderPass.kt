package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.utils.Color
import com.cozmicgames.core.utils.Disposable

interface GPURenderPass : Disposable {
    interface Builder {
        fun addColorAttachment(texture: GPUTexture2D, level: Int = 0, layer: Int = 0, loadOp: LoadOp = LoadOp.DEFAULT, storeOp: StoreOp = StoreOp.DEFAULT, clearColor: Color = Color.CLEAR)
        fun setDepthAttachment(texture: GPUTexture2D, level: Int = 0, layer: Int = 0, loadOp: LoadOp = LoadOp.DEFAULT, storeOp: StoreOp = StoreOp.DEFAULT, clearDepth: Float = 1.0f)
        fun setStencilAttachment(texture: GPUTexture2D, level: Int = 0, layer: Int = 0, loadOp: LoadOp = LoadOp.DEFAULT, storeOp: StoreOp = StoreOp.DEFAULT, clearStencil: Int = 0)
    }

    enum class LoadOp {
        LOAD,
        CLEAR,
        DONT_CARE;

        companion object {
            val DEFAULT = LOAD
        }
    }

    enum class StoreOp {
        STORE,
        DONT_CARE;

        companion object {
            val DEFAULT = STORE
        }
    }
}
