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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class Chunk(val chunkX: Int, val chunkZ: Int, private val biome: Biome, val timerId: Int, private var instanceFloatData: FloatArray, private var highestBlock: Int = 0) {
//    val blocks: ArrayList<Pair<BlockType, Vector3>> = ArrayList(),
//    private var instanceFloatData = FloatArray(0)

    private val instanceSize = 21

    private var untexturedFloatData = FloatArray(0)

    private val subsetBlockPositions = ArrayList<Vector3>()

    private var initialized = false

    private val locked = AtomicBoolean(false)

    private lateinit var block: Block

    init {
//        initInstanceData()
        println("Delay: ${Timer.getDelay(timerId)}")
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

//    fun add(newBlocks: ArrayList<Pair<BlockType, Vector3>>) {
//        blocks += newBlocks
//    }

    fun add(newData: FloatArray, timerId: Int) {
        instanceFloatData += newData
        println("Add Delay: ${Timer.getDelay(timerId)}")
    }

    fun addBlock(type: BlockType, position: Vector3) {
        if (containsBlock(position) && position.y >= 0 && position.y < MAX_HEIGHT) {
            val newBlock = Pair(type, position)
//            blocks += newBlock

            if (position.y.toInt() >= highestBlock) {
                highestBlock = position.y.toInt()
            }

            removeSurroundingBlocks(position)
            addBlockData(newBlock)
        }
    }

    fun removeBlock(position: Vector3) {
        val index = getBlockIndex(position)
        if (index != -1) {
            for (i in index + 3 until index + instanceSize) {
                instanceFloatData[i] = -1f
            }
        }

        addSurroundingBlocks(position)
        makeBlockInvisible(position)
    }

    private fun addBlockData(block: Pair<BlockType, Vector3>) {
        instanceFloatData += block.second.toArray()
        instanceFloatData += block.first.getOffsets()

        untexturedFloatData += block.second.toArray()
    }

    private fun addBlockData(data: FloatArray) {
        instanceFloatData += data

        untexturedFloatData += data[0]
        untexturedFloatData += data[1]
        untexturedFloatData += data[2]
    }

    private fun makeBlockInvisible(position: Vector3) {
        for (i in instanceFloatData.indices step instanceSize) {
            val x = instanceFloatData[i]
            val y = instanceFloatData[i + 1]
            val z = instanceFloatData[i + 2]

            if (position == Vector3(x, y, z)) {
                for (j in 0 until instanceSize - 3) {
                    instanceFloatData[i + j] = -1f
                }
            }
        }
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("overlayColor", biome.overlayColor)
        if (initialized) {
            block.render(instanceFloatData.size / instanceSize, instanceFloatData)
        } else {
            initBlock()
            block.render(instanceFloatData.size / instanceSize, instanceFloatData)
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

    fun determineSubset(constraint: (Vector3) -> Boolean): Int {
        subsetBlockPositions.clear()

        for (i in instanceFloatData.indices step instanceSize) {
            val x = instanceFloatData[i]
            val y = instanceFloatData[i + 1]
            val z = instanceFloatData[i + 2]
            val position = Vector3(x, y, z)
            if (constraint(position)) {
                subsetBlockPositions += position
            }
        }

        return subsetBlockPositions.size
    }

    private fun getBlockIndex(position: Vector3): Int {
        for (i in instanceFloatData.indices step instanceSize) {
            val x = instanceFloatData[i]
            val y = instanceFloatData[i + 1]
            val z = instanceFloatData[i + 2]

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
//            val block = blocks.find { block -> block.second == position + side.normal }
            val index = getBlockIndex(position + side.normal)
            if (index != -1) {
//                val blockId =
                val data = FloatArray(instanceSize)
                for (i in 0 until instanceSize) {
                    data[i] = instanceFloatData[i + index]
                }
                addBlockData(data)
            }
        }
    }

    private fun removeSurroundingBlocks(position: Vector3) {
        for (side in Face.values()) {
            if (side == Face.ALL) {
                continue
            }

//            val block = blocks.find { block -> block.second == position + side.normal } ?: continue
//            if (areAllNeighboursSolid(block.second)) {
//                makeBlockInvisible(block.second)
//            }
        }
    }

    private fun isBlockSolid(x: Int, y: Int, z: Int): Boolean {
        return getBlockIndex(Vector3(x, y, z)) != -1
    }

//    private fun getBlockType(x: Int, y: Int, z: Int): BlockType? {
//
//        return getBlockIndex(Vector3(x, y, x))
//    }

    private fun areAllNeighboursSolid(position: Vector3) = areAllNeighboursSolid(position.x.toInt(), position.y.toInt(), position.z.toInt())

    private fun areAllNeighboursSolid(x: Int, y: Int, z: Int): Boolean {
        return getTransparentNeighbours(x, y, z)
    }

    private fun getTransparentNeighbours(x: Int, y: Int, z: Int): Boolean {
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


//    private fun initInstanceData() {
//        instanceFloatData = FloatArray(0)
//        untexturedFloatData = FloatArray(0)
//
//        val id = Timer.start()
//        for (block in blocks) {
//            if (block.first == BlockType.AIR) {
//                continue
//            }
//
//            while (locked.get()) {
//                Thread.sleep(0)
//            }
//
//            addBlockData(block)
//        }
//        println("Delay: ${Timer.getDelay(id)} ")
//    }

}
