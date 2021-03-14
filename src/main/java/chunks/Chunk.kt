package chunks

import chunks.blocks.Block
import chunks.blocks.BlockType
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import math.vectors.Vector3

class Chunk(private val place: Vector3, blocksData: ArrayList<Pair<BlockType, Vector3>>) {

    private val block = Block()
    private val samplers = ArrayList<Pair<ImageMap?, Sampler>>()
    private val storedBlockTypes = ArrayList<Pair<BlockType, Int>>()

    private val blocks = ArrayList<Pair<BlockType, Vector3>>()
    private val visibleBlocks = ArrayList<Pair<BlockType, Vector3>>()

    val numberOfBlocks = blocksData.size

    init {
        for (blockData in blocksData) {
            if (blockData.first != BlockType.AIR) {
                if (storedBlockTypes.none { storedBlock -> storedBlock.first == blockData.first}) {
                    storedBlockTypes += Pair(blockData.first, samplers.size)
                    samplers += Pair(blockData.first.textures, Sampler(samplers.size))
                }
            }

            blocks += blockData
        }

        determineVisibleBlocks()
        block.chunkChanged()
    }

    fun render(shaderProgram: ShaderProgram) {
        for (sampler in samplers) {
            if (sampler.first != null) {
                sampler.second.bind(sampler.first!!)
            }
        }
        shaderProgram.set("place", place)
        var positions = FloatArray(0)

        for (block in visibleBlocks) {
            if (block.first == BlockType.AIR) {
                continue
            }
            val textureIndex = storedBlockTypes.findLast { storedBlock ->
                storedBlock.first == block.first
            }?.second ?: throw IllegalArgumentException("No block found with type ${block.first}")
            
            positions += textureIndex.toFloat()
            positions += block.second.toArray()
        }

        block.render(visibleBlocks.size, positions)
    }

    private fun determineVisibleBlocks(): ArrayList<Pair<BlockType, Vector3>> {
        visibleBlocks.clear()

        for (x in 0 until ChunkGenerator.CHUNK_SIZE) {
            for (y in 0 until ChunkGenerator.MAX_HEIGHT) {
                for (z in 0 until ChunkGenerator.CHUNK_SIZE) {

                    val currentBlock = blocks.findLast { block ->
                        block.second == Vector3(x, y, z)
                    } ?: throw IllegalArgumentException("No block found at $x $y $z")

                    if (currentBlock.first == BlockType.AIR) {
                        continue
                    }

                    if (x == 0 || x == ChunkGenerator.CHUNK_SIZE - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }
                    if (y == 0 || y == ChunkGenerator.MAX_HEIGHT - 1) {
                        visibleBlocks += currentBlock
                        continue
                    }
                    if (z == 0 || z == ChunkGenerator.CHUNK_SIZE - 1) {
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
