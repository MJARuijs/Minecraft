package chunks2

import chunks.Biome
import chunks.ChunkGenerator
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

        var vertices = FloatArray(0)

        vertices += floatArrayOf(
                -1.0f, 1.0f, 0f, 0f,
                -1.0f, -1.0f, 0f, 0f,
                1.0f, 1.0f, 0f, 0f,

                1.0f, 1.0f, 0f, 0f,
                -1.0f, -1.0f, 0f, 0f,
                1.0f, -1.0f, 0f, 0f
        )

        return Chunk(chunkX, chunkZ, biome, vertices)
    }

}