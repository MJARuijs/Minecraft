package graphics.model

import graphics.material.ColoredMaterial
import graphics.material.Material
import graphics.model.mesh.Mesh
import graphics.shaders.ShaderProgram

class Shape(val mesh: Mesh, private val material: Material) {

    fun render(shaderProgram: ShaderProgram, instances: Int = 1) {
        material.setProperties(shaderProgram)
        mesh.draw(instances)
    }

}