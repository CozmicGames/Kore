package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.GLSLShaderProcessor
import com.cozmicgames.core.graphics.rhi.GPUTextureFormat
import com.cozmicgames.core.graphics.rhi.GPUImageAccess
import com.cozmicgames.core.graphics.Primitive
import org.lwjgl.opengl.GL46C.*

fun GPUTextureFormat.toGLFormat() = when (this) {
    GPUTextureFormat.R8_UNSIGNED,
    GPUTextureFormat.R8_SIGNED,
    GPUTextureFormat.R8_UNORM,
    GPUTextureFormat.R8_SNORM,
    GPUTextureFormat.R16_UNSIGNED,
    GPUTextureFormat.R16_SIGNED,
    GPUTextureFormat.R16_FLOAT,
    GPUTextureFormat.R16_UNORM,
    GPUTextureFormat.R16_SNORM,
    GPUTextureFormat.R32_UNSIGNED,
    GPUTextureFormat.R32_SIGNED,
    GPUTextureFormat.R32_FLOAT -> GL_RED
    GPUTextureFormat.RG8_UNSIGNED,
    GPUTextureFormat.RG8_SIGNED,
    GPUTextureFormat.RG8_UNORM,
    GPUTextureFormat.RG8_SNORM,
    GPUTextureFormat.RG16_UNSIGNED,
    GPUTextureFormat.RG16_SIGNED,
    GPUTextureFormat.RG16_FLOAT,
    GPUTextureFormat.RG16_UNORM,
    GPUTextureFormat.RG16_SNORM,
    GPUTextureFormat.RG32_UNSIGNED,
    GPUTextureFormat.RG32_SIGNED,
    GPUTextureFormat.RG32_FLOAT -> GL_RG
    GPUTextureFormat.RGBA8_UNSIGNED,
    GPUTextureFormat.RGBA8_SIGNED,
    GPUTextureFormat.RGBA8_UNORM,
    GPUTextureFormat.RGBA8_SNORM,
    GPUTextureFormat.RGBA16_UNSIGNED,
    GPUTextureFormat.RGBA16_SIGNED,
    GPUTextureFormat.RGBA16_FLOAT,
    GPUTextureFormat.RGBA16_UNORM,
    GPUTextureFormat.RGBA16_SNORM,
    GPUTextureFormat.RGBA32_UNSIGNED,
    GPUTextureFormat.RGBA32_SIGNED,
    GPUTextureFormat.RGBA32_FLOAT -> GL_RGBA
    GPUTextureFormat.DEPTH16 -> GL_DEPTH_COMPONENT
    GPUTextureFormat.DEPTH24 -> GL_DEPTH_COMPONENT24
    GPUTextureFormat.DEPTH24STENCIL8 -> GL_DEPTH24_STENCIL8
    GPUTextureFormat.DEPTH32 -> GL_DEPTH_COMPONENT32
    GPUTextureFormat.DEPTH32F -> GL_DEPTH_COMPONENT32F
    GPUTextureFormat.STENCIL8 -> GL_STENCIL_INDEX8
}

fun GPUTextureFormat.toGLType() = when (this) {
    GPUTextureFormat.R8_UNSIGNED,
    GPUTextureFormat.R8_UNORM,
    GPUTextureFormat.RG8_UNSIGNED,
    GPUTextureFormat.RG8_UNORM,
    GPUTextureFormat.RGBA8_UNSIGNED,
    GPUTextureFormat.RGBA8_UNORM -> GL_UNSIGNED_BYTE
    GPUTextureFormat.R8_SIGNED,
    GPUTextureFormat.R8_SNORM,
    GPUTextureFormat.RG8_SIGNED,
    GPUTextureFormat.RG8_SNORM,
    GPUTextureFormat.RGBA8_SIGNED,
    GPUTextureFormat.RGBA8_SNORM -> GL_BYTE
    GPUTextureFormat.R16_UNSIGNED,
    GPUTextureFormat.R16_UNORM,
    GPUTextureFormat.RG16_UNSIGNED,
    GPUTextureFormat.RG16_UNORM,
    GPUTextureFormat.RGBA16_UNSIGNED,
    GPUTextureFormat.RGBA16_UNORM -> GL_UNSIGNED_SHORT
    GPUTextureFormat.R16_SIGNED,
    GPUTextureFormat.R16_SNORM,
    GPUTextureFormat.RG16_SIGNED,
    GPUTextureFormat.RG16_SNORM,
    GPUTextureFormat.RGBA16_SIGNED,
    GPUTextureFormat.RGBA16_SNORM -> GL_SHORT
    GPUTextureFormat.R16_FLOAT,
    GPUTextureFormat.RG16_FLOAT,
    GPUTextureFormat.RGBA16_FLOAT -> GL_HALF_FLOAT
    GPUTextureFormat.R32_UNSIGNED,
    GPUTextureFormat.RG32_UNSIGNED,
    GPUTextureFormat.RGBA32_UNSIGNED -> GL_UNSIGNED_INT
    GPUTextureFormat.R32_SIGNED,
    GPUTextureFormat.RG32_SIGNED,
    GPUTextureFormat.RGBA32_SIGNED -> GL_INT
    GPUTextureFormat.R32_FLOAT,
    GPUTextureFormat.RG32_FLOAT,
    GPUTextureFormat.RGBA32_FLOAT -> GL_FLOAT
    GPUTextureFormat.DEPTH16 -> GL_UNSIGNED_SHORT
    GPUTextureFormat.DEPTH24 -> GL_UNSIGNED_INT
    GPUTextureFormat.DEPTH24STENCIL8 -> GL_UNSIGNED_INT_24_8
    GPUTextureFormat.DEPTH32 -> GL_UNSIGNED_INT
    GPUTextureFormat.DEPTH32F -> GL_FLOAT
    GPUTextureFormat.STENCIL8 -> GL_UNSIGNED_BYTE
}

fun GPUTextureFormat.toInternalGLFormat() = when (this) {
    GPUTextureFormat.R8_UNSIGNED -> GL_R8UI
    GPUTextureFormat.RG8_UNSIGNED -> GL_RG8UI
    GPUTextureFormat.RGBA8_UNSIGNED -> GL_RGBA8UI
    GPUTextureFormat.R8_SIGNED -> GL_R8I
    GPUTextureFormat.RG8_SIGNED -> GL_RG8I
    GPUTextureFormat.RGBA8_SIGNED -> GL_RGBA8I
    GPUTextureFormat.R16_UNSIGNED -> GL_R16UI
    GPUTextureFormat.RG16_UNSIGNED -> GL_RG16UI
    GPUTextureFormat.RGBA16_UNSIGNED -> GL_RGBA16UI
    GPUTextureFormat.R16_SIGNED -> GL_R16I
    GPUTextureFormat.RG16_SIGNED -> GL_RG16I
    GPUTextureFormat.RGBA16_SIGNED -> GL_RGBA16I
    GPUTextureFormat.R16_FLOAT -> GL_R16F
    GPUTextureFormat.RG16_FLOAT -> GL_RG16F
    GPUTextureFormat.RGBA16_FLOAT -> GL_RGBA16F
    GPUTextureFormat.R32_UNSIGNED -> GL_R32UI
    GPUTextureFormat.RG32_UNSIGNED -> GL_RG32UI
    GPUTextureFormat.RGBA32_UNSIGNED -> GL_RGBA32UI
    GPUTextureFormat.R32_SIGNED -> GL_R32I
    GPUTextureFormat.RG32_SIGNED -> GL_RG32I
    GPUTextureFormat.RGBA32_SIGNED -> GL_RGBA32I
    GPUTextureFormat.R32_FLOAT -> GL_R32F
    GPUTextureFormat.RG32_FLOAT -> GL_RG32F
    GPUTextureFormat.RGBA32_FLOAT -> GL_RGBA32F
    GPUTextureFormat.R8_UNORM -> GL_R8
    GPUTextureFormat.R16_UNORM -> GL_R16
    GPUTextureFormat.RG8_UNORM -> GL_RG8
    GPUTextureFormat.RG16_UNORM -> GL_RG16
    GPUTextureFormat.RGBA8_UNORM -> GL_RGBA8
    GPUTextureFormat.RGBA16_UNORM -> GL_RGBA16
    GPUTextureFormat.R8_SNORM -> GL_R8_SNORM
    GPUTextureFormat.R16_SNORM -> GL_R16_SNORM
    GPUTextureFormat.RG8_SNORM -> GL_RG8_SNORM
    GPUTextureFormat.RG16_SNORM -> GL_RG16_SNORM
    GPUTextureFormat.RGBA8_SNORM -> GL_RGBA8_SNORM
    GPUTextureFormat.RGBA16_SNORM -> GL_RGBA16_SNORM
    GPUTextureFormat.DEPTH16 -> GL_DEPTH_COMPONENT16
    GPUTextureFormat.DEPTH24 -> GL_DEPTH_COMPONENT24
    GPUTextureFormat.DEPTH24STENCIL8 -> GL_DEPTH24_STENCIL8
    GPUTextureFormat.DEPTH32 -> GL_DEPTH_COMPONENT32
    GPUTextureFormat.DEPTH32F -> GL_DEPTH_COMPONENT32F
    GPUTextureFormat.STENCIL8 -> GL_STENCIL_INDEX8
}

fun GPUTextureFormat.toGLSLType() = when (this) {
    GPUTextureFormat.R8_UNSIGNED -> "r8ui"
    GPUTextureFormat.RG8_UNSIGNED -> "rg8ui"
    GPUTextureFormat.RGBA8_UNSIGNED -> "rgba8ui"
    GPUTextureFormat.R8_SIGNED -> "r8i"
    GPUTextureFormat.RG8_SIGNED -> "rg8i"
    GPUTextureFormat.RGBA8_SIGNED -> "rgba8i"
    GPUTextureFormat.R16_UNSIGNED -> "r16ui"
    GPUTextureFormat.RG16_UNSIGNED -> "rg16ui"
    GPUTextureFormat.RGBA16_UNSIGNED -> "rgba16ui"
    GPUTextureFormat.R16_SIGNED -> "r16i"
    GPUTextureFormat.RG16_SIGNED -> "rg16i"
    GPUTextureFormat.RGBA16_SIGNED -> "rgba16i"
    GPUTextureFormat.R16_FLOAT -> "r16f"
    GPUTextureFormat.RG16_FLOAT -> "rg16f"
    GPUTextureFormat.RGBA16_FLOAT -> "rgba16f"
    GPUTextureFormat.R32_UNSIGNED -> "r32ui"
    GPUTextureFormat.RG32_UNSIGNED -> "rg32ui"
    GPUTextureFormat.RGBA32_UNSIGNED -> "rgba32ui"
    GPUTextureFormat.R32_SIGNED -> "r32i"
    GPUTextureFormat.RG32_SIGNED -> "rg32i"
    GPUTextureFormat.RGBA32_SIGNED -> "rgba32i"
    GPUTextureFormat.R32_FLOAT -> "r32f"
    GPUTextureFormat.RG32_FLOAT -> "rg32f"
    GPUTextureFormat.RGBA32_FLOAT -> "rgba32f"
    GPUTextureFormat.R8_UNORM -> "r8"
    GPUTextureFormat.R16_UNORM -> "r16"
    GPUTextureFormat.RG8_UNORM -> "rg8"
    GPUTextureFormat.RG16_UNORM -> "rg16"
    GPUTextureFormat.RGBA8_UNORM -> "rgba8"
    GPUTextureFormat.RGBA16_UNORM -> "rgba16"
    GPUTextureFormat.R8_SNORM -> "r8_snorm"
    GPUTextureFormat.R16_SNORM -> "r16_snorm"
    GPUTextureFormat.RG8_SNORM -> "rg8_snorm"
    GPUTextureFormat.RG16_SNORM -> "rg16_snorm"
    GPUTextureFormat.RGBA8_SNORM -> "rgba8_snorm"
    GPUTextureFormat.RGBA16_SNORM -> "rgba16_snorm"
    else -> throw Exception("$this does not have a valid GLSL type")
}

fun GPUTextureFormat.toGLSLImagePrefix() = when (this) {
    GPUTextureFormat.R8_UNSIGNED,
    GPUTextureFormat.RG8_UNSIGNED,
    GPUTextureFormat.RGBA8_UNSIGNED,
    GPUTextureFormat.R16_UNSIGNED,
    GPUTextureFormat.RG16_UNSIGNED,
    GPUTextureFormat.RGBA16_UNSIGNED,
    GPUTextureFormat.R32_UNSIGNED,
    GPUTextureFormat.RG32_UNSIGNED,
    GPUTextureFormat.RGBA32_UNSIGNED -> "u"
    GPUTextureFormat.R8_SIGNED,
    GPUTextureFormat.RG8_SIGNED,
    GPUTextureFormat.RGBA8_SIGNED,
    GPUTextureFormat.R16_SIGNED,
    GPUTextureFormat.RG16_SIGNED,
    GPUTextureFormat.RGBA16_SIGNED,
    GPUTextureFormat.R32_SIGNED,
    GPUTextureFormat.RG32_SIGNED,
    GPUTextureFormat.RGBA32_SIGNED -> "i"
    GPUTextureFormat.R16_FLOAT,
    GPUTextureFormat.RG16_FLOAT,
    GPUTextureFormat.RGBA16_FLOAT,
    GPUTextureFormat.R32_FLOAT,
    GPUTextureFormat.RG32_FLOAT,
    GPUTextureFormat.RGBA32_FLOAT,
    GPUTextureFormat.R8_UNORM,
    GPUTextureFormat.R16_UNORM,
    GPUTextureFormat.RG8_UNORM,
    GPUTextureFormat.RG16_UNORM,
    GPUTextureFormat.RGBA8_UNORM,
    GPUTextureFormat.RGBA16_UNORM,
    GPUTextureFormat.R8_SNORM,
    GPUTextureFormat.R16_SNORM,
    GPUTextureFormat.RG8_SNORM,
    GPUTextureFormat.RG16_SNORM,
    GPUTextureFormat.RGBA8_SNORM,
    GPUTextureFormat.RGBA16_SNORM -> ""
    else -> throw Exception("$this does not have a valid GLSL type")
}

fun GPUImageAccess.toGLAccess() = when (this) {
    GPUImageAccess.READ -> GL_READ_ONLY
    GPUImageAccess.WRITE -> GL_WRITE_ONLY
    GPUImageAccess.READ_WRITE -> GL_READ_WRITE
}

fun Primitive.toGLPrimitive() = when (this) {
    Primitive.POINTS -> GL_POINTS
    Primitive.LINES -> GL_LINES
    Primitive.LINE_STRIP -> GL_LINE_STRIP
    Primitive.TRIANGLES -> GL_TRIANGLES
    Primitive.TRIANGLE_STRIP -> GL_TRIANGLE_STRIP
}

fun GLSLShaderProcessor.StageType.toGLType() = when(this) {
    GLSLShaderProcessor.StageType.VERTEX -> GL_VERTEX_SHADER
    GLSLShaderProcessor.StageType.FRAGMENT -> GL_FRAGMENT_SHADER
    GLSLShaderProcessor.StageType.GEOMETRY -> GL_GEOMETRY_SHADER
    GLSLShaderProcessor.StageType.COMPUTE -> GL_COMPUTE_SHADER
}
