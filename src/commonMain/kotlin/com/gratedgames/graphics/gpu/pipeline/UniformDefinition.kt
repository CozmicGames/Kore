package com.gratedgames.graphics.gpu.pipeline

import com.gratedgames.graphics.gpu.*
import com.gratedgames.utils.maths.Matrix4x4

sealed class UniformDefinition<T : Any>(val name: String, val size: Int, val type: Type) {
    enum class Type(val uniformType: Uniform.Type) {
        FLOAT(Uniform.Type.FLOAT),
        VEC2(Uniform.Type.FLOAT),
        VEC3(Uniform.Type.FLOAT),
        VEC4(Uniform.Type.FLOAT),
        INT(Uniform.Type.INT),
        IVEC2(Uniform.Type.INT),
        IVEC3(Uniform.Type.INT),
        IVEC4(Uniform.Type.INT),
        BOOLEAN(Uniform.Type.BOOLEAN),
        TEXTURE_2D(Uniform.Type.TEXTURE_2D),
        TEXTURE_3D(Uniform.Type.TEXTURE_3D),
        TEXTURE_CUBE(Uniform.Type.TEXTURE_CUBE),
        MATRIX(Uniform.Type.MATRIX),
        BUFFER(Uniform.Type.BUFFER),
        IMAGE_2D(Uniform.Type.IMAGE_2D),
        IMAGE_3D(Uniform.Type.IMAGE_3D)
    }

    abstract fun createUniform(pipeline: Pipeline): Uniform<T>
}

class FloatUniformDefinition(name: String, size: Int) : UniformDefinition<Float>(name, size, Type.FLOAT) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getFloatUniform(name))
}

class Vec2UniformDefinition(name: String, size: Int) : UniformDefinition<Float>(name, size, Type.VEC2) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getFloatUniform(name))
}

class Vec3UniformDefinition(name: String, size: Int) : UniformDefinition<Float>(name, size, Type.VEC3) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getFloatUniform(name))
}

class Vec4UniformDefinition(name: String, size: Int) : UniformDefinition<Float>(name, size, Type.VEC4) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getFloatUniform(name))
}

class IntUniformDefinition(name: String, size: Int) : UniformDefinition<Int>(name, size, Type.INT) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getIntUniform(name))
}

class IVec2UniformDefinition(name: String, size: Int) : UniformDefinition<Int>(name, size, Type.IVEC2) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getIntUniform(name))
}

class IVec3UniformDefinition(name: String, size: Int) : UniformDefinition<Int>(name, size, Type.IVEC3) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getIntUniform(name))
}

class IVec4UniformDefinition(name: String, size: Int) : UniformDefinition<Int>(name, size, Type.IVEC4) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getIntUniform(name))
}

class BooleanUniformDefinition(name: String, size: Int) : UniformDefinition<Boolean>(name, size, Type.BOOLEAN) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getBooleanUniform(name))
}

class Texture2DUniformDefinition(name: String, size: Int) : UniformDefinition<Texture2D>(name, size, Type.TEXTURE_2D) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getTexture2DUniform(name))
}

class Texture3DUniformDefinition(name: String, size: Int) : UniformDefinition<Texture3D>(name, size, Type.TEXTURE_3D) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getTexture3DUniform(name))
}

class TextureCubeUniformDefinition(name: String, size: Int) : UniformDefinition<TextureCube>(name, size, Type.TEXTURE_CUBE) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getTextureCubeUniform(name))
}

class MatrixUniformDefinition(name: String, size: Int) : UniformDefinition<Matrix4x4>(name, size, Type.MATRIX) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getMatrixUniform(name))
}

class BufferUniformDefinition(name: String, val content: Array<Property>) : UniformDefinition<UniformBuffer>(name, 1, Type.BUFFER) {
    open class Property(val type: String, val name: String)

    class ArrayProperty(type: String, name: String, val size: Int) : Property(type, name)

    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getBufferUniform(name)) as Uniform<UniformBuffer>
}

class Image2DUniformDefinition(name: String, val format: Texture.Format) : UniformDefinition<TextureImage2D>(name, 1, Type.IMAGE_2D) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getImage2DUniform(name))
}

class Image3DUniformDefinition(name: String, val format: Texture.Format) : UniformDefinition<TextureImage3D>(name, 1, Type.IMAGE_3D) {
    override fun createUniform(pipeline: Pipeline) = requireNotNull(pipeline.getImage3DUniform(name))
}
