package chunks2

import graphics.shaders.ShaderProgram
import math.vectors.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val blocks: ArrayList<BlockData>, private var positionData: FloatArray, private var textureData: IntArray, private var vertexCount: Int) {

    private val hiddenBlocks = ArrayList<BlockData>()
    private var initialized = false

    private lateinit var mesh: ChunkMesh

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

    fun addBlock(position: Vector3, type: BlockType2) {
        for (direction in FaceDirection.values()) {
            if (!containsBlock(position + direction.normal)) {
                for (i in direction.vertices.indices step 3) {
                    positionData += direction.vertices[i] + position.x
                    positionData += direction.vertices[i + 1] + position.y
                    positionData += direction.vertices[i + 2] + position.z
                    textureData += type.textureIndices[FaceDirection.values().indexOf(direction)]
                    vertexCount += 1
                }
            }
        }

        init()
    }

    fun removeBlock(position: Vector3) {
//        blocks.removeIf { block -> block.position == position }
        for (direction in FaceDirection.values()) {
            if (!containsBlock(position + direction.normal)) {
                removeFaceData(position, direction)
            } else {
                println("Contains neighbour ${position + direction.normal}")
            }
        }
        init()
    }

    fun containsBlock(position: Vector3): Boolean {
        return blocks.any { block -> block.position == position } or containsHiddenBlock(position)
    }

    fun containsHiddenBlock(position: Vector3): Boolean {
        return hiddenBlocks.any { block -> block.position == position }
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

    private fun removeFaceData(position: Vector3, direction: FaceDirection) {
        var found = false
        var index = -1
        for (i in positionData.indices step POSITION_INSTANCE_SIZE) {
            for (j in direction.vertices.indices step 3) {
                val x = direction.vertices[j] + position.x
                val y = direction.vertices[j + 1] + position.y
                val z = direction.vertices[j + 2] + position.z

                if (positionData[i + j] != x) break
                if (positionData[i + j + 1] != y) break
                if (positionData[i + j + 2] != z) break

                found = true
                index = i
            }
            if (found) {
                val lastPositionIndex = positionData.size - POSITION_INSTANCE_SIZE
                for (k in index until index + POSITION_INSTANCE_SIZE) {
                    positionData[k] = positionData[lastPositionIndex + k - index]
                }

                val startIndex = (index / POSITION_INSTANCE_SIZE) * TEXTURE_INSTANCE_SIZE
                val lastTextureIndex = textureData.size - TEXTURE_INSTANCE_SIZE
                for (k in startIndex until startIndex + TEXTURE_INSTANCE_SIZE) {
                    textureData[k] = textureData[lastTextureIndex + k - startIndex]
                }
            }
            found = false
        }
    }

    companion object {

        private const val POSITION_INSTANCE_SIZE = 18
        private const val TEXTURE_INSTANCE_SIZE = 6

    }

}