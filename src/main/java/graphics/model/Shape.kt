package graphics.model

import graphics.material.Material
import graphics.model.mesh.Mesh
import graphics.shaders.ShaderProgram

class Shape(val mesh: Mesh, private val material: Material) {

    fun render(shaderProgram: ShaderProgram) {
        material.setProperties(shaderProgram)
        mesh.draw()
    }

}