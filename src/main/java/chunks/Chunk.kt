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

    private var instancePositions = FloatArray(0)
    private val numberOfBlocks = blocksData.size

    init {
//        samplers += Pair(null, Sampler(0))
//        storedBlockTypes += Pair(BlockType.AIR, 0)
        for (blockData in blocksData) {
//            println("${blockData.first} ${blockData.second}")

            if (blockData.first == BlockType.AIR) {
                instancePositions += 100000.0f
            } else {
                //            instancePositions += blockData.second.toArray()
                if (storedBlockTypes.none { storedBlock -> storedBlock.first == blockData.first}) {
                    storedBlockTypes += Pair(blockData.first, samplers.size)
                    instancePositions += samplers.size.toFloat()
//                    println(samplers.size.toFloat())
                    samplers += Pair(blockData.first.textures, Sampler(samplers.size))
                } else {
                    val textureIndex = storedBlockTypes.first { storedBlock ->
                        storedBlock.first == blockData.first
                    }.second

//                    println(textureIndex.toFloat())
                    instancePositions += textureIndex.toFloat()
                }
            }
        }

        block.chunkChanged()
    }

    fun render(shaderProgram: ShaderProgram) {
        for (sampler in samplers) {
            if (sampler.first != null) {
                sampler.second.bind(sampler.first!!)
            }
        }
        shaderProgram.set("place", place)
        block.render(numberOfBlocks, instancePositions)
    }

//    fun blockMoved(previousPosition: Vector3, newPosition: Vector3) {
//        val idX = ChunkGenerator.CHUNK_HEIGHT * ChunkGenerator.CHUNK_SIZE * previousPosition.x
//        val idY = ChunkGenerator.CHUNK_SIZE * previousPosition.y
//        val idZ = previousPosition.z
//
//        val id = idX + idY + idZ
//        instancePositions += newPosition.x
//        instancePositions += newPosition.y
//        instancePositions += newPosition.z
//        instancePositions += id
//
//        block.chunkChanged()
//    }
}
