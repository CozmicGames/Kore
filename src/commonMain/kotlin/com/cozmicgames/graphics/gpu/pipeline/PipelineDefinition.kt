package com.cozmicgames.graphics.gpu.pipeline

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.Files
import com.cozmicgames.files.readToString
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.Pipeline
import com.cozmicgames.log
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.extensions.removeBlankLines
import com.cozmicgames.utils.extensions.removeComments

class PipelineDefinition() {
    constructor(file: String, type: Files.Type) : this() {
        load(file, type)
    }

    constructor(text: String) : this() {
        parse(text)
    }

    private val sections = hashMapOf<DefinitionSection.Type, DefinitionSection>()
    private val isComputeDefinition get() = hasSection(DefinitionSection.Type.COMPUTE)

    fun setSection(section: DefinitionSection) {
        sections[section.type] = section
    }

    fun hasSection(type: DefinitionSection.Type) = type in sections

    fun removeSection(type: DefinitionSection.Type) {
        sections.remove(type)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : DefinitionSection> getSection(type: DefinitionSection.Type) = sections[type] as? T

    fun load(file: String, type: Files.Type, charset: Charset = Charsets.UTF8) {
        val text = Kore.files.readToString(file, type, charset)
        parse(text)
    }

    fun parse(text: String) {
        val lines = text.removeComments().removeBlankLines().lines()

        val layoutBuilder = StringBuilder()
        val typesBuilder = StringBuilder()
        val stateBuilder = StringBuilder()
        val uniformsBuilder = StringBuilder()
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
                    "layout" -> currentBuilder = layoutBuilder
                    "types" -> currentBuilder = typesBuilder
                    "state" -> currentBuilder = stateBuilder
                    "uniforms" -> currentBuilder = uniformsBuilder
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

        if (layoutBuilder.isNotEmpty())
            setSection(LayoutSection(layoutBuilder.toString()))

        if (typesBuilder.isNotEmpty())
            setSection(TypesSection(typesBuilder.toString()))

        if (stateBuilder.isNotEmpty())
            setSection(StateSection(stateBuilder.toString()))

        if (uniformsBuilder.isNotEmpty())
            setSection(UniformsSection(uniformsBuilder.toString()))

        if (commonBuilder.isNotEmpty())
            setSection(SourceSection(commonBuilder.toString(), DefinitionSection.Type.COMMON))

        if (vertexBuilder.isNotEmpty())
            setSection(SourceSection(vertexBuilder.toString(), DefinitionSection.Type.VERTEX))

        if (geometryBuilder.isNotEmpty())
            setSection(SourceSection(geometryBuilder.toString(), DefinitionSection.Type.GEOMETRY))

        if (fragmentBuilder.isNotEmpty())
            setSection(SourceSection(fragmentBuilder.toString(), DefinitionSection.Type.FRAGMENT))

        if (computeBuilder.isNotEmpty())
            setSection(SourceSection(computeBuilder.toString(), DefinitionSection.Type.COMPUTE))
    }

    fun createPipeline(vararg defines: String) = Kore.graphics.createPipeline(if (isComputeDefinition) Pipeline.Type.COMPUTE else Pipeline.Type.GRAPHICS) {
        if (hasSection(DefinitionSection.Type.LAYOUT))
            vertexLayout = requireNotNull(getSection<LayoutSection>(DefinitionSection.Type.LAYOUT)).layout

        if (hasSection(DefinitionSection.Type.STATE)) {
            val section = requireNotNull(getSection<StateSection>(DefinitionSection.Type.STATE))
            blendState = section.blendState
            cullState = section.cullState
            colorMask = section.colorMask
            depthState = section.depthState
            depthMask = section.depthMask
            stencilState = section.stencilState
            stencilMask = section.stencilMask
        }

        if (hasSection(DefinitionSection.Type.VERTEX)) {
            val source = ProgramSource(*defines)

            if (hasSection(DefinitionSection.Type.TYPES)) {
                val section = requireNotNull(getSection<TypesSection>(DefinitionSection.Type.TYPES))
                source.types.addAll(section.types)
            }

            if (hasSection(DefinitionSection.Type.UNIFORMS)) {
                val section = requireNotNull(getSection<UniformsSection>(DefinitionSection.Type.UNIFORMS))
                source.uniforms.addAll(section.uniforms)
            }

            val generalSource = if (hasSection(DefinitionSection.Type.COMMON))
                requireNotNull(getSection<SourceSection>(DefinitionSection.Type.COMMON)).source
            else
                null

            val vertexSource = requireNotNull(getSection<SourceSection>(DefinitionSection.Type.VERTEX)).source
            source.set(VertexSource(buildString {
                generalSource?.let {
                    appendLine(it)
                }
                appendLine(vertexSource)
            }))

            if (hasSection(DefinitionSection.Type.GEOMETRY)) {
                val geometrySource = requireNotNull(getSection<SourceSection>(DefinitionSection.Type.GEOMETRY)).source
                source.set(GeometrySource(buildString {
                    generalSource?.let {
                        appendLine(it)
                    }
                    appendLine(geometrySource)
                }))
            }

            if (hasSection(DefinitionSection.Type.FRAGMENT)) {
                val fragmentSource = requireNotNull(getSection<SourceSection>(DefinitionSection.Type.FRAGMENT)).source
                source.set(FragmentSource(buildString {
                    generalSource?.let {
                        appendLine(it)
                    }
                    appendLine(fragmentSource)
                }))
            }

            programSource = source
        } else if (hasSection(DefinitionSection.Type.COMPUTE)) {
            val source = ProgramSource(*defines)

            if (hasSection(DefinitionSection.Type.TYPES)) {
                val section = requireNotNull(getSection<TypesSection>(DefinitionSection.Type.TYPES))
                source.types.addAll(section.types)
            }

            if (hasSection(DefinitionSection.Type.UNIFORMS)) {
                val section = requireNotNull(getSection<UniformsSection>(DefinitionSection.Type.UNIFORMS))
                source.uniforms.addAll(section.uniforms)
            }

            val generalSource = if (hasSection(DefinitionSection.Type.COMMON))
                requireNotNull(getSection<SourceSection>(DefinitionSection.Type.COMMON)).source
            else
                null

            val computeSource = requireNotNull(getSection<SourceSection>(DefinitionSection.Type.COMPUTE)).source
            source.set(ComputeSource(buildString {
                generalSource?.let {
                    appendLine(it)
                }
                appendLine(computeSource)
            }))

            programSource = source
        }
    }
}