package graphics.material

import graphics.shaders.ShaderProgram
import math.Color

class ColoredMaterial(private val diffuse: Color, private val specular: Color, private val shininess: Float) : Material() {

    override fun setProperties(shaderProgram: ShaderProgram) {
        shaderProgram.set("material.diffuse", diffuse)
        shaderProgram.set("material.specular", specular)
        shaderProgram.set("material.shininess", shininess)
    }

}