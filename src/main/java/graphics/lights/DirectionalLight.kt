package graphics.lights

import graphics.shaders.ShaderProgram
import math.Color
import math.vectors.Vector3

class DirectionalLight(private val color: Color, private val direction: Vector3) {

    fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.set("directional.color", color)
        shaderProgram.set("directional.direction", direction)
    }
}