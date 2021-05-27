package graphics.textures

import org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
import org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL45.*
import org.lwjgl.opengl.GL46.GL_MAX_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.GL46.GL_TEXTURE_MAX_ANISOTROPY
import resources.images.ImageData
import java.lang.Integer.max
import kotlin.math.log2
import kotlin.math.min

class TextureArray(val textures: List<ImageData>) {

    val handle = glCreateTextures(GL_TEXTURE_2D_ARRAY)

    init {
        val width = textures[0].width
        val height = textures[0].height

        val size = min(width, height).toFloat()
        val levels = max(1, log2(size).toInt())
        glTextureStorage3D(handle, levels, GL_RGBA8, width, height, textures.size)

        for (i in textures.indices) {
            val texture = textures[i]
            glTextureSubImage3D(handle, 0, 0, 0, i, texture.width, texture.height, 1, GL_RGBA, GL_UNSIGNED_BYTE, texture.data)
        }

        glTextureParameteri(handle, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTextureParameteri(handle, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTextureParameteri(handle, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTextureParameteri(handle, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        val maxAnisotropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT)
        glTextureParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy)

        glGenerateTextureMipmap(handle)
    }

}