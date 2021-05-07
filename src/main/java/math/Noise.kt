package math

import environment.terrain.chunks.ChunkGenerator.Companion.CHUNK_SIZE
import org.joml.SimplexNoise
import java.util.*

class Noise(private val octaves: Int,
            private val amplitude: Int,
            private val smoothness: Int,
            private val seed: Long = Random().nextInt(1000000).toLong()) {

    private val random = Random()

    operator fun get(x: Int, z: Int) = get(x.toFloat(), z.toFloat())

    operator fun get(x: Float, z: Float): Float {
        var height = 0.0f
        val nx = x / CHUNK_SIZE
        val nz = z / CHUNK_SIZE
        for (i in 0 until octaves) {
            height += getNoise(nx * (i + 1), nz * (i + 1)) * (1.0f / (i + 1))
        }

        return height
    }

    private fun getNoise(x: Float, z: Float): Float {
        return (SimplexNoise.noise((x + seed) / smoothness, (z + seed) / smoothness)) * amplitude
    }
}