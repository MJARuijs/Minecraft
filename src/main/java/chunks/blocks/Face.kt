package chunks.blocks

import math.vectors.Vector3

enum class Face(val normal: Vector3, val positionOffset: Vector3, val sideOne: Vector3, val sideTwo: Vector3) {

    FRONT(Vector3(0, 0, 1), Vector3(-0.5f, -0.5f, 0.5f), Vector3(1, 0, 0), Vector3(0, 1, 0)),
    BACK(Vector3(0, 0, -1), Vector3(0.5f, -0.5f, -0.5f), Vector3(-1, 0, 0), Vector3(0, 1, 0)),
    LEFT(Vector3(-1, 0, 0), Vector3(-0.5f, -0.5f, -0.5f), Vector3(0, 0, 1), Vector3(0, 1, 0)),
    RIGHT(Vector3(1, 0, 0), Vector3(0.5f, -0.5f, 0.5f), Vector3(0, 0, -1), Vector3(0, 1, 0)),
    TOP(Vector3(0, 1, 0), Vector3(-0.5f, 0.5f, 0.5f), Vector3(1, 0, 0), Vector3(0, 0, -1)),
    BOTTOM(Vector3(0, -1, 0), Vector3(-0.5f, -0.5f, -0.5f), Vector3(1, 0, 0), Vector3(0, 0, 1))

}