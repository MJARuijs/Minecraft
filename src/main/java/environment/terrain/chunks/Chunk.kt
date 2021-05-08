package environment.terrain.chunks

import environment.terrain.Biome
import environment.terrain.blocks.BlockData
import environment.terrain.blocks.BlockType
import graphics.shaders.ShaderProgram
import math.vectors.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, private val blocks: ArrayList<BlockData>, private var positionData: FloatArray, private var textureData: IntArray, private var vertexCount: Int) {

    private val hiddenBlocks = ArrayList<BlockData>()
    private var initialized = false

    private lateinit var mesh: ChunkMesh

    private fun init() {
        mesh = ChunkMesh(bufferData(), vertexCount)
        initialized = true
    }

    private fun updateMesh() {
        mesh.updateInstanceData(bufferData(), vertexCount)
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

    fun containsBlock(position: Vector3): Boolean {
        return containsVisibleBlock(position) or containsHiddenBlock(position)
    }

    private fun containsVisibleBlock(position: Vector3): Boolean {
        return blocks.any { block -> block.position == position }
    }

    private fun containsHiddenBlock(position: Vector3): Boolean {
        return hiddenBlocks.any { block -> block.position == position }
    }

    private fun getVisibleBlock(position: Vector3, remove: Boolean): BlockData? {
        val blockData = blocks.find { block -> block.position == position }
        if (remove) {
            blocks.removeIf { block -> block.position == position }
        }
        return blockData
    }

    private fun getHiddenBlock(position: Vector3, remove: Boolean): BlockData? {
        val blockData = hiddenBlocks.find { block -> block.position == position }
        if (remove) {
            hiddenBlocks.removeIf { block -> block.position == position }
        }
        return blockData
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

    private fun bufferData(): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(vertexCount * 4 * 4).order(ByteOrder.nativeOrder())
        for (i in 0 until vertexCount * 3 step 3) {
            buffer.putFloat(positionData[i])
            buffer.putFloat(positionData[i + 1])
            buffer.putFloat(positionData[i + 2])
            buffer.putInt(textureData[i / 3])
        }
        return buffer
    }

    fun addBlock(position: Vector3, type: BlockType) {
        blocks += BlockData(type, position)

        for (direction in FaceDirection.values()) {
            if (containsBlock(position + direction.normal)) {
                removeFaceData(position + direction.normal, direction.getOpposite())
            }

            if (!containsBlock(position + direction.normal)) {
                for (i in direction.vertices.indices step 3) {
                    positionData += direction.vertices[i] + position.x
                    positionData += direction.vertices[i + 1] + position.y
                    positionData += direction.vertices[i + 2] + position.z
                    textureData += type[direction]
                    vertexCount += 1
                }
            }
        }

    updateMesh()
    }

    fun removeBlock(position: Vector3) {
        blocks.removeIf { block -> block.position == position }

        for (direction in FaceDirection.values()) {
            if (!containsBlock(position + direction.normal)) {
                removeFaceData(position, direction)
            } else {
                if (containsHiddenBlock(position + direction.normal)) {
                    val block = getHiddenBlock(position + direction.normal, true)!!
                    blocks += block
                    addFaceData(block, direction)
                } else if (containsVisibleBlock(position + direction.normal)) {
                    val block = getVisibleBlock(position + direction.normal, false)!!
                    addFaceData(block, direction)
                }
            }
        }
        updateMesh()
    }

    private fun addFaceData(block: BlockData, direction: FaceDirection) {
        for (i in direction.vertices.indices step 3) {
            positionData += block.position.x + direction.getOpposite().vertices[i]
            positionData += block.position.y + direction.getOpposite().vertices[i + 1]
            positionData += block.position.z + direction.getOpposite().vertices[i + 2]
            textureData += block.type[direction.getOpposite()]
            vertexCount += 1
        }
    }

    private fun removeFaceData(position: Vector3, direction: FaceDirection) {
        val indices = getIndices(position, direction)

        for (index in indices) {
            removeFaceData(index)
        }
    }

    private fun removeFaceData(index: Int) {
        val lastPositionIndex = positionData.size - POSITION_INSTANCE_SIZE
        for (k in index until index + POSITION_INSTANCE_SIZE) {
            positionData[k] = positionData[lastPositionIndex + k - index]
        }

        positionData = positionData.sliceArray(0 until positionData.size - POSITION_INSTANCE_SIZE)

        val startIndex = (index / POSITION_INSTANCE_SIZE) * TEXTURE_INSTANCE_SIZE
        val lastTextureIndex = textureData.size - TEXTURE_INSTANCE_SIZE
        for (k in startIndex until startIndex + TEXTURE_INSTANCE_SIZE) {
            textureData[k] = textureData[lastTextureIndex + k - startIndex]
        }

        textureData = textureData.sliceArray(0 until textureData.size - TEXTURE_INSTANCE_SIZE)
        vertexCount -= 6
    }

    private fun getIndices(position: Vector3, direction: FaceDirection): IntArray {
        var indices = IntArray(0)

        loop@for (i in positionData.indices step POSITION_INSTANCE_SIZE) {
            for (j in direction.vertices.indices step 3) {
                val x = direction.vertices[j] + position.x
                val y = direction.vertices[j + 1] + position.y
                val z = direction.vertices[j + 2] + position.z

                if (positionData[i + j] != x) continue@loop
                if (positionData[i + j + 1] != y) continue@loop
                if (positionData[i + j + 2] != z) continue@loop

            }
            indices += i
        }
        return indices
    }

    companion object {

        private const val POSITION_INSTANCE_SIZE = 18
        private const val TEXTURE_INSTANCE_SIZE = 6

    }

}