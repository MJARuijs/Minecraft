package math

import java.util.*
import kotlin.math.cos
import kotlin.math.pow

class PerlinNoise(
        private val octaves: Int,
        private val amplitude: Double,
        private val roughness: Double,
        private val seed: Long = Random().nextInt(1000000).toLong()
) {

    private val random = Random()

    operator fun get(x: Int, z: Int) = get(x.toFloat(), z.toFloat())

    operator fun get(x: Float, z: Float): Float {
        var height = 0.0
        val delta = 2.0.pow(octaves - 1.0)

        for (i in 0 until octaves) {
            val exponent = i.toDouble()
            val frequency = (2.0.pow(exponent) / delta).toFloat()
            val amplitude = (roughness.pow(exponent)) * amplitude
            height += getInterpolatedNoise(x * frequency, z * frequency) * amplitude
        }

        return height.toFloat()
    }

    private fun getNoise(x: Int, z: Int): Float {
        random.setSeed(x * 49632 + z * 325176 + seed)
        return random.nextFloat()
    }

    private fun getInterpolatedNoise(x: Float, y: Float): Float {

        val v1 = getSmoothNoise(x, y)
        val v2 = getSmoothNoise(x + 1, y)
        val v3 = getSmoothNoise(x, y + 1)
        val v4 = getSmoothNoise(x + 1, y + 1)

        val i1 = interpolate(v1, v2, x % 1.0f)
        val i2 = interpolate(v3, v4, x % 1.0f)

        return interpolate(i1, i2, y % 1.0f)
    }

    private fun getSmoothNoise(x: Float, y: Float) = getSmoothNoise(x.toInt(), y.toInt())

    private fun getSmoothNoise(x: Int, y: Int): Float {
        val corners = (getNoise(x - 1, y - 1)
                + getNoise(x + 1, y - 1)
                + getNoise(x - 1, y + 1)
                + getNoise(x + 1, y + 1)) / 16f

        val sides = (getNoise(x - 1, y)
                + getNoise(x + 1, y)
                + getNoise(x, y - 1)
                + getNoise(x, y + 1)) / 8f

        val center = getNoise(x, y) / 4f

        return corners + sides + center
    }

    private fun interpolate(a: Float, b: Float, blend: Float): Float {
        val theta = blend * Math.PI.toFloat()
        val factor = (1.0f - cos(theta)) * 0.5f
        return a * (1.0f - factor) + b * factor
    }
}
