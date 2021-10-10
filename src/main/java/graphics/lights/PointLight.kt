package graphics.lights

import graphics.shaders.ShaderProgram
import math.Color
import math.vectors.Vector3

class PointLight(val color: Color, var position: Vector3) {

    fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.set("pointLight.color", color)
        shaderProgram.set("pointLight.position", position)
    }

}