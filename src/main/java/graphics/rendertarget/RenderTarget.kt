package graphics.rendertarget

import graphics.rendertarget.attachments.*
import graphics.textures.ColorMap
import graphics.textures.DepthMap
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL30.*

class RenderTarget(private var width: Int, private var height: Int, vararg attachmentTypes: AttachmentType, val handle: Int = glGenFramebuffers()) {

    private val attachments = ArrayList<Attachment>()
    private var available = true

    init {
        glBindFramebuffer(GL_FRAMEBUFFER, handle)

        var colorCounter = 0

        for (type in attachmentTypes) {
            attachments += when (type) {
                AttachmentType.COLOR_TEXTURE -> ColorTextureAttachment(colorCounter++, width, height)
                AttachmentType.DEPTH_TEXTURE -> DepthTextureAttachment(width, height)
                AttachmentType.DEPTH_BUFFER -> DepthBufferAttachment(width, height)
            }
        }

        val drawBuffers = createIntBuffer(colorCounter)

        for (i in 0 until colorCounter) {
            drawBuffers.put(GL_COLOR_ATTACHMENT0 + i)
        }

        drawBuffers.flip()
        glDrawBuffers(drawBuffers)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun getWidth() = width

    fun getHeight() = height

    fun getAspectRatio() = width.toFloat() / height.toFloat()

    fun getColorMap(index: Int = 0): ColorMap {
        attachments.forEach {
            if (it is ColorTextureAttachment) {
                if (it.index == index) {
                    return it.colorMap
                }
            }
        }

        throw IllegalArgumentException("This RenderTarget does not have a ColorTexture Attachment that matches the requested index!")
    }

    fun getDepthTexture(): DepthMap {
        attachments.forEach {
            if (it is DepthTextureAttachment) {
                return it.depthMap
            }
        }

        throw IllegalArgumentException("This RenderTarget does not have a DepthTexture Attachment!")
    }

    fun start() {
        glBindFramebuffer(GL_FRAMEBUFFER, handle)
        glViewport(0, 0, width, height)
        available = false
    }

    fun clear() {
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    fun stop() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        available = true
    }

    fun renderTo(target: RenderTarget) = renderTo(target.handle)

    private fun renderTo(targetId: Int) {
        if (handle == targetId) {
            return
        }

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, targetId)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, handle)
        glReadBuffer(GL_COLOR_ATTACHMENT0)
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT, GL_NEAREST)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun renderTo(targetID: Int, colorBuffer: Int = 0) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, targetID)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, handle)

        glReadBuffer(GL_COLOR_ATTACHMENT0 + colorBuffer)

        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT, GL_NEAREST)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun renderToScreen() = renderTo(0)

    fun matches(width: Int, height: Int, vararg requiredTypes: AttachmentType): Boolean {

        if (width != this.width) return false
        if (height != this.height) return false

        if (requiredTypes.size != attachments.size) return false

        val requestedColorAttachments = requiredTypes.count { it == AttachmentType.COLOR_TEXTURE }
        val availableColorAttachments = attachments.count { it.type == AttachmentType.COLOR_TEXTURE }

        if (availableColorAttachments != requestedColorAttachments) return false

        for (type in requiredTypes) {
            attachments.find { it.type == type } ?: return false
        }

        for (attachment in attachments) {
            requiredTypes.find { it == attachment.type } ?: return false
        }

        return true
    }

    fun destroy() {
        attachments.forEach(Attachment::destroy)
        glDeleteFramebuffers(handle)
    }
}