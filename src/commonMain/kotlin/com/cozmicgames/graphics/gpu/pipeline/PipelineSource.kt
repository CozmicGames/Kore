package com.cozmicgames.graphics.gpu.pipeline

enum class StageType {
    VERTEX,
    GEOMETRY,
    FRAGMENT,
    COMPUTE
}

sealed class PipelineStage(val type: StageType) {
    abstract val preprocessedSource: String
}

class VertexSource(val source: String) : PipelineStage(StageType.VERTEX) {
    override val preprocessedSource
        get() = PipelineSourcePreprocessor.preprocess(
            """
        #define VERTEX
        #include "standard"
        $source
    """.trimIndent()
        )
}

class GeometrySource(val source: String) : PipelineStage(StageType.GEOMETRY) {
    override val preprocessedSource
        get() = PipelineSourcePreprocessor.preprocess(
            """
        #define GEOMETRY
        #include "standard"
        $source
    """.trimIndent()
        )
}

class FragmentSource(val source: String) : PipelineStage(StageType.FRAGMENT) {
    override val preprocessedSource
        get() = PipelineSourcePreprocessor.preprocess(
            """
        #define FRAGMENT
        #include "standard"
        $source
    """.trimIndent()
        )
}

class ComputeSource(val source: String) : PipelineStage(StageType.COMPUTE) {
    override val preprocessedSource
        get() = PipelineSourcePreprocessor.preprocess(
            """
        #define COMPUTE
        #include "standard"
        $source
    """.trimIndent()
        )
}

class ProgramSource(vararg val defines: String) {
    val types = arrayListOf<TypeDefinition>()
    val uniforms = arrayListOf<UniformDefinition<*>>()

    private val stages = arrayOfNulls<PipelineStage>(StageType.values().size)

    fun set(stage: PipelineStage) {
        stages[stage.type.ordinal] = stage
    }

    operator fun get(type: StageType): PipelineStage? {
        return stages[type.ordinal]
    }

    fun has(type: StageType): Boolean {
        return stages[type.ordinal] != null
    }
}

operator fun ProgramSource.contains(type: StageType) = has(type)
