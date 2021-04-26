package graphics.lights

import graphics.shaders.ShaderProgram
import math.Color
import math.vectors.Vector3

class Sun(private val color: Color, val direction: Vector3) {

    fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.set("sun.color", color)
        shaderProgram.set("sun.direction", direction)
    }
}