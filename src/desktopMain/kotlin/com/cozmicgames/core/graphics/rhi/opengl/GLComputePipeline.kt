package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.DesktopStatistics
import com.cozmicgames.core.graphics.rhi.GPUComputePipeline
import com.cozmicgames.core.graphics.rhi.GPUShader
import com.cozmicgames.core.graphics.rhi.internal.checkFail
import com.cozmicgames.core.utils.maths.Vector3i
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryStack.*

class GLComputePipeline(device: GLDevice, builder: Builder) : GPUComputePipeline() {
    class Builder : GPUComputePipeline.Builder {
        internal var shader: GPUShader? = null

        override fun setShader(shader: GPUShader) {
            this.shader = shader
        }
    }

    override val shader = builder.shader as GLProgram

    override val id get() = shader.handle

    override val workgroupSizes = Vector3i()

    init {
        device.checkFail(builder.shader != null) {
            "Pipeline creation requires a shader"
        }

        stackPush().use { stack ->
            val localWorkGroupSize = stack.callocInt(3)

            glGetProgramiv(shader.handle, GL_COMPUTE_WORK_GROUP_SIZE, localWorkGroupSize)

            workgroupSizes.x = localWorkGroupSize.get(0)
            workgroupSizes.y = localWorkGroupSize.get(1)
            workgroupSizes.z = localWorkGroupSize.get(2)
        }

        DesktopStatistics.numPipelines++
    }

    override fun dispose() {
        DesktopStatistics.numPipelines--
    }
}