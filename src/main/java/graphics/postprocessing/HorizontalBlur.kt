package graphics.postprocessing

import graphics.model.Quad
import graphics.rendertarget.RenderTarget
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram

class HorizontalBlur(private val strength: Float) {

    private val shaderProgram = ShaderProgram.load("shaders/postprocessing/gaussian_horizontal_blur.vert", "shaders/postprocessing/gaussian_blur.frag")
    private val quad = Quad()
    private val sampler = Sampler(0)

    fun apply(source: RenderTarget, destination: RenderTarget) {
        destination.start()
        destination.clear()

        shaderProgram.start()
        shaderProgram.set("sampler", sampler.index)
        shaderProgram.set("strength", strength)

        sampler.bind(source.getColorMap(0))

        quad.draw()

        shaderProgram.stop()
        destination.stop()
    }
}