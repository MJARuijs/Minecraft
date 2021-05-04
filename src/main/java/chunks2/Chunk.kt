package chunks2

import chunks.Biome
import chunks.blocks.BlockData
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Primitive
import graphics.shaders.ShaderProgram
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glBindAttribLocation

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val blocks: List<BlockData>) {

    private val mesh = ChunkMesh.create(blocks)

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("overlayColor", biome.overlayColor)

        mesh.draw()
    }

    fun update() {

    }

    fun stopBreaking() {

    }

}