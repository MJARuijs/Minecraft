package graphics.material

import graphics.shaders.ShaderProgram
import math.Color
import math.vectors.Vector4

class ColoredMaterial(private val diffuse: Color, private val specular: Color, private val shininess: Float) : Material() {

    override fun setProperties(shaderProgram: ShaderProgram) {
        shaderProgram.set("material.diffuse", diffuse)
        shaderProgram.set("material.specular", Vector4(1.0f, 1.0f, 1.0f,1.0f))
        shaderProgram.set("material.shininess", shininess)
    }

}