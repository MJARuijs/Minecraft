package chunks2

import chunks.Biome
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Primitive
import graphics.shaders.ShaderProgram

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, visibleBlocks: FloatArray) {

    private val mesh = ChunkMesh(
            Layout(Primitive.TRIANGLE,
                    Attribute(0, 3),
                    Attribute(1, 1)
            ),
            visibleBlocks
    )

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("overlayColor", biome.overlayColor)
        mesh.draw()
    }

    fun update() {

    }

    fun stopBreaking() {

    }

}