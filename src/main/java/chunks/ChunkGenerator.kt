package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3

object ChunkGenerator {

    const val CHUNK_SIZE = 16
    const val CHUNK_HEIGHT = 70

    fun generateChunk(place: Vector3, biome: Biome, seed: Long): Chunk {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)
        val blocks = ArrayList<Pair<BlockType, Vector3>>()
        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_HEIGHT) {
                for (z in 0 until CHUNK_SIZE) {
                    blocks += if (y < 1) {
                        Pair(BlockType.BEDROCK, Vector3())
                    } else {
                        Pair(BlockType.DIRT, Vector3())
                    }
                }
            }
        }

        return Chunk(place * CHUNK_SIZE, blocks)
    }
}