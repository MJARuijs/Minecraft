package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3

class ChunkGenerator {

    companion object {
        const val CHUNK_SIZE = 16
        const val TERRAIN_HEIGHT = 20
        const val MAX_HEIGHT = 30
    }

    fun generateChunkData(biomeX: Int, biomeZ: Int, biome: Biome, seed: Long): ChunkData {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)
        val blocks = ArrayList<Pair<BlockType, Vector3>>()

        var highestBlock = 0

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                val worldX = biomeX + x
                val worldZ = biomeZ + z
                val h = noise[worldX, worldZ].toInt()
                val height = TERRAIN_HEIGHT + h

                if (height - 1 > highestBlock) {
                    highestBlock = height - 1
                }

                for (y in 0 until height) {
                    val position = Vector3(worldX, y, worldZ)
                    if (blocks.none { block -> block.second == position }) {
                        blocks += when {
                            y == height - 1 -> Pair(BlockType.GRASS, position)
                            y < height - 4 -> Pair(BlockType.STONE, position)
                            y < 2 -> Pair(BlockType.BEDROCK, position)
                            else -> Pair(BlockType.DIRT, position)
                        }
                    }
                }
            }
        }

        return ChunkData(biomeX, biomeZ, highestBlock, biome, blocks)
    }
}