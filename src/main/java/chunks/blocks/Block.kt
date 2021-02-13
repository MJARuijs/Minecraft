package chunks.blocks

import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.MeshCache
import graphics.model.mesh.Primitive

class Block {

    private val mesh = MeshCache.get("models/block.obj")

    fun chunkChanged() {
        mesh.initInstancedBuffers(
                Layout(Primitive.TRIANGLE,
                        Attribute(3, 3),
                        Attribute(4, 1)
                )
        )
    }

    fun render(instances: Int, instancePositions: FloatArray) {
        mesh.updateInstanceData(instancePositions)
        mesh.draw(instances)
    }
}