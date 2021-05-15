package graphics.renderer

import graphics.shaders.ShaderProgram

interface Renderable {

    fun render(shaderProgram: ShaderProgram)

}