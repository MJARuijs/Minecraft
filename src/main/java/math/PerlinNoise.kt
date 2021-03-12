package math

import java.util.*
import kotlin.math.cos
import kotlin.math.pow

class PerlinNoise(
        private val octaves: Int,
        private val amplitude: Float,
        private val roughness: Float,
        private val seed: Long = Random().nextInt(1000000).toLong()
) {

    private val random = Random()

    operator fun get(x: Float, y: Float): Float {

        var height = 0.0f
        val delta = power(2.0f, (octaves - 1.0f))

        for (index in 0 until octaves) {
            val exponent = index.toFloat()
            val frequency = power(2.0f, exponent) / delta
            val amplitude = power(roughness, exponent) * amplitude
            height += getInterpolated(x * frequency, y * frequency) * amplitude
        }

        return height
    }

    private fun getInterpolated(x: Float, y: Float): Float {

        val v1 = getSmooth(x, y)
        val v2 = getSmooth(x + 1, y)
        val v3 = getSmooth(x, y + 1)
        val v4 = getSmooth(x + 1, y + 1)

        val i1 = interpolate(v1, v2, x % 1.0f)
        val i2 = interpolate(v3, v4, x % 1.0f)

        return interpolate(i1, i2, y % 1.0f)
    }

    private fun getSmooth(x: Float, y: Float) = getSmooth(x.toInt(), y.toInt())

    private fun getSmooth(x: Int, y: Int): Float {

        val corners = (getRandom(x - 1, y - 1)
                + getRandom(x + 1, y - 1)
                + getRandom(x - 1, y + 1)
                + getRandom(x + 1, y + 1)) / 16f

        val sides = (getRandom(x - 1, y)
                + getRandom(x + 1, y)
                + getRandom(x, y - 1)
                + getRandom(x, y + 1)) / 8f

        val center = getRandom(x, y) / 4f

        return corners + sides + center
    }

    private fun getRandom(x: Int, y: Int): Float {
        random.setSeed(seed + (x * 49632) + (y * 325176))
        return (random.nextFloat() * 2.0f) - 1.0f
    }

    private fun power(base: Float, exponent: Float) = base.toDouble().pow(exponent.toDouble()).toFloat()

    private fun interpolate(a: Float, b: Float, blend: Float): Float {
        val theta = blend * Math.PI.toFloat()
        val factor = (1.0f - cos(theta)) * 0.5f
        return a * (1.0f - factor) + b * factor
    }

}
