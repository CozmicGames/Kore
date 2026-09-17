package com.cozmicgames.core.graphics.rhi.opengl

import com.cozmicgames.core.graphics.rhi.GPUBuffer
import com.cozmicgames.core.graphics.rhi.GPUCommandBuffer
import com.cozmicgames.core.graphics.rhi.GPUGraphicsPipeline
import com.cozmicgames.core.graphics.rhi.GPUImageAccess
import com.cozmicgames.core.graphics.rhi.GPUPipeline
import com.cozmicgames.core.graphics.rhi.GPURenderPass
import com.cozmicgames.core.graphics.rhi.GPUSampler
import com.cozmicgames.core.graphics.rhi.GPUTexture
import com.cozmicgames.core.graphics.Primitive
import com.cozmicgames.core.graphics.rhi.GPUShader
import com.cozmicgames.core.utils.Color
import com.cozmicgames.core.utils.collections.DynamicArray
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.system.MemoryStack.*

class GLCommandBuffer : GPUCommandBuffer {
    private object OpCodes {
        // 1-31: render passes / state
        const val BEGIN_MAIN_RENDERPASS = 1
        const val END_MAIN_RENDERPASS = 2
        const val BEGIN_RENDERPASS = 3
        const val END_RENDERPASS = 4
        const val SET_VIEWPORT = 5
        const val SET_SCISSOR = 6

        // 32-63: resource binding
        const val SET_PIPELINE = 32
        const val SET_BUFFER = 33
        const val SET_TEXTURE = 34
        const val SET_SAMPLER = 35
        const val SET_IMAGE = 36

        // 64-95: graphics
        const val DRAW = 64
        const val DRAW_INDIRECT = 65

        // 96-127: compute
        const val DISPATCH = 96
        const val DISPATCH_INDIRECT = 97
    }

    private var commands = IntArray(1024)
    private var commandCount = 0
    private val objects = arrayListOf<Any>()
    private val dynamicBuffers = DynamicArray<GLDynamicBuffer>()

    private fun ensureCapacity(count: Int) {
        if (commandCount + count > commands.size) {
            var newSize = commands.size

            while (commandCount + count > newSize)
                newSize *= 2

            commands = commands.copyOf(newSize)
        }
    }

    private fun objectId(value: Any): Int {
        objects += value
        return objects.lastIndex
    }

    override fun beginMainRenderPass(clearColor: Color, clearDepth: Float) {
        ensureCapacity(6)

        commands[commandCount++] = OpCodes.BEGIN_MAIN_RENDERPASS
        commands[commandCount++] = clearColor.r.toBits()
        commands[commandCount++] = clearColor.g.toBits()
        commands[commandCount++] = clearColor.b.toBits()
        commands[commandCount++] = clearColor.a.toBits()
        commands[commandCount++] = clearDepth.toBits()
    }

    override fun endMainRenderPass() {
        ensureCapacity(1)

        commands[commandCount++] = OpCodes.END_MAIN_RENDERPASS
    }

    override fun beginRenderPass(renderPass: GPURenderPass) {
        ensureCapacity(2)

        commands[commandCount++] = OpCodes.BEGIN_RENDERPASS
        commands[commandCount++] = objectId(renderPass)
    }

    override fun endRenderPass() {
        ensureCapacity(1)

        commands[commandCount++] = OpCodes.END_RENDERPASS
    }

    override fun setViewport(x: Int, y: Int, width: Int, height: Int) {
        ensureCapacity(7)

        commands[commandCount++] = OpCodes.SET_VIEWPORT
        commands[commandCount++] = x
        commands[commandCount++] = y
        commands[commandCount++] = width
        commands[commandCount++] = height
    }

    override fun setScissor(x: Int, y: Int, width: Int, height: Int) {
        ensureCapacity(5)

        commands[commandCount++] = OpCodes.SET_SCISSOR
        commands[commandCount++] = x
        commands[commandCount++] = y
        commands[commandCount++] = width
        commands[commandCount++] = height
    }

    override fun setPipeline(pipeline: GPUPipeline) {
        ensureCapacity(2)

        commands[commandCount++] = OpCodes.SET_PIPELINE
        commands[commandCount++] = objectId(pipeline)
    }

    override fun setBuffer(binding: Int, buffer: GPUBuffer) {
        ensureCapacity(3)

        commands[commandCount++] = OpCodes.SET_BUFFER
        commands[commandCount++] = binding
        commands[commandCount++] = objectId(buffer)
    }

    override fun setTexture(binding: Int, texture: GPUTexture) {
        ensureCapacity(3)

        commands[commandCount++] = OpCodes.SET_TEXTURE
        commands[commandCount++] = binding
        commands[commandCount++] = objectId(texture)
    }

    override fun setSampler(binding: Int, sampler: GPUSampler) {
        ensureCapacity(3)

        commands[commandCount++] = OpCodes.SET_SAMPLER
        commands[commandCount++] = binding
        commands[commandCount++] = objectId(sampler)
    }

    override fun setImage(binding: Int, texture: GPUTexture, level: Int, layer: Int, access: GPUImageAccess) {
        ensureCapacity(6)

        commands[commandCount++] = OpCodes.SET_IMAGE
        commands[commandCount++] = binding
        commands[commandCount++] = objectId(texture)
        commands[commandCount++] = level
        commands[commandCount++] = layer
        commands[commandCount++] = access.toGLAccess()
    }

    override fun draw(primitive: Primitive, count: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
        ensureCapacity(6)

        commands[commandCount++] = OpCodes.DRAW
        commands[commandCount++] = primitive.toGLPrimitive()
        commands[commandCount++] = count
        commands[commandCount++] = instanceCount
        commands[commandCount++] = firstVertex
        commands[commandCount++] = firstInstance
    }

    override fun drawIndirect(primitive: Primitive, buffer: GPUBuffer, offset: Int, drawCount: Int, stride: Int) {
        ensureCapacity(6)

        commands[commandCount++] = OpCodes.DRAW_INDIRECT
        commands[commandCount++] = primitive.toGLPrimitive()
        commands[commandCount++] = objectId(buffer)
        commands[commandCount++] = offset
        commands[commandCount++] = drawCount
        commands[commandCount++] = stride
    }

    override fun dispatch(x: Int, y: Int, z: Int) {
        ensureCapacity(4)

        commands[commandCount++] = OpCodes.DISPATCH
        commands[commandCount++] = x
        commands[commandCount++] = y
        commands[commandCount++] = z
    }

    override fun dispatchIndirect(buffer: GPUBuffer, offset: Int) {
        ensureCapacity(3)

        commands[commandCount++] = OpCodes.DISPATCH_INDIRECT
        commands[commandCount++] = objectId(buffer)
        commands[commandCount++] = offset
    }

    fun execute() {
        var index = 0
        var currentPipeline: GPUPipeline? = null
        var currentRenderPass: GLRenderPass? = null

        dynamicBuffers.clear()

        while (index < commandCount) {
            when (commands[index++]) {
                OpCodes.BEGIN_MAIN_RENDERPASS -> {
                    val r = Float.fromBits(commands[index++])
                    val g = Float.fromBits(commands[index++])
                    val b = Float.fromBits(commands[index++])
                    val a = Float.fromBits(commands[index++])
                    val depth = Float.fromBits(commands[index++])

                    GLManager.bindFramebuffer(0)
                    glClearColor(r, g, b, a)
                    glClearDepth(depth.toDouble())
                    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                }

                OpCodes.END_MAIN_RENDERPASS -> {
                    // Nothing to do
                }

                OpCodes.BEGIN_RENDERPASS -> {
                    val pass = objects[commands[index++]] as GLRenderPass

                    GLManager.bindFramebuffer(pass.handle)

                    currentRenderPass = pass

                    glBindFramebuffer(GL_FRAMEBUFFER, pass.handle)

                    stackPush().use { stack ->
                        for (i in pass.colorAttachments.indices) {
                            val attachment = pass.colorAttachments[i]

                            if (attachment.loadOp == GPURenderPass.LoadOp.CLEAR)
                                glClearBufferfv(GL_COLOR, i, stack.floats(attachment.clearColor.r, attachment.clearColor.g, attachment.clearColor.b, attachment.clearColor.a))
                        }

                        val depth = pass.depthAttachment
                        if (depth != null && depth.loadOp == GPURenderPass.LoadOp.CLEAR)
                            glClearBufferfv(GL_DEPTH, 0, stack.floats(depth.clearDepth))

                        val stencil = pass.stencilAttachment
                        if (stencil != null && stencil.loadOp == GPURenderPass.LoadOp.CLEAR)
                            glClearBufferiv(GL_STENCIL, 0, stack.ints(stencil.clearStencil))
                    }
                }

                OpCodes.END_RENDERPASS -> {
                    val pass = currentRenderPass

                    if (pass != null) {
                        val invalidatesSize = pass.colorAttachments.count { it.storeOp == GPURenderPass.StoreOp.DONT_CARE } + (if (pass.depthAttachment?.storeOp == GPURenderPass.StoreOp.DONT_CARE) 1 else 0) + (if (pass.stencilAttachment?.storeOp == GPURenderPass.StoreOp.DONT_CARE) 1 else 0)

                        if (invalidatesSize > 0)
                            stackPush().use { stack ->
                                val invalidates = stack.callocInt(invalidatesSize)

                                for (i in pass.colorAttachments.indices)
                                    if (pass.colorAttachments[i].storeOp == GPURenderPass.StoreOp.DONT_CARE)
                                        invalidates.put(GL_COLOR_ATTACHMENT0 + i)

                                if (pass.depthAttachment?.storeOp == GPURenderPass.StoreOp.DONT_CARE)
                                    invalidates.put(GL_DEPTH_ATTACHMENT)

                                if (pass.stencilAttachment?.storeOp == GPURenderPass.StoreOp.DONT_CARE)
                                    invalidates.put(GL_STENCIL_ATTACHMENT)

                                glInvalidateNamedFramebufferData(pass.handle, invalidates.flip())
                            }

                        currentRenderPass = null
                        GLManager.bindFramebuffer(0)
                    }
                }

                OpCodes.SET_VIEWPORT -> {
                    val x = commands[index++]
                    val y = commands[index++]
                    val width = commands[index++]
                    val height = commands[index++]

                    glViewport(x, y, width, height)
                }

                OpCodes.SET_SCISSOR -> {
                    val x = commands[index++]
                    val y = commands[index++]
                    val width = commands[index++]
                    val height = commands[index++]

                    glScissor(x, y, width, height)
                }

                OpCodes.SET_PIPELINE -> {
                    val pipeline = objects[commands[index++]]

                    when (pipeline) {
                        is GLGraphicsPipeline -> {
                            GLManager.bindProgram(pipeline.shader.handle)

                            glDepthMask(pipeline.depthMask ?: true)
                            glStencilMask(pipeline.stencilMask ?: 0xFFFFFFFF.toInt())

                            val colorMaskR = pipeline.colorMask?.r ?: true
                            val colorMaskG = pipeline.colorMask?.g ?: true
                            val colorMaskB = pipeline.colorMask?.b ?: true
                            val colorMaskA = pipeline.colorMask?.a ?: true
                            glColorMask(colorMaskR, colorMaskG, colorMaskB, colorMaskA)

                            if (pipeline.blendState == null)
                                GLManager.disable(GL_BLEND)
                            else
                                pipeline.blendState.let {
                                    GLManager.enable(GL_BLEND)

                                    val sFactor = when (it.srcFactor) {
                                        GPUGraphicsPipeline.BlendState.Factor.ZERO -> GL_ZERO
                                        GPUGraphicsPipeline.BlendState.Factor.ONE -> GL_ONE
                                        GPUGraphicsPipeline.BlendState.Factor.SOURCE_COLOR -> GL_SRC_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_SOURCE_COLOR -> GL_ONE_MINUS_SRC_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.DEST_COLOR -> GL_DST_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_DEST_COLOR -> GL_ONE_MINUS_DST_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.SOURCE_ALPHA -> GL_SRC_ALPHA
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_SOURCE_ALPHA -> GL_ONE_MINUS_SRC_ALPHA
                                        GPUGraphicsPipeline.BlendState.Factor.DEST_ALPHA -> GL_DST_ALPHA
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_DEST_ALPHA -> GL_ONE_MINUS_DST_ALPHA
                                    }

                                    val dFactor = when (it.destFactor) {
                                        GPUGraphicsPipeline.BlendState.Factor.ZERO -> GL_ZERO
                                        GPUGraphicsPipeline.BlendState.Factor.ONE -> GL_ONE
                                        GPUGraphicsPipeline.BlendState.Factor.SOURCE_COLOR -> GL_SRC_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_SOURCE_COLOR -> GL_ONE_MINUS_SRC_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.DEST_COLOR -> GL_DST_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_DEST_COLOR -> GL_ONE_MINUS_DST_COLOR
                                        GPUGraphicsPipeline.BlendState.Factor.SOURCE_ALPHA -> GL_SRC_ALPHA
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_SOURCE_ALPHA -> GL_ONE_MINUS_SRC_ALPHA
                                        GPUGraphicsPipeline.BlendState.Factor.DEST_ALPHA -> GL_DST_ALPHA
                                        GPUGraphicsPipeline.BlendState.Factor.ONE_MINUS_DEST_ALPHA -> GL_ONE_MINUS_DST_ALPHA
                                    }

                                    glBlendFunc(sFactor, dFactor)

                                    val glEquation = when (it.equation) {
                                        GPUGraphicsPipeline.BlendState.Equation.ADD -> GL_FUNC_ADD
                                        GPUGraphicsPipeline.BlendState.Equation.SUBTRACT -> GL_FUNC_SUBTRACT
                                        GPUGraphicsPipeline.BlendState.Equation.REVERSE_SUBTRACT -> GL_FUNC_REVERSE_SUBTRACT
                                        GPUGraphicsPipeline.BlendState.Equation.MIN -> GL_MIN
                                        GPUGraphicsPipeline.BlendState.Equation.MAX -> GL_MAX
                                    }

                                    glBlendEquation(glEquation)
                                }

                            if (pipeline.depthState == null)
                                GLManager.disable(GL_DEPTH_TEST)
                            else
                                pipeline.depthState.let {
                                    GLManager.enable(GL_DEPTH_TEST)

                                    glDepthRange(it.min.toDouble(), it.max.toDouble())

                                    val func = when (it.func) {
                                        GPUGraphicsPipeline.DepthState.Func.ALWAYS -> GL_ALWAYS
                                        GPUGraphicsPipeline.DepthState.Func.LESS -> GL_LESS
                                        GPUGraphicsPipeline.DepthState.Func.LESS_OR_EQUAL -> GL_LEQUAL
                                        GPUGraphicsPipeline.DepthState.Func.EQUAL -> GL_EQUAL
                                        GPUGraphicsPipeline.DepthState.Func.GREATER_OR_EQUAL -> GL_GEQUAL
                                        GPUGraphicsPipeline.DepthState.Func.GREATER -> GL_GREATER
                                        GPUGraphicsPipeline.DepthState.Func.NOT_EQUAL -> GL_NOTEQUAL
                                        GPUGraphicsPipeline.DepthState.Func.NEVER -> GL_NEVER
                                    }

                                    glDepthFunc(func)
                                }

                            if (pipeline.stencilState == null)
                                GLManager.disable(GL_STENCIL_TEST)
                            else
                                pipeline.stencilState.let {
                                    GLManager.enable(GL_STENCIL_TEST)

                                    val func = when (it.func) {
                                        GPUGraphicsPipeline.StencilState.Func.ALWAYS -> GL_ALWAYS
                                        GPUGraphicsPipeline.StencilState.Func.LESS -> GL_LESS
                                        GPUGraphicsPipeline.StencilState.Func.LEQUAL -> GL_LEQUAL
                                        GPUGraphicsPipeline.StencilState.Func.EQUAL -> GL_EQUAL
                                        GPUGraphicsPipeline.StencilState.Func.GEQUAL -> GL_GEQUAL
                                        GPUGraphicsPipeline.StencilState.Func.GREATER -> GL_GREATER
                                        GPUGraphicsPipeline.StencilState.Func.NOT_EQUAL -> GL_NOTEQUAL
                                        GPUGraphicsPipeline.StencilState.Func.NEVER -> GL_NEVER
                                    }

                                    glStencilFunc(func, it.ref, it.mask)

                                    val sFail = when (it.stencilFail) {
                                        GPUGraphicsPipeline.StencilState.Operation.KEEP -> GL_KEEP
                                        GPUGraphicsPipeline.StencilState.Operation.ZERO -> GL_ZERO
                                        GPUGraphicsPipeline.StencilState.Operation.INCREMENT -> GL_INCR
                                        GPUGraphicsPipeline.StencilState.Operation.DECREMENT -> GL_DECR
                                        GPUGraphicsPipeline.StencilState.Operation.INVERT -> GL_INVERT
                                        GPUGraphicsPipeline.StencilState.Operation.REPLACE -> GL_REPLACE
                                    }

                                    val dFail = when (it.stencilFail) {
                                        GPUGraphicsPipeline.StencilState.Operation.KEEP -> GL_KEEP
                                        GPUGraphicsPipeline.StencilState.Operation.ZERO -> GL_ZERO
                                        GPUGraphicsPipeline.StencilState.Operation.INCREMENT -> GL_INCR
                                        GPUGraphicsPipeline.StencilState.Operation.DECREMENT -> GL_DECR
                                        GPUGraphicsPipeline.StencilState.Operation.INVERT -> GL_INVERT
                                        GPUGraphicsPipeline.StencilState.Operation.REPLACE -> GL_REPLACE
                                    }

                                    val dPass = when (it.stencilFail) {
                                        GPUGraphicsPipeline.StencilState.Operation.KEEP -> GL_KEEP
                                        GPUGraphicsPipeline.StencilState.Operation.ZERO -> GL_ZERO
                                        GPUGraphicsPipeline.StencilState.Operation.INCREMENT -> GL_INCR
                                        GPUGraphicsPipeline.StencilState.Operation.DECREMENT -> GL_DECR
                                        GPUGraphicsPipeline.StencilState.Operation.INVERT -> GL_INVERT
                                        GPUGraphicsPipeline.StencilState.Operation.REPLACE -> GL_REPLACE
                                    }

                                    glStencilOp(sFail, dFail, dPass)
                                }

                            if (pipeline.cullState == null || (!pipeline.cullState.front && !pipeline.cullState.back))
                                GLManager.disable(GL_CULL_FACE)
                            else
                                pipeline.cullState.let {
                                    GLManager.enable(GL_CULL_FACE)

                                    val face = when {
                                        it.front && !it.back -> GL_FRONT
                                        it.back && !it.front -> GL_BACK
                                        it.back && it.front -> GL_FRONT_AND_BACK
                                        else -> GL_NONE
                                    }

                                    glCullFace(face)
                                }

                            currentPipeline = pipeline
                        }

                        is GLComputePipeline -> {
                            GLManager.bindProgram(pipeline.shader.handle)

                            currentPipeline = pipeline
                        }
                    }
                }

                OpCodes.SET_BUFFER -> {
                    val binding = commands[index++]
                    val buffer = objects[commands[index++]] as GLBuffer

                    glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, buffer.handle)

                    if (buffer is GLDynamicBuffer)
                        dynamicBuffers[binding] = buffer
                }

                OpCodes.SET_TEXTURE -> {
                    val binding = commands[index++]
                    val texture = objects[commands[index++]] as GLTexture

                    glBindTextureUnit(binding, texture.handle)
                }

                OpCodes.SET_SAMPLER -> {
                    val binding = commands[index++]
                    val sampler = objects[commands[index++]] as GLSampler

                    glBindSampler(binding, sampler.handle)
                }

                OpCodes.SET_IMAGE -> {
                    val binding = commands[index++]
                    val texture = objects[commands[index++]] as GLTexture
                    val level = commands[index++]
                    val layer = commands[index++]
                    val access = commands[index++]

                    glBindImageTexture(binding, texture.handle, level, false, layer, access, texture.format.toInternalGLFormat())
                }

                OpCodes.DRAW -> {
                    val primitive = commands[index++]
                    val count = commands[index++]
                    val instanceCount = commands[index++]
                    val firstVertex = commands[index++]
                    val firstInstance = commands[index++]

                    glDrawArraysInstancedBaseInstance(primitive, firstVertex, count, instanceCount, firstInstance)
                }

                OpCodes.DRAW_INDIRECT -> {
                    val primitive = commands[index++]
                    val buffer = objects[commands[index++]] as GLBuffer
                    val offset = commands[index++]
                    val drawCount = commands[index++]
                    val stride = commands[index++]

                    GLManager.bindIndirectDrawBuffer(buffer.handle)

                    if (drawCount == 1)
                        glDrawArraysIndirect(primitive, offset.toLong())
                    else
                        glMultiDrawArraysIndirect(primitive, offset.toLong(), drawCount, stride)
                }

                OpCodes.DISPATCH -> {
                    val x = commands[index++]
                    val y = commands[index++]
                    val z = commands[index++]

                    glDispatchCompute(x, y, z)

                    var barrierMask = 0

                    currentPipeline?.let {
                        it.shader.resources.forEach {
                            if (it.type == GPUShader.ResourceType.TEXTURE) {
                                barrierMask = barrierMask or GL_TEXTURE_FETCH_BARRIER_BIT
                                barrierMask = barrierMask or GL_TEXTURE_UPDATE_BARRIER_BIT
                            }

                            if (it.type == GPUShader.ResourceType.IMAGE)
                                barrierMask = barrierMask or GL_SHADER_IMAGE_ACCESS_BARRIER_BIT

                            if (it.type == GPUShader.ResourceType.BUFFER)
                                barrierMask = barrierMask or GL_BUFFER_UPDATE_BARRIER_BIT
                        }
                    }

                    if (barrierMask != 0)
                        glMemoryBarrier(barrierMask)
                }

                OpCodes.DISPATCH_INDIRECT -> {
                    val buffer = objects[commands[index++]] as GLBuffer
                    val offset = commands[index++]

                    GLManager.bindIndirectDispatchBuffer(buffer.handle)

                    glDispatchComputeIndirect(offset.toLong())

                    var barrierMask = 0

                    currentPipeline?.let {
                        it.shader.resources.forEach {
                            if (it.type == GPUShader.ResourceType.TEXTURE) {
                                barrierMask = barrierMask or GL_TEXTURE_FETCH_BARRIER_BIT
                                barrierMask = barrierMask or GL_TEXTURE_UPDATE_BARRIER_BIT
                            }

                            if (it.type == GPUShader.ResourceType.IMAGE)
                                barrierMask = barrierMask or GL_SHADER_IMAGE_ACCESS_BARRIER_BIT

                            if (it.type == GPUShader.ResourceType.BUFFER)
                                barrierMask = barrierMask or GL_BUFFER_UPDATE_BARRIER_BIT
                        }
                    }

                    if (barrierMask != 0)
                        glMemoryBarrier(barrierMask)
                }

                else -> {
                    throw IllegalStateException(
                        "Unknown command opcode: ${commands[index - 1]}"
                    )
                }
            }
        }

        commandCount = 0
        objects.clear()
    }

    override fun dispose() {
        commandCount = 0
        objects.clear()
    }
}