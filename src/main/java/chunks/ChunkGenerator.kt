package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3
import kotlin.math.roundToInt

object ChunkGenerator {

    private const val CHUNK_SIZE = 32
    private const val CHUNK_HEIGHT = 70

    fun generateChunk(place: Vector3, biome: Biome, seed: Long): Chunk {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)

        val blocks = ArrayList<Pair<BlockType, Vector3>>()

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                val worldX = place.x * CHUNK_SIZE + x
                val worldZ = place.z * CHUNK_SIZE + z
                val y = CHUNK_HEIGHT + noise[worldX, worldZ].roundToInt()
                val position = place * CHUNK_SIZE + Vector3(x, y, z)

                if (blocks.none { block -> block.second == position }) {
                    blocks += Pair(BlockType.DIRT, position)
                }
            }
        }

        return Chunk(blocks)
    }
}