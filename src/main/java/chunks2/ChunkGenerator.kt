package chunks2

import chunks.Biome
import chunks.ChunkGenerator
import chunks.blocks.BlockData
import chunks.blocks.BlockType
import chunks.blocks.BlockType2
import math.Noise
import math.vectors.Vector3

class ChunkGenerator {

    companion object {
        const val CHUNK_SIZE = 16
        const val HALF_CHUNK_SIZE = CHUNK_SIZE / 2
        const val TERRAIN_HEIGHT = 10
        const val MAX_HEIGHT = 256
    }

    private val heights = Array(ChunkGenerator.CHUNK_SIZE) { Array(ChunkGenerator.CHUNK_SIZE) { 0 } }
    private val data = FloatArray(ChunkGenerator.CHUNK_SIZE * ChunkGenerator.CHUNK_SIZE * ChunkGenerator.MAX_HEIGHT * 21)
    private val positions = ArrayList<Vector3>()

    private var chunkX = 0
    private var chunkZ = 0

    private lateinit var noise: Noise

    fun generate(chunkX: Int, chunkZ: Int, biome: Biome, seed: Long): Chunk {
        this.chunkX = chunkX - HALF_CHUNK_SIZE
        this.chunkZ = chunkZ - HALF_CHUNK_SIZE
        this.noise = Noise(biome.octaves, biome.amplitude, biome.smoothness, seed)

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                heights[x][z] = 0
            }
        }

        val blockData = ArrayList<BlockData>()

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {

                val worldX = chunkX + x - HALF_CHUNK_SIZE
                val worldZ = chunkZ + z - HALF_CHUNK_SIZE

                val height = get(x, z)
//                val height = TERRAIN_HEIGHT

                val position = Vector3(worldX, height, worldZ)
                val blockType = determineBlockType(height, height, biome)
                blockData += BlockData(blockType, position)
            }
        }
//        blockData += BlockData(BlockType2.STONE, Vector3())
        return Chunk(chunkX, chunkZ, biome, blockData)
    }

    private fun get(x: Int, z: Int): Int {
        if (heights[x][z] == 0) {
            heights[x][z] = noise[x + chunkX, z + chunkZ].toInt() + ChunkGenerator.TERRAIN_HEIGHT
        }
        return heights[x][z]
    }

    private fun determineBlockType(y: Int, maxY: Int, biome: Biome): BlockType2 {
        var blockType = BlockType2.STONE
        var typeHeight = 0
        for (type in biome.blocks) {
            if (y > maxY - type.second - typeHeight) {
                blockType = type.first
                break
            }
            typeHeight += type.second
        }
        return blockType
    }
}