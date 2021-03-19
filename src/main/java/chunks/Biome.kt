package chunks

import math.Color

enum class Biome(val octaves: Int, val amplitude: Double, val roughness: Double, val overlayColor: Color) {

    PLANES(5, 20.0, 0.35, Color(50, 200, 50)),
    MOUNTAINS(4, 80.0, 0.5, Color(50, 200, 50))

}