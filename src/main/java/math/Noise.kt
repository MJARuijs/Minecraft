package math

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class Noise {

    private val random: Random = Random(Random().nextLong())

    private fun generateWhiteNoise(width: Int, height: Int): Array<FloatArray> {
        val noise = Array(width) { FloatArray(height) }
        for (i in 0 until width) {
            for (j in 0 until height) {
                noise[i][j] = random.nextDouble().toFloat() % 1
            }
        }
        return noise
    }

    private fun generateSmoothNoise(baseNoise: Array<FloatArray>, octave: Int): Array<FloatArray> {
        val width = baseNoise.size
        val height: Int = baseNoise[0].size
        val smoothNoise = Array(width) { FloatArray(height) }
        val samplePeriod = 1 shl octave // calculates 2 ^ k
        val sampleFrequency = 1.0f / samplePeriod
        for (i in 0 until width) {
            // calculate the horizontal sampling indices
            val sample_i0 = i / samplePeriod * samplePeriod
            val sample_i1 = (sample_i0 + samplePeriod) % width // wrap around
            val horizontal_blend = (i - sample_i0) * sampleFrequency
            for (j in 0 until height) {
                // calculate the vertical sampling indices
                val sample_j0 = j / samplePeriod * samplePeriod
                val sample_j1 = (sample_j0 + samplePeriod) % height // wrap
                // around
                val vertical_blend = (j - sample_j0) * sampleFrequency

                // blend the top two corners
                val top = interpolate(baseNoise[sample_i0][sample_j0],
                        baseNoise[sample_i1][sample_j0], horizontal_blend)

                // blend the bottom two corners
                val bottom = interpolate(baseNoise[sample_i0][sample_j1],
                        baseNoise[sample_i1][sample_j1], horizontal_blend)

                // final blend
                smoothNoise[i][j] = interpolate(top, bottom, vertical_blend)
            }
        }
        return smoothNoise
    }

    private fun interpolate(x0: Float, x1: Float, alpha: Float): Float {
        return x0 * (1 - alpha) + alpha * x1
    }

    private fun generatePerlinNoise(baseNoise: Array<FloatArray>,
                                    octaveCount: Int): Array<FloatArray>? {
        val width = baseNoise.size
        val height = baseNoise[0].size
        val smoothNoise = ArrayList<Array<FloatArray>>(octaveCount) // an array of 2D
        // arrays
        // containing
        val persistance = 0.5f

        // generate smooth noise
        for (i in 0 until octaveCount) {
            smoothNoise[i] = generateSmoothNoise(baseNoise, i)
        }

        val perlinNoise = Array(width) { FloatArray(height) }
        var amplitude = 0.0f // the bigger, the more big mountains
        var totalAmplitude = 0.0f

        // blend noise together
        for (octave in octaveCount - 1 downTo 0) {
            amplitude *= persistance
            totalAmplitude += amplitude
            for (i in 0 until width) {
                for (j in 0 until height) {
                    perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude
                }
            }
        }
        for (i in 0 until width) {
            for (j in 0 until height) {
                perlinNoise[i][j] /= totalAmplitude
                perlinNoise[i][j] = floor(perlinNoise[i][j] * 25.0).toFloat()
            }
        }
        return perlinNoise
    }

    operator fun get(x: Int, y: Int) = noise[x][y]

    private var noise = ArrayList<FloatArray>(0)

    fun generate( width: Int, height: Int) {
        val noise = generatePerlinNoise(generateWhiteNoise(width, height), 16)
    }
//...
}