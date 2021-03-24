package chunks

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.ChunkGenerator.Companion.HALF_CHUNK_SIZE
import chunks.ChunkGenerator.Companion.MAX_HEIGHT
import chunks.blocks.Block
import chunks.blocks.BlockType
import chunks.blocks.Face
import graphics.shaders.ShaderProgram
import math.vectors.Vector2
import math.vectors.Vector3
import kotlin.math.roundToInt

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, private var visibleInstanceData: FloatArray, private var highestBlock: Int = 0) {

    private val instanceSize = 21

    private var untexturedFloatData = FloatArray(0)
    private var hiddenInstanceData = FloatArray(0)

    private val subsetBlockPositions = ArrayList<Vector3>()

    private var initialized = false

    private lateinit var block: Block

    private fun initBlock() {
        block = Block()
        block.initAttributes()
        initialized = true
    }

    fun getSubsetSize() = subsetBlockPositions.size

    fun getCenter() = Vector2(chunkX + CHUNK_SIZE / 2, chunkZ + CHUNK_SIZE / 2)

    fun getPosition() = Vector2(chunkX, chunkZ)

    fun getSubsetPosition(i: Int) = subsetBlockPositions[i]

    fun containsBlock(position: Vector3) = containsBlock(position.x.roundToInt(), position.y.roundToInt(), position.z.roundToInt())

    private fun containsBlock(x: Int, y: Int, z: Int): Boolean {
        if (y < 0 || y > MAX_HEIGHT) {
            return false
        }
        if (x < chunkX - HALF_CHUNK_SIZE|| x >= chunkX + HALF_CHUNK_SIZE) {
            return false
        }
        if (z < chunkZ - HALF_CHUNK_SIZE || z >= chunkZ + HALF_CHUNK_SIZE) {
            return false
        }
        return true
    }

    fun add(newData: FloatArray) {
        hiddenInstanceData += newData
    }

    fun addBlock(type: BlockType, position: Vector3) {
        if (containsBlock(position) && position.y >= 0 && position.y < MAX_HEIGHT) {
            val newBlock = Pair(type, position)

            if (position.y.toInt() >= highestBlock) {
                highestBlock = position.y.toInt()
            }

            addBlockData(newBlock)
            removeSurroundingBlocks(position)
        }
    }

    fun removeBlock(position: Vector3) {
        val index = getBlockIndex(position, visibleInstanceData)
        if (index != -1) {
            val lastBlockIndex = visibleInstanceData.size - instanceSize
            for (i in 0 until instanceSize) {
                visibleInstanceData[index + i] = visibleInstanceData[lastBlockIndex + i]
            }

            visibleInstanceData = visibleInstanceData.sliceArray(0 until visibleInstanceData.size - instanceSize)
        }

        addSurroundingBlocks(position)
    }

    private fun addBlockData(block: Pair<BlockType, Vector3>) {
        visibleInstanceData += block.second.toArray()
        visibleInstanceData += block.first.getOffsets()

        untexturedFloatData += block.second.toArray()
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("overlayColor", biome.overlayColor)
        if (initialized) {
            block.render(visibleInstanceData.size / instanceSize, visibleInstanceData)
        } else {
            initBlock()
            block.render(visibleInstanceData.size / instanceSize, visibleInstanceData)
        }
    }

    fun renderSubset() {
        val subsetData = FloatArray(subsetBlockPositions.size * 3)
        var i = 0
        for (block in subsetBlockPositions) {
            subsetData[i] = block.x
            subsetData[i + 1] = block.y
            subsetData[i + 2] = block.z
            i += 3
        }
        if (initialized) {
            block.renderUnTextured(subsetData.size / 3, subsetData)
        } else {
            initBlock()
            block.renderUnTextured(subsetData.size / 3, subsetData)
        }
    }

    fun determineSubset(constraint: (Vector3) -> Boolean): Int {
        subsetBlockPositions.clear()

        for (i in visibleInstanceData.indices step instanceSize) {
            val x = visibleInstanceData[i]
            val y = visibleInstanceData[i + 1]
            val z = visibleInstanceData[i + 2]

            val position = Vector3(x, y, z)
            if (constraint(position)) {
                subsetBlockPositions += position
            }
        }

        return subsetBlockPositions.size
    }

    private fun getBlockIndex(position: Vector3, data: FloatArray): Int {
        for (i in data.indices step instanceSize) {
            val x = data[i]
            val y = data[i + 1]
            val z = data[i + 2]

            if (position == Vector3(x, y, z)) {
                return i
            }
        }
        return -1
    }

    private fun addSurroundingBlocks(position: Vector3) {
        for (side in Face.values()) {
            if (side == Face.ALL) {
                continue
            }

            val index = getBlockIndex(position + side.normal, hiddenInstanceData)
            if (index != -1) {
                val lastHiddenIndex = hiddenInstanceData.size - instanceSize
                untexturedFloatData += hiddenInstanceData[index]
                untexturedFloatData += hiddenInstanceData[index + 1]
                untexturedFloatData += hiddenInstanceData[index + 2]

                for (i in 0 until instanceSize) {
                    visibleInstanceData += hiddenInstanceData[index + i]
                    hiddenInstanceData[index + i] = hiddenInstanceData[lastHiddenIndex + i]
                }

                hiddenInstanceData = hiddenInstanceData.sliceArray(0 until hiddenInstanceData.size - instanceSize)
            }
        }
    }

    private fun removeSurroundingBlocks(position: Vector3) {
        for (side in Face.values()) {
            if (side == Face.ALL) {
                continue
            }

            val index = getBlockIndex(position + side.normal, visibleInstanceData)
            if (index != -1) {
                if (areAllNeighboursSolid(position + side.normal)) {
                    val newHiddenData = FloatArray(instanceSize)
                    val lastBlockIndex = visibleInstanceData.size - instanceSize
                    for (i in 0 until instanceSize) {
                        newHiddenData[i] = visibleInstanceData[index + i]
                        visibleInstanceData[index + i] = visibleInstanceData[lastBlockIndex + i]
                    }

                    hiddenInstanceData += newHiddenData
                    visibleInstanceData = visibleInstanceData.sliceArray(0 until visibleInstanceData.size - instanceSize)
                }
            }
        }
    }

    private fun isBlockSolid(x: Int, y: Int, z: Int): Boolean {
        val isVisible = getBlockIndex(Vector3(x, y, z), visibleInstanceData) != -1
        val isNotVisible = getBlockIndex(Vector3(x, y, z), hiddenInstanceData) != -1

        return isVisible || isNotVisible
    }

    private fun areAllNeighboursSolid(position: Vector3) = areAllNeighboursSolid(position.x.toInt(), position.y.toInt(), position.z.toInt())

    private fun areAllNeighboursSolid(x: Int, y: Int, z: Int): Boolean {
        if (!isBlockSolid(x - 1, y, z)) {
            return false
        }
        if (!isBlockSolid(x + 1, y, z)) {
            return false
        }
        if (!isBlockSolid(x, y - 1, z)) {
            return false
        }
        if (!isBlockSolid(x, y + 1, z)) {
            return false
        }
        if (!isBlockSolid(x, y, z - 1)) {
            return false
        }
        if (!isBlockSolid(x, y, z + 1)) {
            return false
        }
        return true
    }
}
