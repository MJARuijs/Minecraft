package graphics.rendertarget

import graphics.textures.ColorMap
import graphics.textures.DepthMap
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.glFramebufferTexture

class RenderTarget(private var width: Int, private var height: Int, val handle: Int = glGenFramebuffers()) {

    private val colorTexture = ColorMap(width, height)
    private val depthTexture = DepthMap(width, height)

    init {
        glBindFramebuffer(GL_FRAMEBUFFER, handle)
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorTexture.handle, 0)
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture.handle, 0)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun getWidth() = width

    fun getHeight() = height

    fun getAspectRatio() = width.toFloat() / height.toFloat()

    fun getColorTexture() = colorTexture

    fun getDepthTexture() = depthTexture

    fun start() {
        glBindFramebuffer(GL_FRAMEBUFFER, handle)
        glViewport(0, 0, width, height)
    }

    fun clear() {
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }

    fun stop() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
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

    fun renderToScreen() = renderTo(0)

    fun destroy() {
        depthTexture.destroy()
        colorTexture.destroy()
        glDeleteFramebuffers(handle)
    }
}