package graphics.textures

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import java.nio.ByteBuffer

class ColorMap(private var width: Int, private var height: Int): TextureMap {

    override val handle = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D, handle)

        val data: ByteBuffer? = null
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun resize(width: Int, height: Int) {

        if (this.width == width && this.height == height) {
            return
        }

        this.width = width
        this.height = height

        glBindTexture(GL_TEXTURE_2D, handle)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun destroy() {
        glDeleteTextures(handle)
    }

}