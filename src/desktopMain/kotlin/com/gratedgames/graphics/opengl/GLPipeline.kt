package com.gratedgames.graphics.opengl

import com.gratedgames.graphics.DesktopStatistics
import com.gratedgames.graphics.gpu.Pipeline
import com.gratedgames.graphics.gpu.Uniform
import com.gratedgames.utils.Disposable
import org.lwjgl.opengl.GL32C.*

abstract class GLPipeline : Pipeline() {
    companion object {
        private var currentID = 0
    }

    override val id = ++currentID

    abstract val shader: Int

    override val uniforms get() = uniformMap.keys

    protected val uniformMap = hashMapOf<String, Uniform<*>>()

    internal val applyOnSetPipelineUniforms = arrayListOf<GLUniform>()

    private val updatedUniforms = hashSetOf<GLUniform>()
    private val updatingUniforms = hashSetOf<GLUniform>()

    init {
        DesktopStatistics.numPipelines++
    }

    fun setUniformUpdated(uniform: GLUniform) {
        updatedUniforms += uniform
    }

    fun updateUniforms() {
        if (updatedUniforms.isEmpty())
            return

        updatingUniforms.addAll(updatedUniforms)
        updatedUniforms.clear()

        updatingUniforms.forEach {
            it.apply()
        }

        updatingUniforms.clear()
    }

    override fun <T : Any> getUniform(name: String) = uniformMap[name] as? Uniform<T>?

    override fun dispose() {
        for ((_, uniform) in uniformMap)
            if (uniform is Disposable)
                uniform.dispose()

        glDeleteProgram(shader)

        DesktopStatistics.numPipelines--
    }
}