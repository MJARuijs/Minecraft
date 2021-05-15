package graphics.test

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.opengl.GL42.glTexStorage3D
import resources.images.ImageData

class TextureArray(val textures: List<ImageData>) {

    val handle = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D_ARRAY, handle)
        glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, textures[0].width, textures[0].height, textures.size)

        for (i in textures.indices) {
            val texture = textures[i]
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, texture.width, texture.height, 1, GL_RGBA, GL_UNSIGNED_BYTE, texture.data)
        }

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameterf(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_LOD_BIAS, 0f)

        glGenerateMipmap(GL_TEXTURE_2D_ARRAY)

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0)

    }

}