package com.cozmicgames.core.graphics.rhi

import com.cozmicgames.core.utils.Disposable
import com.cozmicgames.core.utils.maths.Vector3i

sealed interface GPUPipeline: Disposable {
    enum class Type {
        GRAPHICS,
        COMPUTE
    }

    val id: Int
    val type: Type
    val shader: GPUShader
}

abstract class GPUGraphicsPipeline : GPUPipeline {
    class ColorMask(var r: Boolean, var g: Boolean, var b: Boolean, var a: Boolean) {
        companion object {
            operator fun invoke(block: ColorMask.() -> Unit) = ColorMask(true, true, true, true).also(block)
        }
    }

    class DepthState(var func: Func, var min: Float, var max: Float) {
        companion object {
            operator fun invoke(block: DepthState.() -> Unit) = DepthState(Func.ALWAYS, 0.0f, 1.0f).also(block)
        }

        enum class Func {
            ALWAYS,
            LESS,
            LESS_OR_EQUAL,
            EQUAL,
            GREATER_OR_EQUAL,
            GREATER,
            NOT_EQUAL,
            NEVER
        }
    }

    class StencilState(var func: Func, var ref: Int, var mask: Int, var stencilFail: Operation, var depthFail: Operation, var depthPass: Operation) {
        companion object {
            operator fun invoke(block: StencilState.() -> Unit) = StencilState(Func.ALWAYS, 0, 0xFFFFFFFF.toInt(), Operation.KEEP, Operation.KEEP, Operation.KEEP).also(block)
        }

        enum class Func {
            ALWAYS,
            LESS,
            LEQUAL,
            EQUAL,
            GEQUAL,
            GREATER,
            NOT_EQUAL,
            NEVER
        }

        enum class Operation {
            KEEP,
            ZERO,
            INCREMENT,
            DECREMENT,
            INVERT,
            REPLACE
        }
    }

    class BlendState(var equation: Equation, var srcFactor: Factor, var destFactor: Factor) {
        companion object {
            operator fun invoke(block: BlendState.() -> Unit) = BlendState(Equation.ADD, Factor.SOURCE_ALPHA, Factor.ONE_MINUS_SOURCE_ALPHA).also(block)
        }

        enum class Factor {
            ZERO,
            ONE,
            SOURCE_COLOR,
            ONE_MINUS_SOURCE_COLOR,
            DEST_COLOR,
            ONE_MINUS_DEST_COLOR,
            SOURCE_ALPHA,
            ONE_MINUS_SOURCE_ALPHA,
            DEST_ALPHA,
            ONE_MINUS_DEST_ALPHA
        }

        enum class Equation {
            ADD,
            SUBTRACT,
            REVERSE_SUBTRACT,
            MIN,
            MAX
        }
    }

    class CullState(var front: Boolean, var back: Boolean) {
        companion object {
            operator fun invoke(block: CullState.() -> Unit) = CullState(false, false).also(block)
        }
    }

    interface Builder {
        fun setShader(shader: GPUShader)
        fun setColorMask(mask: ColorMask.() -> Unit)
        fun setDepthMask(mask: Boolean)
        fun setStencilMask(mask: Int)
        fun setDepthState(state: DepthState.() -> Unit)
        fun setStencilState(state: StencilState.() -> Unit)
        fun setBlendState(state: BlendState.() -> Unit)
        fun setCullState(state: CullState.() -> Unit)
    }

    final override val type: GPUPipeline.Type = GPUPipeline.Type.GRAPHICS
}

abstract class GPUComputePipeline : GPUPipeline {
    interface Builder {
        fun setShader(shader: GPUShader)
    }

    final override val type: GPUPipeline.Type = GPUPipeline.Type.COMPUTE

    abstract val workgroupSizes: Vector3i
}