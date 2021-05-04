package chunks.blocks

import math.vectors.Vector3

enum class Direction(val normal: Vector3) {

    FRONT(Vector3(0, 0, 1)),
    BACK(Vector3(0, 0, -1)),
    UP(Vector3(0, 1, 0)),
    DOWN(Vector3(0, -1, 0)),
    RIGHT(Vector3(1, 0, 0)),
    LEFT(Vector3(-1, 0, 0))

}