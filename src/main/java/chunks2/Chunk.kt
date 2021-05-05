package chunks2

import graphics.shaders.ShaderProgram
import java.nio.ByteBuffer

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val blocks: List<BlockData>, private val positionData: ByteBuffer, private val vertexCount: Int) {

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

    fun add(blockData: List<BlockData>, hiddenPositions: FloatArray, hiddenTextures: IntArray) {
        hiddenBlocks += blockData
//        mesh = ChunkMesh.create(blocks + hiddenBlocks)
//        initialized = false
    }

    fun update() {

    }

    fun stopBreaking() {

    }


}