package chunks

import chunks.blocks.Block
import chunks.blocks.BlockType
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import math.vectors.Vector3

class Chunk(private val place: Vector3, blocksData: ArrayList<Pair<BlockType, Vector3>>) {

    private val block = Block()
    private val samplers = ArrayList<Pair<ImageMap, Sampler>>()
    private val storedBlockTypes = ArrayList<BlockType>()

    private var instancePositions = FloatArray(0)
    private val numberOfBlocks = blocksData.size

    init {
        for (blockData in blocksData) {
//            instancePositions += blockData.second.toArray()
            if (!storedBlockTypes.contains(blockData.first)) {
                storedBlockTypes += blockData.first
                samplers += Pair(blockData.first.textures, Sampler(samplers.size))
            }

//            instancePositions += BlockType.values().indexOf(blockData.first).toFloat()
        }

//        block.chunkChanged()
    }

    fun render(shaderProgram: ShaderProgram) {
        for (sampler in samplers) {
            sampler.second.bind(sampler.first)
        }
        shaderProgram.set("place", place)
        block.render(numberOfBlocks, instancePositions)
    }

    fun blockMoved(previousPosition: Vector3, newPosition: Vector3) {
        val idX = ChunkGenerator.CHUNK_HEIGHT * ChunkGenerator.CHUNK_SIZE * previousPosition.x
        val idY = ChunkGenerator.CHUNK_SIZE * previousPosition.y
        val idZ = previousPosition.z

        val id = idX + idY + idZ
        instancePositions += newPosition.x
        instancePositions += newPosition.y
        instancePositions += newPosition.z
        instancePositions += id

        block.chunkChanged()
    }
}
