package chunks

import chunks.ChunkGenerator.CHUNK_SIZE
import chunks.blocks.Block
import chunks.blocks.BlockType
import chunks.blocks.Face
import math.vectors.Vector2
import math.vectors.Vector3
import kotlin.math.roundToInt

class Chunk(val chunkX: Int, val chunkZ: Int, private val blocks: ArrayList<Pair<BlockType, Vector3>>) {

    constructor(vector2: Vector2, blocks: ArrayList<Pair<BlockType, Vector3>>) : this(vector2.x.roundToInt(), vector2.y.roundToInt(), blocks)

    private val block = Block()
    private var instanceData = FloatArray(0)
    private var untexturedData = FloatArray(0)

    private val subsetBlockPositions = ArrayList<Vector3>()

    private val visibleBlocks = ArrayList<Pair<BlockType, Vector3>>()

    init {
        block.initAttributes()

        determineVisibleBlocks()
        determineInstanceData()
    }

    fun getSubsetSize() = subsetBlockPositions.size

    fun getPosition() = Vector2(chunkX, chunkZ)

    fun getCenter() = Vector2(chunkX + CHUNK_SIZE / 2, chunkZ + CHUNK_SIZE / 2)

    fun containsBlock(position: Vector3) = containsBlock(position.x.roundToInt(), position.z.roundToInt())

    fun getSubsetPosition(i: Int) = subsetBlockPositions[i]

    private fun containsBlock(x: Int, z: Int): Boolean {
        if (x < chunkX || x > chunkX + CHUNK_SIZE) {
            return false
        }
        if (z < chunkZ || z > chunkZ + CHUNK_SIZE) {
            return false
        }

        return true
    }

    fun addBlock(type: BlockType, position: Vector3, face: Face) {
        blocks += when (face) {
            Face.FRONT -> Pair(type, position + Vector3(0, 0, 1))
            Face.BACK -> Pair(type, position + Vector3(0, 0, -1))
            Face.LEFT -> Pair(type, position + Vector3(-1, 0, 0))
            Face.RIGHT -> Pair(type, position + Vector3(1, 0, 0))
            Face.TOP -> Pair(type, position + Vector3(0, 1, 0))
            Face.BOTTOM -> Pair(type, position + Vector3(0, -1, 0))
        }

        determineVisibleBlocks()
        determineInstanceData()
    }

    fun removeBlock(position: Vector3) {
        blocks.removeIf { block ->
            block.second == position
        }

        determineVisibleBlocks()
        determineInstanceData()
    }

    fun render() {
        block.render(visibleBlocks.size, instanceData)
    }

    fun renderSubset() {
        var subsetData = FloatArray(0)
        for (block in subsetBlockPositions) {
            subsetData += block.toArray()
        }
        block.renderUnTextured(subsetBlockPositions.size, subsetData)
    }

    fun determineSubset(constraint: (Pair<BlockType, Vector3>) -> Boolean): Int {
        subsetBlockPositions.clear()

        for (visibleBlock in visibleBlocks) {
            if (constraint(visibleBlock)) {
                subsetBlockPositions += visibleBlock.second
            }
        }

        return subsetBlockPositions.size
    }

    private fun determineInstanceData() {
        instanceData = FloatArray(0)
        untexturedData = FloatArray(0)

        for (block in visibleBlocks) {
            if (block.first == BlockType.AIR) {
                continue
            }

            instanceData += block.second.toArray()
            instanceData += block.first.getOffsets()

            untexturedData += block.second.toArray()
        }
    }

    private fun determineVisibleBlocks(): ArrayList<Pair<BlockType, Vector3>> {
        visibleBlocks.clear()
        for (x in chunkX until chunkX + CHUNK_SIZE) {
            for (y in 0 until ChunkGenerator.MAX_HEIGHT) {
                for (z in chunkZ until chunkZ + CHUNK_SIZE) {

                    val currentBlock = blocks.findLast { block ->
                        block.second == Vector3(x, y, z)
                    } ?: continue

                    if (currentBlock.first == BlockType.AIR) {
                        continue
                    }

                    if (x == chunkX || x == chunkX + CHUNK_SIZE - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }
                    if (y == 0 || y == ChunkGenerator.MAX_HEIGHT - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }
                    if (z == chunkZ || z == chunkZ + CHUNK_SIZE - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }

                    if (!areAllNeighboursSolid(currentBlock.second)) {
                        visibleBlocks += currentBlock
                    }
                }
            }
        }
        return visibleBlocks
    }

    private fun isNeighbourBlockSolid(x: Float, y: Float, z: Float): Boolean {
        val neighbourBlockType = checkNeighbourBlockType(x, y, z) ?: return false
        return neighbourBlockType != BlockType.AIR
    }

    private fun checkNeighbourBlockType(x: Float, y: Float, z: Float): BlockType? {
        return blocks.findLast { block ->
            block.second == Vector3(x, y, z)
        }?.first
    }

    private fun areAllNeighboursSolid(position: Vector3) = areAllNeighboursSolid(position.x, position.y, position.z)

    private fun areAllNeighboursSolid(x: Float, y: Float, z: Float): Boolean {
        if (!isNeighbourBlockSolid(x - 1.0f, y, z)) {
            return false
        }
        if (!isNeighbourBlockSolid(x + 1.0f, y, z)) {
            return false
        }
        if (!isNeighbourBlockSolid(x, y - 1.0f, z)) {
            return false
        }
        if (!isNeighbourBlockSolid(x, y + 1.0f, z)) {
            return false
        }
        if (!isNeighbourBlockSolid(x, y, z - 1.0f)) {
            return false
        }
        if (!isNeighbourBlockSolid(x, y, z + 1.0f)) {
            return false
        }
        return true
    }
}
