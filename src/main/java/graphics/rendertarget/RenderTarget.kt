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

class RenderTarget(
        private var width: Int,
        private var height: Int,
        private val multiSampled: Boolean,
        private val numberOfColorTextures: Int,
        private val numberOfColorBuffers: Int,
        private val numberOfDepthTextures: Int,
        private val numberOfDepthBuffers: Int,
        val handle: Int = glCreateFramebuffers()) {

    private val attachments = ArrayList<Attachment>()
    private var available = true

    init {
        glBindFramebuffer(GL_FRAMEBUFFER, handle)

        var colorCounter = 0

        for (i in 0 until numberOfColorTextures) {
            attachments += ColorTextureAttachment(colorCounter++, width, height, multiSampled)
        }

        for (i in 0 until numberOfColorBuffers) {
            attachments += ColorBufferAttachment(colorCounter++, width, height, multiSampled)
        }

        for (i in 0 until numberOfDepthTextures) {
            attachments += DepthTextureAttachment(width, height, multiSampled)
        }

        for (i in 0 until numberOfDepthBuffers) {
            attachments += DepthBufferAttachment(width, height, multiSampled)
        }

        val drawBuffers = createIntBuffer(numberOfColorTextures + numberOfColorBuffers)

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

    fun isAvailable() = available

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

    fun clearColor() {
        glClear(GL_COLOR_BUFFER_BIT)
    }

    fun clearDepth() {
        glClear(GL_DEPTH_BUFFER_BIT)
    }

    fun clear() {
        clearColor()
        clearDepth()
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

    fun renderToScreen(buffers: Int) = renderTo(0, buffers)

    fun matches(width: Int, height: Int, multiSampled: Boolean, requiredNumberOfColorTextures: Int, requiredNumberOfColorBuffers: Int, requiredNumberOfDepthTextures: Int, requiredNumberOfDepthBuffers: Int): Boolean {
        if (width != this.width) return false
        if (height != this.height) return false
        if (multiSampled != this.multiSampled) return false

        if (numberOfColorTextures != requiredNumberOfColorTextures) return false
        if (numberOfColorBuffers != requiredNumberOfColorBuffers) return false
        if (numberOfDepthTextures != requiredNumberOfDepthTextures) return false
        if (numberOfDepthBuffers != requiredNumberOfDepthBuffers) return false

        return true
    }

    fun destroy() {
        attachments.forEach(Attachment::destroy)
        glDeleteFramebuffers(handle)
    }
}