package graphics.model

import graphics.model.mesh.Mesh
import graphics.shaders.ShaderProgram
import resources.Resource

open class Model(private val shapes: List<Shape>): Resource {

    fun render(shaderProgram: ShaderProgram) {
        for (shape in shapes) {
            shape.render(shaderProgram)
        }
    }

    override fun destroy() {
        shapes.map(Shape::mesh).distinct().forEach(Mesh::destroy)
    }

}