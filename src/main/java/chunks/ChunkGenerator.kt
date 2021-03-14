package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3

object ChunkGenerator {

    const val CHUNK_SIZE = 16
    const val TERRAIN_HEIGHT = 10
    const val MAX_HEIGHT = 20

    fun generateChunk(place: Vector3, biome: Biome, seed: Long): Chunk {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)
        val blocks = ArrayList<Pair<BlockType, Vector3>>()

        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until MAX_HEIGHT) {
                for (z in 0 until CHUNK_SIZE) {
                    val worldX = place.x * CHUNK_SIZE + x
                    val worldZ = place.z * CHUNK_SIZE + z
                    val h = noise[worldX, worldZ]
                    val height = TERRAIN_HEIGHT

                    val position = place * CHUNK_SIZE + Vector3(worldX, y.toFloat(), worldZ)

                    blocks += when {
                        x == 1 && y == 1 && z == 1 -> Pair(BlockType.STONE, position)
                        y >= height -> Pair(BlockType.AIR, position)
                        y < 2 -> Pair(BlockType.BEDROCK, position)
                        else -> Pair(BlockType.DIRT, position)
                    }
                }
            }
        }

        return Chunk(place * CHUNK_SIZE, blocks)
    }
}