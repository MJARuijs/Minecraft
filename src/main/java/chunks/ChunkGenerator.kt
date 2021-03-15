package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3

object ChunkGenerator {

    const val CHUNK_SIZE = 16
    const val TERRAIN_HEIGHT = 1
    const val MAX_HEIGHT = 15

    fun generateChunk(biomeX: Int, biomeZ: Int, biome: Biome, seed: Long): Chunk {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)
        val blocks = ArrayList<Pair<BlockType, Vector3>>()

        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until MAX_HEIGHT) {
                for (z in 0 until CHUNK_SIZE) {
                    val worldX = biomeX + x
                    val worldZ = biomeZ + z
                    val h = noise[worldX, worldZ]
                    val height = TERRAIN_HEIGHT

                    val position = Vector3(worldX, y, worldZ)
                    if ((x == 0 || x == CHUNK_SIZE - 1 || z == 0 || z == CHUNK_SIZE - 1) && y < 1) {
                        blocks += Pair(BlockType.DIRT, position)
                    } else {
                        blocks += when {
                            y >= height -> Pair(BlockType.AIR, position)
                            y < 2 -> Pair(BlockType.BEDROCK, position)
                            else -> Pair(BlockType.DIRT, position)
                        }
                    }


                }
            }
        }

        return Chunk(biomeX, biomeZ, blocks)
    }
}