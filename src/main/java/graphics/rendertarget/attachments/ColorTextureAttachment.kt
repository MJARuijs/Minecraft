package graphics.rendertarget.attachments

import graphics.textures.ColorMap
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE
import org.lwjgl.opengl.GL32.glFramebufferTexture

class ColorTextureAttachment(val index: Int, width: Int, height: Int, multiSampled: Boolean) : Attachment {

    val colorMap = ColorMap(width, height, multiSampled)

    init {
        if (multiSampled) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + index, GL_TEXTURE_2D_MULTISAMPLE, colorMap.handle, 0)
        } else {
            glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + index, colorMap.handle, 0)
        }

    }

    override val type = AttachmentType.COLOR_TEXTURE

    override fun resize(width: Int, height: Int) = colorMap.resize(width, height)

    override fun matches(other: Any?) = other is ColorTextureAttachment

    override fun destroy() {
        colorMap.destroy()
    }
}