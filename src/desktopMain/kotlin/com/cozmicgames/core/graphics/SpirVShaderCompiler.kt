package com.cozmicgames.core.graphics

import com.cozmicgames.core.graphics.rhi.GPUShaderSource
import com.cozmicgames.core.graphics.rhi.GPUDevice
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.shaderc.Shaderc.*

class SpirVShaderCompiler(debug: Boolean) : ShaderCompiler {
    private val compiler = shaderc_compiler_initialize()
    private val options = shaderc_compile_options_initialize()

    init {
        if (compiler == 0L || options == 0L) {
            shaderc_compiler_release(compiler)
            shaderc_compile_options_release(options)

            throw Exception("Failed to initialize shaderc")
        }

        shaderc_compile_options_set_target_env(options, shaderc_target_env_vulkan, shaderc_env_version_vulkan_1_3)

        if (debug)
            shaderc_compile_options_set_generate_debug_info(options)
    }

    private fun compileShader(stage: GLSLShaderProcessor.Stage): Result<ByteArray> {
        val kind = when (stage.type) {
            GLSLShaderProcessor.StageType.VERTEX -> shaderc_glsl_vertex_shader
            GLSLShaderProcessor.StageType.FRAGMENT -> shaderc_glsl_fragment_shader
            GLSLShaderProcessor.StageType.COMPUTE -> shaderc_glsl_compute_shader
            GLSLShaderProcessor.StageType.GEOMETRY -> shaderc_glsl_geometry_shader
        }

        val result = shaderc_compile_into_spv(compiler, stage.source, kind, "shader", "main", options)
        val status = shaderc_result_get_compilation_status(result)

        if (status != shaderc_compilation_status_success) {
            val error = shaderc_result_get_error_message(result)
            shaderc_result_release(result)

            return Result.failure(Exception("Failed to compile shader: $error"))
        }

        val spirv = shaderc_result_get_bytes(result)

        val copy = ByteArray(spirv.remaining())
        MemoryUtil.memCopy(spirv, copy)

        shaderc_result_release(result)

        return Result.success(copy)
    }

    override fun compile(source: String, defines: Set<String>): Result<GPUShaderSource> {
        val processedSource = GLSLShaderProcessor.process(source, defines)

        if (processedSource.stages.isEmpty())
            return Result.failure(Exception("No valid shader stages found"))

        val compiledShaders = hashMapOf<GLSLShaderProcessor.StageType, ByteArray>()

        for (stage in processedSource.stages) {
            val result = compileShader(stage)
            if (result.isFailure)
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to compile shader stage ${stage.type}"))

            compiledShaders[stage.type] = result.getOrThrow()
        }

        val type = when {
            compiledShaders.containsKey(GLSLShaderProcessor.StageType.VERTEX) && compiledShaders.containsKey(GLSLShaderProcessor.StageType.FRAGMENT) -> GPUShaderSource.Type.GRAPHICS
            compiledShaders.containsKey(GLSLShaderProcessor.StageType.COMPUTE) -> GPUShaderSource.Type.COMPUTE
            else -> return Result.failure(Exception("Invalid shader stages, must contain either vertex and fragment or compute"))
        }

        return Result.success(SpirVCompiledShader(type, processedSource.resources, compiledShaders))
    }

    override fun dispose() {
        shaderc_compile_options_release(options)
        shaderc_compiler_release(compiler)
    }
}