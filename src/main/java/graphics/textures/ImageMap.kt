package graphics.textures

import org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
import org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.opengl.GL45.*
import resources.images.ImageData
import java.lang.Integer.max
import kotlin.math.log2
import kotlin.math.min

class ImageMap(private val image: ImageData): TextureMap {

    override val handle = glCreateTextures(GL_TEXTURE_2D)

    init {
        val size = min(image.width, image.height).toFloat()
        val levels = max(1, log2(size).toInt())

        glTextureStorage2D(handle, levels, GL_RGBA8, image.width, image.height)
        glTextureSubImage2D(handle, 0, 0, 0, image.width, image.height, GL_RGBA, GL_UNSIGNED_BYTE, image.data)

        glTextureParameteri(handle, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTextureParameteri(handle, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTextureParameteri(handle, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTextureParameteri(handle, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glGenerateTextureMipmap(handle)

        val maxAnisotropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT)
        glTextureParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy)
    }

    fun getWidth() = image.width

    fun getHeight() = image.height

    override fun destroy() {
        glDeleteTextures(handle)
    }

}