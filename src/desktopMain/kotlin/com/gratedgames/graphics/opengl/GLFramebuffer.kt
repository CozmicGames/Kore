package com.gratedgames.graphics.opengl

import com.gratedgames.Kore
import com.gratedgames.graphics
import com.gratedgames.graphics.DesktopStatistics
import com.gratedgames.graphics.gpu.Framebuffer
import com.gratedgames.graphics.gpu.Texture
import com.gratedgames.graphics.gpu.Texture2D
import com.gratedgames.log
import com.gratedgames.memory.Memory
import com.gratedgames.memory.of
import com.gratedgames.utils.Disposable
import com.gratedgames.utils.use
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GL32C.glFramebufferTexture
import java.util.*

class GLFramebuffer : Framebuffer {
    private inner class AttachmentObject(val texture: Texture2D) : Disposable {
        constructor(format: Texture.Format) : this(Kore.graphics.createTexture2D(format))

        fun update(width: Int, height: Int) {
            texture.setSize(width, height)
        }

        override fun dispose() {
            texture.dispose()
        }
    }

    override val attachments: Iterable<Framebuffer.Attachment> = arrayListOf()

    override var width = 0
        private set

    override var height = 0
        private set

    var handle = 0
        private set

    private val attachmentObjects = EnumMap<Framebuffer.Attachment, AttachmentObject>(Framebuffer.Attachment::class.java)

    init {
        DesktopStatistics.numFramebuffers++
    }

    override fun addAttachment(attachment: Framebuffer.Attachment, format: Texture.Format) {
        attachmentObjects.put(attachment, AttachmentObject(format))?.dispose()
    }

    override fun addAttachment(attachment: Framebuffer.Attachment, texture: Texture2D) {
        attachmentObjects.put(attachment, AttachmentObject(texture))?.dispose()
    }

    override fun removeAttachment(attachment: Framebuffer.Attachment) {
        attachmentObjects.remove(attachment)?.dispose()
    }

    override fun get(attachment: Framebuffer.Attachment): Texture2D? {
        return attachmentObjects[attachment]?.texture
    }

    override fun update(width: Int, height: Int) {
        if (attachmentObjects.isEmpty())
            return

        this.width = width
        this.height = height

        if (glIsFramebuffer(handle))
            glDeleteFramebuffers(handle)

        handle = glGenFramebuffers()

        tempBind {
            val drawBuffers = arrayListOf<Int>()

            for ((attachment, obj) in attachmentObjects) {
                val glAttachment = when (attachment) {
                    Framebuffer.Attachment.COLOR0 -> GL_COLOR_ATTACHMENT0
                    Framebuffer.Attachment.COLOR1 -> GL_COLOR_ATTACHMENT1
                    Framebuffer.Attachment.COLOR2 -> GL_COLOR_ATTACHMENT2
                    Framebuffer.Attachment.COLOR3 -> GL_COLOR_ATTACHMENT3
                    Framebuffer.Attachment.COLOR4 -> GL_COLOR_ATTACHMENT4
                    Framebuffer.Attachment.COLOR5 -> GL_COLOR_ATTACHMENT5
                    Framebuffer.Attachment.COLOR6 -> GL_COLOR_ATTACHMENT6
                    Framebuffer.Attachment.COLOR7 -> GL_COLOR_ATTACHMENT7
                    Framebuffer.Attachment.DEPTH -> GL_DEPTH_ATTACHMENT
                    Framebuffer.Attachment.STENCIL -> GL_STENCIL_ATTACHMENT
                }

                if (attachment != Framebuffer.Attachment.DEPTH && attachment != Framebuffer.Attachment.STENCIL)
                    drawBuffers += glAttachment

                obj.texture.setSize(width, height)

                glFramebufferTexture(GL_FRAMEBUFFER, glAttachment, (obj.texture as GLTexture2D).handle, 0)
            }

            GLManager.checkErrors {
                if (drawBuffers.isEmpty())
                    glDrawBuffer(GL_NONE)
                else
                    Memory.of(*drawBuffers.toIntArray()).use {
                        nglDrawBuffers(drawBuffers.size, it.address)
                    }
            }

            when (glCheckFramebufferStatus(GL_FRAMEBUFFER)) {
                GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> Kore.log.fail(this::class, "Framebuffer attachment incomplete")
                GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> Kore.log.fail(this::class, "Framebuffer attachment missing")
                GL_FRAMEBUFFER_UNSUPPORTED -> Kore.log.fail(this::class, "Framebuffers are unsupported")
            }
        }
    }

    override fun dispose() {
        for ((_, obj) in attachmentObjects)
            obj.dispose()

        glDeleteFramebuffers(handle)

        DesktopStatistics.numFramebuffers--
    }
}

fun GLFramebuffer.tempBind(block: () -> Unit) {
    val previous = GLManager.boundFramebuffer
    GLManager.bindFramebuffer(handle)
    block()
    GLManager.bindFramebuffer(previous)
}