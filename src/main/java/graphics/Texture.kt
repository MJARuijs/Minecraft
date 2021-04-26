package graphics

import graphics.shaders.ShaderProgram
import math.vectors.Vector2
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture

class Texture(var translation: Vector2, var scale: Vector2) {

    private val quad = Quad()

    fun render(shaderProgram: ShaderProgram, textureId: Int) {

        shaderProgram.start()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)

        shaderProgram.set("translation", translation)
        shaderProgram.set("scale", scale)
        shaderProgram.set("sampler", 0)

        quad.draw()
        shaderProgram.stop()
    }
}