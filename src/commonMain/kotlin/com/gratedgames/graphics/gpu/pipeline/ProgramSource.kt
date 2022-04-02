package com.gratedgames.graphics.gpu.pipeline

enum class StageType {
    VERTEX,
    GEOMETRY,
    FRAGMENT,
    COMPUTE
}

sealed class ProgramStage(val type: StageType) {
    abstract val preprocessedSource: String
}

class VertexSource(val source: String) : ProgramStage(StageType.VERTEX) {
    override val preprocessedSource
        get() = ProgramLibrary.process(
            """
        #define VERTEX
        #include "standard"
        $source
    """.trimIndent()
        )
}

class GeometrySource(val source: String) : ProgramStage(StageType.GEOMETRY) {
    override val preprocessedSource
        get() = ProgramLibrary.process(
            """
        #define GEOMETRY
        #include "standard"
        $source
    """.trimIndent()
        )
}

class FragmentSource(val source: String) : ProgramStage(StageType.FRAGMENT) {
    override val preprocessedSource
        get() = ProgramLibrary.process(
            """
        #define FRAGMENT
        #include "standard"
        $source
    """.trimIndent()
        )
}

class ComputeSource(val source: String) : ProgramStage(StageType.COMPUTE) {
    override val preprocessedSource
        get() = ProgramLibrary.process(
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

    private val stages = arrayOfNulls<ProgramStage>(StageType.values().size)

    fun set(stage: ProgramStage) {
        stages[stage.type.ordinal] = stage
    }

    operator fun get(type: StageType): ProgramStage? {
        return stages[type.ordinal]
    }

    fun has(type: StageType): Boolean {
        return stages[type.ordinal] != null
    }
}

operator fun ProgramSource.contains(type: StageType) = has(type)
