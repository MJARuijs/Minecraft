package chunks2

import graphics.shaders.ShaderProgram
import math.vectors.Vector3
import java.nio.ByteBuffer

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val blocks: ArrayList<BlockData>, private val positionData: ByteBuffer, private val vertexCount: Int) {

    private lateinit var mesh: ChunkMesh
    private var initialized = false

    private val hiddenBlocks = ArrayList<BlockData>()

    private fun init() {
        mesh = ChunkMesh(positionData, vertexCount)
        initialized = true
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("overlayColor", biome.overlayColor)

        if (initialized) {
            mesh.draw()
        } else {
            init()
            mesh.draw()
        }
    }

    fun removeBlock(position: Vector3) {
        blocks.removeIf { block -> block.position == position }



    }

    fun containsBlock(position: Vector3): Boolean {
        return blocks.any { block -> block.position == position }
    }

    fun getBlocksNearPosition(position: Vector3, maxDistance: Float): List<Vector3> {
        val blocksPositions = ArrayList<Vector3>()
        for (block in blocks) {
            if ((block.position - position).length() < maxDistance) {
                blocksPositions += block.position
            }
        }
        return blocksPositions
    }

    fun add(blockData: List<BlockData>) {
        hiddenBlocks += blockData
    }

    fun update() {

    }

    fun stopBreaking() {

    }


}