package chunks2

import graphics.shaders.ShaderProgram
import math.vectors.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val blocks: ArrayList<BlockData>, private var positionData: FloatArray, private var textureData: IntArray, private val vertexCount: Int) {

    private lateinit var mesh: ChunkMesh
    private var initialized = false

    private val hiddenBlocks = ArrayList<BlockData>()

    private fun init() {
        val buffer = ByteBuffer.allocateDirect(vertexCount * 4 * 4).order(ByteOrder.nativeOrder())
        for (i in 0 until vertexCount * 3 step 3) {
            buffer.putFloat(positionData[i])
            buffer.putFloat(positionData[i + 1])
            buffer.putFloat(positionData[i + 2])
            buffer.putInt(textureData[i / 3])
        }
        mesh = ChunkMesh(buffer.rewind(), vertexCount)
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

        val index = getBlockIndex(position)
        println(index)
        if (index != -1) {
            val lastBlockIndex = positionData.size - 3
            for (i in 0 until 3) {
                positionData[index + i] = positionData[lastBlockIndex + i]
            }

            positionData = positionData.sliceArray(0 until positionData.size - 3)

            val lastBlockIndex2 = textureData.size - 1
//            for (i in 0 until 2) {
                textureData[(index / 3) * 2] = textureData[lastBlockIndex2]
//            }

            textureData = textureData.sliceArray(0 until textureData.size - 1)
            init()

        }
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

    private fun getBlockIndex(position: Vector3): Int {
        println("LOOKING FOR $position")
        for (i in positionData.indices step 3) {
            val x = positionData[i]
            val y = positionData[i + 1]
            val z = positionData[i + 2]

            if (position == Vector3(x, y, z)) {
                return i
            }
        }
        return -1
    }
}