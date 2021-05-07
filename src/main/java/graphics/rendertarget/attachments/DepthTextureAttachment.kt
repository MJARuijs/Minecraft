package graphics.rendertarget.attachments

import graphics.textures.DepthMap
import org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL32.glFramebufferTexture

class DepthTextureAttachment(width: Int, height: Int) : Attachment {

    val depthMap = DepthMap(width, height)

    init {
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthMap.handle, 0)
    }

    override val type = AttachmentType.DEPTH_TEXTURE

    override fun resize(width: Int, height: Int) = depthMap.resize(width, height)

    override fun matches(other: Any?) = other is DepthTextureAttachment

    override fun destroy() {
        depthMap.destroy()
    }
}