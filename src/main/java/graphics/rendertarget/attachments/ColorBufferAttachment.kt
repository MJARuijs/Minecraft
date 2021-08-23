package graphics.rendertarget.attachments

import org.lwjgl.opengl.GL11.GL_RGBA8
import org.lwjgl.opengl.GL30.*

class ColorBufferAttachment(val index: Int, width: Int, height: Int, multiSampled: Boolean): Attachment {

    val handle = glGenRenderbuffers()

    init {
        glBindRenderbuffer(GL_RENDERBUFFER, handle)
        if (multiSampled) {
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, 16, GL_RGBA8, width, height)
        } else {
            glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, width, height)
        }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + index, GL_RENDERBUFFER, handle)
    }

    override fun resize(width: Int, height: Int) {}

    override fun matches(other: Any?) = other is ColorBufferAttachment

    override fun destroy() {}

}