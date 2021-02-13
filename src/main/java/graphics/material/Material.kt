package graphics.material

import graphics.shaders.ShaderProgram

abstract class Material {

    abstract fun setProperties(shaderProgram: ShaderProgram)

}