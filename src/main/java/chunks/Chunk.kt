package chunks

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.ChunkGenerator.Companion.MAX_HEIGHT
import chunks.blocks.Block
import chunks.blocks.BlockType
import chunks.blocks.Face
import graphics.shaders.ShaderProgram
import math.vectors.Vector2
import math.vectors.Vector3
import util.Timer
import kotlin.math.roundToInt

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val blocks: ArrayList<Pair<BlockType, Vector3>> = ArrayList(), private var highestBlock: Int = 0) {

    private var instanceFloatData = FloatArray(0)
    private var untexturedFloatData = FloatArray(0)
    private val subsetBlockPositions = ArrayList<Vector3>()

    private val visibleBlocks = ArrayList<Pair<BlockType, Vector3>>()

    private var initialized = false

    private lateinit var block: Block

    init {
        visibleBlocks += blocks
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

    fun containsBlock(position: Vector3) = containsBlock(position.x.roundToInt(), position.y.roundToInt(), position.z.roundToInt())

    private fun containsBlock(x: Int, y: Int, z: Int): Boolean {
        if (y < 0 || y > MAX_HEIGHT) {
            return false
        }
        if (x < chunkX  || x >= chunkX + CHUNK_SIZE) {
            return false
        }
        if (z < chunkZ || z >= chunkZ + CHUNK_SIZE ) {
            return false
        }
        return true
    }

    fun add(newBlocks: ArrayList<Pair<BlockType, Vector3>>) {
        blocks += newBlocks
        determineVisibleBlocks()
        determineInstanceData()
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
            block.render(visibleBlocks.size, instanceFloatData)
        } else {
            initBlock()
            block.render(visibleBlocks.size, instanceFloatData)
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
        instanceFloatData = FloatArray(0)
        untexturedFloatData = FloatArray(0)

        val id = Timer.start()
        for (block in visibleBlocks) {
            if (block.first == BlockType.AIR) {
                continue
            }

            instanceFloatData += block.second.toArray()
            instanceFloatData += block.first.getOffsets()

            untexturedFloatData += block.second.toArray()
        }
        println("Delay: ${Timer.getDelay(id)}")
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

    private fun determineVisibleBlocksOld(): ArrayList<Pair<BlockType, Vector3>> {
        visibleBlocks.clear()
        println("DETERMINING")
        for (x in chunkX  until chunkX + CHUNK_SIZE) {
            for (y in 0 .. highestBlock) {
                for (z in chunkZ  until chunkZ + CHUNK_SIZE) {

                    val currentBlock = blocks.findLast { block ->
                        block.second == Vector3(x, 0, 0)
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
        println("DONE")
        return visibleBlocks
    }

    private fun determineVisibleBlocks(): ArrayList<Pair<BlockType, Vector3>> {
//        visibleBlocks.clear()
        println("DETERMINING ${blocks.size} ${visibleBlocks.size}")
        for (currentBlock in blocks) {
            val x = currentBlock.second.x.toInt()
            val y = currentBlock.second.y.toInt()
            val z = currentBlock.second.z.toInt()

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
        println("DONE ${visibleBlocks.size}")
        return visibleBlocks
    }

    private fun isBlockSolid(x: Int, y: Int, z: Int): Boolean {
        val neighbourBlockType = getBlockType(x, y, z) ?: return false
        return neighbourBlockType != BlockType.AIR
    }

    private fun getBlockType(x: Int, y: Int, z: Int): BlockType? {
        return blocks.findLast { block ->
            block.second == Vector3(x, y, z)
        }?.first
    }

    private fun areAllNeighboursSolid(position: Vector3) = areAllNeighboursSolid(position.x.toInt(), position.y.toInt(), position.z.toInt())

    private fun areAllNeighboursSolid(x: Int, y: Int, z: Int): Boolean {
        return getTransparentNeighbours(x, y, z).isEmpty()
    }

    private fun getTransparentNeighbours(position: Vector3) = getTransparentNeighbours(position.x.toInt(), position.y.toInt(), position.z.toInt())

    private fun getTransparentNeighbours(x: Int, y: Int, z: Int): ArrayList<Vector3> {
        val transparentNeighbours = ArrayList<Vector3>()
        if (!isBlockSolid(x - 1, y, z)) {
            transparentNeighbours += Vector3(x - 1, y, z)
        }
        if (!isBlockSolid(x + 1, y, z)) {
            transparentNeighbours += Vector3(x + 1, y, z)
        }
        if (!isBlockSolid(x, y - 1, z)) {
            transparentNeighbours += Vector3(x, y - 1, z)
        }
        if (!isBlockSolid(x, y + 1, z)) {
            transparentNeighbours += Vector3(x, y + 1, z)
        }
        if (!isBlockSolid(x, y, z - 1)) {
            transparentNeighbours += Vector3(x, y, z - 1)
        }
        if (!isBlockSolid(x, y, z + 1)) {
            transparentNeighbours += Vector3(x, y, z + 1)
        }
        return transparentNeighbours
    }
}
