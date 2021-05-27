package graphics.rendertarget.attachments

import graphics.textures.DepthMap
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE
import org.lwjgl.opengl.GL32.glFramebufferTexture

class DepthTextureAttachment(width: Int, height: Int, multiSampled: Boolean) : Attachment {

    val depthMap = DepthMap(width, height, multiSampled)

    init {
        if (multiSampled) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D_MULTISAMPLE, depthMap.handle, 0)
        } else {
            glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthMap.handle, 0)
        }

    }

    override val type = AttachmentType.DEPTH_TEXTURE

    override fun resize(width: Int, height: Int) = depthMap.resize(width, height)

    override fun matches(other: Any?) = other is DepthTextureAttachment

    override fun destroy() {
        depthMap.destroy()
    }
}