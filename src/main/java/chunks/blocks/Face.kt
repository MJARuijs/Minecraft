package chunks.blocks

import math.vectors.Vector3

enum class Face(val normal: Vector3) {

    FRONT(Vector3(0, 0, 1)),
    BACK(Vector3(0, 0, -1)),
    LEFT(Vector3(-1, 0, 0)),
    RIGHT(Vector3(1, 0, 0)),
    TOP(Vector3(0, 1, 0)),
    BOTTOM(Vector3(0, -1, 0)),
    ALL(Vector3(0, 0, 0))

}