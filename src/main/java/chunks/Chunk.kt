package chunks

import chunks.ChunkGenerator.CHUNK_SIZE
import chunks.blocks.Block
import chunks.blocks.BlockType
import math.vectors.Vector2
import math.vectors.Vector3

class Chunk(val chunkX: Int, val chunkZ: Int, private val blocks: ArrayList<Pair<BlockType, Vector3>>) {

    private val block = Block()
    private val visibleBlocks = ArrayList<Pair<BlockType, Vector3>>()
    private var instanceData = FloatArray(0)
    private var untexturedData = FloatArray(0)

    init {
        determineVisibleBlocks()
        for (block in visibleBlocks) {
            if (block.first == BlockType.AIR) {
                continue
            }

            instanceData += block.second.toArray()
            instanceData += block.first.getOffsets()

            untexturedData += block.second.toArray()
        }

        block.initAttributes()
    }

    fun getNumberOfBlocks() = visibleBlocks.size

    fun getPosition() = Vector2(chunkX, chunkZ)

    fun getCenter() = Vector2(chunkX + CHUNK_SIZE / 2, chunkZ + CHUNK_SIZE / 2)

    fun render() {
        block.render(visibleBlocks.size, instanceData)
    }

    fun renderUnTextured() {
        block.renderUnTextured(visibleBlocks.size, untexturedData)
    }

    private fun determineVisibleBlocks(): ArrayList<Pair<BlockType, Vector3>> {
        visibleBlocks.clear()
        for (x in chunkX until chunkX + ChunkGenerator.CHUNK_SIZE) {
            for (y in 0 until ChunkGenerator.MAX_HEIGHT) {
                for (z in chunkZ until chunkZ + ChunkGenerator.CHUNK_SIZE) {

                    val currentBlock = blocks.findLast { block ->
                        block.second == Vector3(x, y, z)
                    } ?: throw IllegalArgumentException("No block found at $x $y $z")

                    if (currentBlock.first == BlockType.AIR) {
                        continue
                    }

                    if (x == chunkX || x == chunkX + ChunkGenerator.CHUNK_SIZE - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }
                    if (y == 0 || y == ChunkGenerator.MAX_HEIGHT - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }
                    if (z == chunkZ || z == chunkZ + ChunkGenerator.CHUNK_SIZE - 1) {
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
        return checkNeighbourBlockType(x, y, z) != BlockType.AIR
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
