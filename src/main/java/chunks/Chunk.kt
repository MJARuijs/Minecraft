package chunks

import chunks.blocks.Block
import chunks.blocks.BlockType
import graphics.samplers.Sampler
import graphics.textures.ImageMap
import math.vectors.Vector3

class Chunk(blocksData: ArrayList<Pair<BlockType, Vector3>>) {

    private val block = Block()
    private val samplers = ArrayList<Pair<ImageMap, Sampler>>()
    private val storedBlockTypes = ArrayList<BlockType>()

    private var instancePositions = FloatArray(0)
    private val numberOfBlocks = blocksData.size

    init {
        for (blockData in blocksData) {
            instancePositions += blockData.second.toArray()
            if (!storedBlockTypes.contains(blockData.first)) {
                storedBlockTypes += blockData.first
                samplers += Pair(blockData.first.textures, Sampler(samplers.size))
            }

            instancePositions += BlockType.values().indexOf(blockData.first).toFloat()
        }

        block.chunkChanged()
    }

    fun render() {
        for (sampler in samplers) {
            sampler.second.bind(sampler.first)
        }

        block.render(numberOfBlocks, instancePositions)
    }
}