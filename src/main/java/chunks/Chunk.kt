package chunks

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.ChunkGenerator.Companion.MAX_HEIGHT
import chunks.blocks.Block
import chunks.blocks.BlockType
import chunks.blocks.Face
import graphics.shaders.ShaderProgram
import math.vectors.Vector2
import math.vectors.Vector3
import kotlin.math.roundToInt

class Chunk(val chunkX: Int, val chunkZ: Int, private var highestBlock: Int, private val biome: Biome, private val blocks: ArrayList<Pair<BlockType, Vector3>>) {

    constructor(data: ChunkData) : this(data.x, data.z, data.highestBlock, data.biome, data.blocks)

    private var instanceData = FloatArray(0)
    private var untexturedData = FloatArray(0)
    private val subsetBlockPositions = ArrayList<Vector3>()

    private val visibleBlocks = ArrayList<Pair<BlockType, Vector3>>()

    private var initialized = false

    private lateinit var block: Block

    init {
        determineVisibleBlocks()
        determineInstanceData()
    }

    private fun initBlock() {
        block = Block()
        block.initAttributes()
        initialized = true
    }

    fun getSubsetSize() = subsetBlockPositions.size

    fun getCenter() = Vector2(chunkX + CHUNK_SIZE / 2, chunkZ + CHUNK_SIZE / 2)

    fun getSubsetPosition(i: Int) = subsetBlockPositions[i]

    fun containsBlock(position: Vector3) = containsBlock(position.x.roundToInt(), position.z.roundToInt())

    private fun containsBlock(x: Int, z: Int): Boolean {
        if (x < chunkX  || x >= chunkX + CHUNK_SIZE) {
            return false
        }
        if (z < chunkZ || z >= chunkZ + CHUNK_SIZE ) {
            return false
        }
        return true
    }

    fun addBlock(type: BlockType, position: Vector3) {
        if (containsBlock(position) && position.y >= 0 && position.y < MAX_HEIGHT) {
            blocks += Pair(type, position)

            if (position.y.toInt() >= highestBlock) {
                highestBlock = position.y.toInt()
            }

            visibleBlocks += Pair(type, position)

            removeSurroundingBlocks(position)
            determineInstanceData()
        }
    }

    fun removeBlock(position: Vector3) {
        blocks.removeIf { block ->
            block.second == position
        }

        visibleBlocks.removeIf { block ->
            block.second == position
        }

        addSurroundingBlocks(position)
        determineInstanceData()
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("overlayColor", biome.overlayColor)
        if (initialized) {
            block.render(visibleBlocks.size, instanceData)
        } else {
            initBlock()
            block.render(visibleBlocks.size, instanceData)
        }
    }

    fun renderSubset() {
        var subsetData = FloatArray(0)
        for (block in subsetBlockPositions) {
            subsetData += block.toArray()
        }
        if (initialized) {
            block.renderUnTextured(subsetBlockPositions.size, subsetData)
        } else {
            initBlock()
            block.renderUnTextured(subsetBlockPositions.size, subsetData)
        }
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

    private fun addSurroundingBlocks(position: Vector3) {
        for (side in Face.values()) {
            val block = blocks.find { block -> block.second == position + side.normal }
            if (block != null && visibleBlocks.none { visibleBlock -> visibleBlock.second == block.second }) {
                visibleBlocks += block
            }
        }
    }

    private fun removeSurroundingBlocks(position: Vector3) {
        for (side in Face.values()) {
            val block = blocks.find { block -> block.second == position + side.normal } ?: continue
            if (areAllNeighboursSolid(block.second)) {
                visibleBlocks.removeIf { visibleBlock ->
                    visibleBlock.second == block.second
                }
            }
        }
    }

    private fun determineVisibleBlocks(): ArrayList<Pair<BlockType, Vector3>> {
        visibleBlocks.clear()
        for (x in chunkX  until chunkX + CHUNK_SIZE) {
            for (y in 0 .. highestBlock) {
                for (z in chunkZ  until chunkZ + CHUNK_SIZE) {

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
                    if (y == 0 || y == highestBlock - 1) {
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
