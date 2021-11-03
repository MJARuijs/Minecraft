package graphics.shadows

import graphics.shaders.ShaderProgram
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL14.GL_DECR_WRAP
import org.lwjgl.opengl.GL14.GL_INCR_WRAP
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.glStencilOpSeparate
import org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP

class StencilShadowRenderer {

    private val outlineProgram = ShaderProgram.load("shaders/adjacency.vert", "shaders/adjacency.geom", "shaders/adjacency.frag")

    fun disableDepthBuffer() {
        glDepthMask(false)
    }

    private fun prepareDepthRender() {
        glDrawBuffer(GL_NONE)
    }

    private fun renderShadowsToStencilBuffer() {
        glDepthMask(false)
        glEnable(GL_DEPTH_CLAMP)
        glDisable(GL_CULL_FACE)

        glStencilFunc(GL_ALWAYS, 0, 0xFF)

        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR_WRAP, GL_KEEP)
        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR_WRAP, GL_KEEP)



        glDisable(GL_DEPTH_CLAMP)
        glEnable(GL_CULL_FACE)
    }

    private fun renderShadowedScene() {
        glDrawBuffer(GL_BACK)

        glStencilFunc(GL_EQUAL, 0x0, 0xFF)
        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_KEEP)
    }

}