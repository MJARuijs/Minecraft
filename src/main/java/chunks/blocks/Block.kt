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
                        Attribute(4, 3),
                        Attribute(5, 3),
                        Attribute(6, 3),
                        Attribute(7, 3),
                        Attribute(8, 3),
                        Attribute(9, 3)
                )
        )

        unTexturedMesh.initInstancedBuffers(
                Layout(Primitive.TRIANGLE,
                        Attribute(1, 3)
                )
        )
    }

    fun update(instancePositions: FloatArray) {
        mesh.updateInstanceData(instancePositions)
    }
    
    fun render(instances: Int, instancePositions: FloatArray) {
        update(instancePositions)
        mesh.draw(instances)
    }

    fun renderUnTextured(instances: Int, instancePositions: FloatArray) {
        unTexturedMesh.updateInstanceData(instancePositions)
        unTexturedMesh.draw(instances)
    }
}