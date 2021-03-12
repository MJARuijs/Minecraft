package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3

object ChunkGenerator {

    const val CHUNK_SIZE = 16
    const val TERRAIN_HEIGHT = 15
    const val MAX_HEIGHT = 30

    fun generateChunk(place: Vector3, biome: Biome, seed: Long): Chunk {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)
        val blocks = ArrayList<Pair<BlockType, Vector3>>()

//        var minY = Float.MAX_VALUE

        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until MAX_HEIGHT) {
                for (z in 0 until CHUNK_SIZE) {
                    val worldX = place.x * CHUNK_SIZE + x
                    val worldZ = place.z * CHUNK_SIZE + z
                    val h = noise[worldX, worldZ]
                    val height = TERRAIN_HEIGHT + h

//                    if (h < minY) {
//                        minY = h
//                    }

//                    println(noise[worldX, worldZ])
                    val position = place * CHUNK_SIZE + Vector3(worldX, y.toFloat(), worldZ)

                    if (y > height) {
                        blocks += Pair(BlockType.AIR, position)
                    } else if (y < 1) {
                        blocks += Pair(BlockType.BEDROCK, position)
                    } else {
                        blocks += Pair(BlockType.DIRT, position)
                    }

//                    blocks += if (y < 1) {
//                        Pair(BlockType.BEDROCK, Vector3())
//                    } else {
//                        Pair(BlockType.DIRT, Vector3())
//                    }
                }

            }
        }

//        println(minY)

        return Chunk(place * CHUNK_SIZE, blocks)
    }
}