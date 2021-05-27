package graphics.rendertarget

import graphics.rendertarget.attachments.*
import graphics.textures.ColorMap
import graphics.textures.DepthMap
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL45.glBlitNamedFramebuffer
import org.lwjgl.opengl.GL45.glCreateFramebuffers

class RenderTarget(private var width: Int, private var height: Int, private val multiSampled: Boolean, vararg types: AttachmentType, private val handle: Int = glCreateFramebuffers()) {

    private val attachments = ArrayList<Attachment>()
    private var available = true

    init {
        glBindFramebuffer(GL_FRAMEBUFFER, handle)

        var colorCounter = 0

        for (attachmentType in types) {
            attachments += when (attachmentType) {
                AttachmentType.COLOR_TEXTURE -> ColorTextureAttachment(colorCounter++, width, height, multiSampled)
                AttachmentType.COLOR_BUFFER -> ColorBufferAttachment(colorCounter++, width, height, multiSampled)
                AttachmentType.DEPTH_TEXTURE -> DepthTextureAttachment(width, height, multiSampled)
                AttachmentType.DEPTH_BUFFER -> DepthBufferAttachment(width, height, multiSampled)
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
        glViewport(0, 0, width, height)
        glBindFramebuffer(GL_FRAMEBUFFER, handle)
        available = false
    }

    fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    fun stop() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        available = true
    }

    private fun renderTo(targetHandle: Int, buffers: Int) {
        if (handle == targetHandle) {
            return
        }

        glBlitNamedFramebuffer(handle, targetHandle, 0, 0, width, height, 0, 0, width, height, buffers, GL_NEAREST)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun renderTo(renderTarget: RenderTarget, buffers: Int) = renderTo(renderTarget.handle, buffers)

    fun matches(width: Int, height: Int, multiSampled: Boolean, vararg requiredTypes: AttachmentType): Boolean {

        if (width != this.width) return false
        if (height != this.height) return false

        if (multiSampled != this.multiSampled) return false

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