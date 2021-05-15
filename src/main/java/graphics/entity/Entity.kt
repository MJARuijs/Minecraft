package graphics.entity

import graphics.model.Model
import graphics.renderer.Renderable
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Entity(val model: Model, var transformation: Matrix4): Renderable {

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", transformation)

        model.render(shaderProgram)
    }

}