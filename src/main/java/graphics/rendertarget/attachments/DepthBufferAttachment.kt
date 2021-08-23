package graphics.rendertarget.attachments

import org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT
import org.lwjgl.opengl.GL30.*

class DepthBufferAttachment(width: Int, height: Int, multiSampled: Boolean) : Attachment {

    val handle = glGenRenderbuffers()

    init {
        glBindRenderbuffer(GL_RENDERBUFFER, handle)
        if (multiSampled) {
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, 16, GL_DEPTH_COMPONENT, width, height)
        } else {
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height)
        }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, handle)
    }

    override fun resize(width: Int, height: Int) {
        glBindRenderbuffer(GL_RENDERBUFFER, handle)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, handle)
    }

    override fun matches(other: Any?) = other is DepthBufferAttachment

    override fun destroy() {
        glDeleteRenderbuffers(handle)
    }
}