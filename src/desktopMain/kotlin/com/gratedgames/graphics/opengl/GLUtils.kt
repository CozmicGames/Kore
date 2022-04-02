package com.gratedgames.graphics.opengl

import com.gratedgames.graphics.IndexDataType
import com.gratedgames.graphics.Primitive
import com.gratedgames.graphics.gpu.Texture
import org.lwjgl.opengl.GL31C.*

fun Texture.Format.toGLFormat() = when (this) {
    Texture.Format.R8_UNSIGNED,
    Texture.Format.R8_SIGNED,
    Texture.Format.R8_UNORM,
    Texture.Format.R8_SNORM,
    Texture.Format.R16_UNSIGNED,
    Texture.Format.R16_SIGNED,
    Texture.Format.R16_FLOAT,
    Texture.Format.R16_UNORM,
    Texture.Format.R16_SNORM,
    Texture.Format.R32_UNSIGNED,
    Texture.Format.R32_SIGNED,
    Texture.Format.R32_FLOAT -> GL_RED
    Texture.Format.RG8_UNSIGNED,
    Texture.Format.RG8_SIGNED,
    Texture.Format.RG8_UNORM,
    Texture.Format.RG8_SNORM,
    Texture.Format.RG16_UNSIGNED,
    Texture.Format.RG16_SIGNED,
    Texture.Format.RG16_FLOAT,
    Texture.Format.RG16_UNORM,
    Texture.Format.RG16_SNORM,
    Texture.Format.RG32_UNSIGNED,
    Texture.Format.RG32_SIGNED,
    Texture.Format.RG32_FLOAT -> GL_RG
    Texture.Format.RGBA8_UNSIGNED,
    Texture.Format.RGBA8_SIGNED,
    Texture.Format.RGBA8_UNORM,
    Texture.Format.RGBA8_SNORM,
    Texture.Format.RGBA16_UNSIGNED,
    Texture.Format.RGBA16_SIGNED,
    Texture.Format.RGBA16_FLOAT,
    Texture.Format.RGBA16_UNORM,
    Texture.Format.RGBA16_SNORM,
    Texture.Format.RGBA32_UNSIGNED,
    Texture.Format.RGBA32_SIGNED,
    Texture.Format.RGBA32_FLOAT -> GL_RGBA
    Texture.Format.DEPTH16 -> GL_DEPTH_COMPONENT
    Texture.Format.DEPTH24 -> GL_DEPTH_COMPONENT24
    Texture.Format.DEPTH24STENCIL8 -> GL_DEPTH24_STENCIL8
    Texture.Format.DEPTH32 -> GL_DEPTH_COMPONENT32
    Texture.Format.DEPTH32F -> GL_DEPTH_COMPONENT32F
    Texture.Format.STENCIL8 -> GL_STENCIL_INDEX8
}

fun Texture.Format.toGLType() = when (this) {
    Texture.Format.R8_UNSIGNED,
    Texture.Format.R8_UNORM,
    Texture.Format.RG8_UNSIGNED,
    Texture.Format.RG8_UNORM,
    Texture.Format.RGBA8_UNSIGNED,
    Texture.Format.RGBA8_UNORM -> GL_UNSIGNED_BYTE
    Texture.Format.R8_SIGNED,
    Texture.Format.R8_SNORM,
    Texture.Format.RG8_SIGNED,
    Texture.Format.RG8_SNORM,
    Texture.Format.RGBA8_SIGNED,
    Texture.Format.RGBA8_SNORM -> GL_BYTE
    Texture.Format.R16_UNSIGNED,
    Texture.Format.R16_UNORM,
    Texture.Format.RG16_UNSIGNED,
    Texture.Format.RG16_UNORM,
    Texture.Format.RGBA16_UNSIGNED,
    Texture.Format.RGBA16_UNORM -> GL_UNSIGNED_SHORT
    Texture.Format.R16_SIGNED,
    Texture.Format.R16_SNORM,
    Texture.Format.RG16_SIGNED,
    Texture.Format.RG16_SNORM,
    Texture.Format.RGBA16_SIGNED,
    Texture.Format.RGBA16_SNORM -> GL_SHORT
    Texture.Format.R16_FLOAT,
    Texture.Format.RG16_FLOAT,
    Texture.Format.RGBA16_FLOAT -> GL_HALF_FLOAT
    Texture.Format.R32_UNSIGNED,
    Texture.Format.RG32_UNSIGNED,
    Texture.Format.RGBA32_UNSIGNED -> GL_UNSIGNED_INT
    Texture.Format.R32_SIGNED,
    Texture.Format.RG32_SIGNED,
    Texture.Format.RGBA32_SIGNED -> GL_INT
    Texture.Format.R32_FLOAT,
    Texture.Format.RG32_FLOAT,
    Texture.Format.RGBA32_FLOAT -> GL_FLOAT
    Texture.Format.DEPTH16 -> GL_UNSIGNED_SHORT
    Texture.Format.DEPTH24 -> GL_UNSIGNED_INT
    Texture.Format.DEPTH24STENCIL8 -> GL_UNSIGNED_INT_24_8
    Texture.Format.DEPTH32 -> GL_UNSIGNED_INT
    Texture.Format.DEPTH32F -> GL_FLOAT
    Texture.Format.STENCIL8 -> GL_UNSIGNED_BYTE
}

fun Texture.Format.toInternalGLFormat() = when (this) {
    Texture.Format.R8_UNSIGNED -> GL_R8UI
    Texture.Format.RG8_UNSIGNED -> GL_RG8UI
    Texture.Format.RGBA8_UNSIGNED -> GL_RGBA8UI
    Texture.Format.R8_SIGNED -> GL_R8I
    Texture.Format.RG8_SIGNED -> GL_RG8I
    Texture.Format.RGBA8_SIGNED -> GL_RGBA8I
    Texture.Format.R16_UNSIGNED -> GL_R16UI
    Texture.Format.RG16_UNSIGNED -> GL_RG16UI
    Texture.Format.RGBA16_UNSIGNED -> GL_RGBA16UI
    Texture.Format.R16_SIGNED -> GL_R16I
    Texture.Format.RG16_SIGNED -> GL_RG16I
    Texture.Format.RGBA16_SIGNED -> GL_RGBA16I
    Texture.Format.R16_FLOAT -> GL_R16F
    Texture.Format.RG16_FLOAT -> GL_RG16F
    Texture.Format.RGBA16_FLOAT -> GL_RGBA16F
    Texture.Format.R32_UNSIGNED -> GL_R32UI
    Texture.Format.RG32_UNSIGNED -> GL_RG32UI
    Texture.Format.RGBA32_UNSIGNED -> GL_RGBA32UI
    Texture.Format.R32_SIGNED -> GL_R32I
    Texture.Format.RG32_SIGNED -> GL_RG32I
    Texture.Format.RGBA32_SIGNED -> GL_RGBA32I
    Texture.Format.R32_FLOAT -> GL_R32F
    Texture.Format.RG32_FLOAT -> GL_RG32F
    Texture.Format.RGBA32_FLOAT -> GL_RGBA32F
    Texture.Format.R8_UNORM -> GL_R8
    Texture.Format.R16_UNORM -> GL_R16
    Texture.Format.RG8_UNORM -> GL_RG8
    Texture.Format.RG16_UNORM -> GL_RG16
    Texture.Format.RGBA8_UNORM -> GL_RGBA8
    Texture.Format.RGBA16_UNORM -> GL_RGBA16
    Texture.Format.R8_SNORM -> GL_R8_SNORM
    Texture.Format.R16_SNORM -> GL_R16_SNORM
    Texture.Format.RG8_SNORM -> GL_RG8_SNORM
    Texture.Format.RG16_SNORM -> GL_RG16_SNORM
    Texture.Format.RGBA8_SNORM -> GL_RGBA8_SNORM
    Texture.Format.RGBA16_SNORM -> GL_RGBA16_SNORM
    Texture.Format.DEPTH16 -> GL_DEPTH_COMPONENT16
    Texture.Format.DEPTH24 -> GL_DEPTH_COMPONENT24
    Texture.Format.DEPTH24STENCIL8 -> GL_DEPTH24_STENCIL8
    Texture.Format.DEPTH32 -> GL_DEPTH_COMPONENT32
    Texture.Format.DEPTH32F -> GL_DEPTH_COMPONENT32F
    Texture.Format.STENCIL8 -> GL_STENCIL_INDEX8
}

fun Texture.Format.toGLSLType() = when (this) {
    Texture.Format.R8_UNSIGNED -> "r8ui"
    Texture.Format.RG8_UNSIGNED -> "rg8ui"
    Texture.Format.RGBA8_UNSIGNED -> "rgba8ui"
    Texture.Format.R8_SIGNED -> "r8i"
    Texture.Format.RG8_SIGNED -> "rg8i"
    Texture.Format.RGBA8_SIGNED -> "rgba8i"
    Texture.Format.R16_UNSIGNED -> "r16ui"
    Texture.Format.RG16_UNSIGNED -> "rg16ui"
    Texture.Format.RGBA16_UNSIGNED -> "rgba16ui"
    Texture.Format.R16_SIGNED -> "r16i"
    Texture.Format.RG16_SIGNED -> "rg16i"
    Texture.Format.RGBA16_SIGNED -> "rgba16i"
    Texture.Format.R16_FLOAT -> "r16f"
    Texture.Format.RG16_FLOAT -> "rg16f"
    Texture.Format.RGBA16_FLOAT -> "rgba16f"
    Texture.Format.R32_UNSIGNED -> "r32ui"
    Texture.Format.RG32_UNSIGNED -> "rg32ui"
    Texture.Format.RGBA32_UNSIGNED -> "rgba32ui"
    Texture.Format.R32_SIGNED -> "r32i"
    Texture.Format.RG32_SIGNED -> "rg32i"
    Texture.Format.RGBA32_SIGNED -> "rgba32i"
    Texture.Format.R32_FLOAT -> "r32f"
    Texture.Format.RG32_FLOAT -> "rg32f"
    Texture.Format.RGBA32_FLOAT -> "rgba32f"
    Texture.Format.R8_UNORM -> "r8"
    Texture.Format.R16_UNORM -> "r16"
    Texture.Format.RG8_UNORM -> "rg8"
    Texture.Format.RG16_UNORM -> "rg16"
    Texture.Format.RGBA8_UNORM -> "rgba8"
    Texture.Format.RGBA16_UNORM -> "rgba16"
    Texture.Format.R8_SNORM -> "r8_snorm"
    Texture.Format.R16_SNORM -> "r16_snorm"
    Texture.Format.RG8_SNORM -> "rg8_snorm"
    Texture.Format.RG16_SNORM -> "rg16_snorm"
    Texture.Format.RGBA8_SNORM -> "rgba8_snorm"
    Texture.Format.RGBA16_SNORM -> "rgba16_snorm"
    else -> throw Exception("$this does not have a valid GLSL type")
}

fun Texture.Format.toGLSLImagePrefix() = when (this) {
    Texture.Format.R8_UNSIGNED,
    Texture.Format.RG8_UNSIGNED,
    Texture.Format.RGBA8_UNSIGNED,
    Texture.Format.R16_UNSIGNED,
    Texture.Format.RG16_UNSIGNED,
    Texture.Format.RGBA16_UNSIGNED,
    Texture.Format.R32_UNSIGNED,
    Texture.Format.RG32_UNSIGNED,
    Texture.Format.RGBA32_UNSIGNED -> "u"
    Texture.Format.R8_SIGNED,
    Texture.Format.RG8_SIGNED,
    Texture.Format.RGBA8_SIGNED,
    Texture.Format.R16_SIGNED,
    Texture.Format.RG16_SIGNED,
    Texture.Format.RGBA16_SIGNED,
    Texture.Format.R32_SIGNED,
    Texture.Format.RG32_SIGNED,
    Texture.Format.RGBA32_SIGNED -> "i"
    Texture.Format.R16_FLOAT,
    Texture.Format.RG16_FLOAT,
    Texture.Format.RGBA16_FLOAT,
    Texture.Format.R32_FLOAT,
    Texture.Format.RG32_FLOAT,
    Texture.Format.RGBA32_FLOAT,
    Texture.Format.R8_UNORM,
    Texture.Format.R16_UNORM,
    Texture.Format.RG8_UNORM,
    Texture.Format.RG16_UNORM,
    Texture.Format.RGBA8_UNORM,
    Texture.Format.RGBA16_UNORM,
    Texture.Format.R8_SNORM,
    Texture.Format.R16_SNORM,
    Texture.Format.RG8_SNORM,
    Texture.Format.RG16_SNORM,
    Texture.Format.RGBA8_SNORM,
    Texture.Format.RGBA16_SNORM -> ""
    else -> throw Exception("$this does not have a valid GLSL type")
}

fun Primitive.toGLPrimitive() = when (this) {
    Primitive.POINTS -> GL_POINTS
    Primitive.LINES -> GL_LINES
    Primitive.LINE_STRIP -> GL_LINE_STRIP
    Primitive.TRIANGLES -> GL_TRIANGLES
    Primitive.TRIANGLE_STRIP -> GL_TRIANGLE_STRIP
}

fun IndexDataType.toGLIndexDataType() = when (this) {
    IndexDataType.BYTE -> GL_UNSIGNED_BYTE
    IndexDataType.SHORT -> GL_UNSIGNED_SHORT
    IndexDataType.INT -> GL_UNSIGNED_INT
}
