package com.gratedgames.graphics.gpu

import com.gratedgames.utils.Disposable
import com.gratedgames.utils.maths.Vector3i
import com.gratedgames.graphics.gpu.pipeline.ProgramSource

abstract class Pipeline : Disposable {
    enum class Type {
        GRAPHICS,
        COMPUTE
    }

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

    abstract val id: Int

    abstract val type: Type

    var colorMask: ColorMask? = null
    var depthMask: Boolean? = null
    var stencilMask: Int? = null
    var depthState: DepthState? = null
    var stencilState: StencilState? = null
    var blendState: BlendState? = null
    var cullState: CullState? = null
    var vertexLayout: VertexLayout? = null
    var programSource: ProgramSource? = null

    abstract val uniforms: Iterable<String>

    abstract val workgroupSizes: Vector3i

    abstract fun update()

    abstract fun <T : Any> getUniform(name: String): Uniform<T>?
}

inline fun colorMask(noinline block: Pipeline.ColorMask.() -> Unit) = Pipeline.ColorMask(block)

inline fun depthState(noinline block: Pipeline.DepthState.() -> Unit) = Pipeline.DepthState(block)

inline fun stencilState(noinline block: Pipeline.StencilState.() -> Unit) = Pipeline.StencilState(block)

inline fun blendState(noinline block: Pipeline.BlendState.() -> Unit) = Pipeline.BlendState(block)

inline fun cullState(noinline block: Pipeline.CullState.() -> Unit) = Pipeline.CullState(block)

fun backFaceCulling() = cullState {
    back = true
}

fun frontFaceCulling() = cullState {
    front = true
}

fun alphaBlending() = blendState {
    equation = Pipeline.BlendState.Equation.ADD
    srcFactor = Pipeline.BlendState.Factor.SOURCE_ALPHA
    destFactor = Pipeline.BlendState.Factor.ONE_MINUS_SOURCE_ALPHA
}

fun depthLessOrEqual() = depthState {
    func = Pipeline.DepthState.Func.LESS_OR_EQUAL
}

fun depthGreaterOrEqual() = depthState {
    func = Pipeline.DepthState.Func.GREATER_OR_EQUAL
}

fun Pipeline.getFloatUniform(name: String): FloatUniform? {
    val base = getUniform<Float>(name) ?: return null
    return FloatUniform(base)
}

fun Pipeline.getIntUniform(name: String): IntUniform? {
    val base = getUniform<Int>(name) ?: return null
    return IntUniform(base)
}

fun Pipeline.getMatrixUniform(name: String): MatrixUniform? {
    val base = getUniform<Float>(name) ?: return null
    return MatrixUniform(base)
}

fun Pipeline.getBooleanUniform(name: String): BooleanUniform? {
    val base = getUniform<Float>(name) ?: return null
    return BooleanUniform(base)
}

fun Pipeline.getTexture2DUniform(name: String): TextureUniform<Texture2D>? {
    val base = getUniform<Texture2D>(name) ?: return null
    return TextureUniform(base)
}

fun Pipeline.getTexture3DUniform(name: String): TextureUniform<Texture3D>? {
    val base = getUniform<Texture3D>(name) ?: return null
    return TextureUniform(base)
}

fun Pipeline.getTextureCubeUniform(name: String): TextureUniform<TextureCube>? {
    val base = getUniform<TextureCube>(name) ?: return null
    return TextureUniform(base)
}

fun Pipeline.getImage2DUniform(name: String): Image2DUniform? {
    return getUniform<TextureImage2D>(name) as? Image2DUniform
}

fun Pipeline.getImage3DUniform(name: String): Image3DUniform? {
    return getUniform<TextureImage3D>(name) as? Image3DUniform
}

fun Pipeline.getBufferUniform(name: String): BufferUniform? {
    return getUniform<UniformBuffer>(name) as? BufferUniform
}
