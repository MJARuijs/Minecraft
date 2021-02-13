package graphics.lights

import graphics.shaders.ShaderProgram
import math.Color

class AmbientLight(private val color: Color) {

    fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.set("ambient.color", color)
    }

}