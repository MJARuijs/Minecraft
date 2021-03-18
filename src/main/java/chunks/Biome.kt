package chunks

import math.Color

enum class Biome(val octaves: Int, val amplitude: Float, val roughness: Float, val overlayColor: Color) {

    PLANES(5, 20f, 0.35f, Color(50, 200, 50)),
    MOUNTAINS(4, 80f, 0.5f, Color(50, 200, 50))

}