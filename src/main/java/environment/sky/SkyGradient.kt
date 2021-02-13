package environment.sky

import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.Quad
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap

class SkyGradient(private val texture: ImageMap) {

    private val shaderProgram = ShaderProgram.load("shaders/sky/gradient.vert", "shaders/sky/gradient.frag")
    private val sampler = Sampler(0)
    private val quad = Quad()

    fun render() {
        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING)

        shaderProgram.start()
        shaderProgram.set("gradient", sampler.index)

        sampler.bind(texture)
        quad.draw()

        shaderProgram.stop()

        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING)
    }

    fun destroy() {
        quad.destroy()
        texture.destroy()
        shaderProgram.destroy()
    }

}