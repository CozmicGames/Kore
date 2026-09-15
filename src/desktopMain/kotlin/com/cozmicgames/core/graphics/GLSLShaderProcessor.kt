package com.cozmicgames.core.graphics

import com.cozmicgames.core.Kore
import com.cozmicgames.core.graphics.rhi.GPUShader
import com.cozmicgames.core.log
import com.cozmicgames.core.utils.extensions.removeBlankLines
import com.cozmicgames.core.utils.extensions.removeComments

object GLSLShaderProcessor {
    enum class StageType {
        VERTEX,
        FRAGMENT,
        GEOMETRY,
        COMPUTE
    }

    class Stage(val type: StageType, val source: String)

    class Source(val stages: List<Stage>, val resources: List<GPUShader.Resource>)

    private val resourceRegex = Regex("""layout\s*\(([^)]*)\)\s*(?:uniform\s+(\w+)\s+(\w+)\s*;|buffer\s+(\w+)\s*\{)""")
    private val bindingRegex = Regex("""\bbinding\s*=\s*(\d+)""")

    private fun buildSource(common: String, source: String, defines: Set<String>, stageType: StageType): String {
        val builder = StringBuilder()

        builder.appendLine("#version 460")
        builder.appendLine("#define ${stageType.name}_SHADER")
        defines.forEach { define ->
            builder.appendLine("#define $define")
        }
        builder.appendLine(common)
        builder.appendLine(source)

        return builder.toString()
    }

    //TODO: Includes
    fun process(source: String, defines: Set<String>): Source {
        val stages = arrayListOf<Stage>()

        val lines = source.removeComments().removeBlankLines().lines()

        val commonBuilder = StringBuilder()
        val vertexBuilder = StringBuilder()
        val geometryBuilder = StringBuilder()
        val fragmentBuilder = StringBuilder()
        val computeBuilder = StringBuilder()

        var currentBuilder: StringBuilder? = null

        for (line in lines) {
            if (line.trim().startsWith("#section ", true)) {
                val parts = line.trim().split(" ").filter { it.isNotEmpty() && it.isNotBlank() }
                when (val sectionType = parts.getOrElse(1) { "Missing section type" }) {
                    "common" -> currentBuilder = commonBuilder
                    "vertex" -> currentBuilder = vertexBuilder
                    "geometry" -> currentBuilder = geometryBuilder
                    "fragment" -> currentBuilder = fragmentBuilder
                    "compute" -> currentBuilder = computeBuilder
                    else -> Kore.log.error(this::class, "Failed to parse program section, unknown section type: $sectionType")
                }
            } else
                currentBuilder?.appendLine(line)
        }

        if (vertexBuilder.isNotEmpty())
            stages += Stage(StageType.VERTEX, buildSource(commonBuilder.toString(), vertexBuilder.toString(), defines, StageType.VERTEX))

        if (geometryBuilder.isNotEmpty())
            stages += Stage(StageType.GEOMETRY, buildSource(commonBuilder.toString(), geometryBuilder.toString(), defines, StageType.GEOMETRY))

        if (fragmentBuilder.isNotEmpty())
            stages += Stage(StageType.FRAGMENT, buildSource(commonBuilder.toString(), fragmentBuilder.toString(), defines, StageType.FRAGMENT))

        if (computeBuilder.isNotEmpty())
            stages += Stage(StageType.COMPUTE, buildSource(commonBuilder.toString(), computeBuilder.toString(), defines, StageType.COMPUTE))

        val resources = arrayListOf<GPUShader.Resource>()

        for (stage in stages) {
            for (match in resourceRegex.findAll(stage.source)) {
                val layout = match.groupValues[1]
                val typeName = match.groupValues[2]
                val uniformName = match.groupValues[3]
                val bufferName = match.groupValues[4]

                val binding = bindingRegex.find(layout)?.groupValues?.get(1)?.toInt() ?: throw IllegalArgumentException("Resource has no binding")

                val name: String
                val type: GPUShader.ResourceType

                if (bufferName.isNotEmpty()) {
                    name = bufferName
                    type = GPUShader.ResourceType.BUFFER
                } else {
                    name = uniformName

                    type = when {
                        typeName.startsWith("sampler") -> GPUShader.ResourceType.TEXTURE
                        typeName.startsWith("image") -> GPUShader.ResourceType.IMAGE
                        else -> throw IllegalArgumentException("Unknown resource type: $typeName")
                    }
                }

                if (resources.any { it.name == name })
                    throw IllegalArgumentException("Duplicate resource name: $name")

                if (resources.any { it.binding == binding })
                    throw IllegalArgumentException("Duplicate resource binding: $binding")

                resources += GPUShader.Resource(name, type, binding)
            }
        }

        return Source(stages, resources)
    }
}