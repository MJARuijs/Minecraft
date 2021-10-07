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

    override fun resize(width: Int, height: Int) = depthMap.resize(width, height)

    override fun destroy() {
        depthMap.destroy()
    }
}