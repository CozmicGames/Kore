package com.cozmicgames.core.graphics

import com.cozmicgames.core.graphics.rhi.GPUShaderSource
import com.cozmicgames.core.utils.Disposable

interface ShaderCompiler : Disposable {
    fun compile(source: String, defines: Set<String> = emptySet()): Result<GPUShaderSource>
}