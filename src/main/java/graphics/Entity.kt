package graphics

import graphics.model.Model
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Entity(val model: Model, var transformation: Matrix4) {

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", transformation)

        model.render(shaderProgram)
    }

}