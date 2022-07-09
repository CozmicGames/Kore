package com.cozmicgames.graphics.gpu

import com.cozmicgames.utils.Disposable

interface Framebuffer : Disposable {
    enum class Attachment {
        COLOR0,
        COLOR1,
        COLOR2,
        COLOR3,
        COLOR4,
        COLOR5,
        COLOR6,
        COLOR7,
        DEPTH,
        STENCIL
    }

    val width: Int
    val height: Int

    val attachments: Iterable<Attachment>

    fun addAttachment(attachment: Attachment, format: Texture.Format, sampler: Sampler)
    fun addAttachment(attachment: Attachment, texture: Texture2D)
    fun removeAttachment(attachment: Attachment)
    fun update(width: Int, height: Int)

    operator fun get(attachment: Attachment): Texture2D?
}