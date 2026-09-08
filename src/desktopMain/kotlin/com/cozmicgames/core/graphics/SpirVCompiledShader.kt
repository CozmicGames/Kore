package com.cozmicgames.core.graphics

import com.cozmicgames.core.PlatformType
import com.cozmicgames.core.graphics.rhi.GPUShaderSource
import com.cozmicgames.core.graphics.rhi.GPUShader

class SpirVCompiledShader(override val type: GPUShaderSource.Type, val resources: List<GPUShader.Resource>, val data: Map<GLSLShaderProcessor.StageType, ByteArray>): GPUShaderSource {
    override val platformType = PlatformType.DESKTOP
}