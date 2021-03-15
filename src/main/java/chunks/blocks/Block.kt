package chunks.blocks

import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.MeshCache
import graphics.model.mesh.Primitive

class Block {

    private val mesh = MeshCache.get("models/block.obj")
    private val unTexturedMesh = MeshCache.get("models/untexturedBlock.obj")

    fun initAttributes() {
        mesh.initInstancedBuffers(
                Layout(Primitive.TRIANGLE,
                        Attribute(3, 3),
                        Attribute(4, 2),
                        Attribute(5, 2),
                        Attribute(6, 2),
                        Attribute(7, 2),
                        Attribute(8, 2),
                        Attribute(9, 2)
                )
        )

        unTexturedMesh.initInstancedBuffers(
                Layout(Primitive.TRIANGLE,
                        Attribute(1, 3)
                )
        )
    }

    fun render(instances: Int, instancePositions: FloatArray) {
        mesh.updateInstanceData(instancePositions)
        mesh.draw(instances)
    }

    fun renderUnTextured(instances: Int, instancePositions: FloatArray) {
        unTexturedMesh.updateInstanceData(instancePositions)
        unTexturedMesh.draw(instances)
    }
}