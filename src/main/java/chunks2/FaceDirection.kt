package chunks2

import math.vectors.Vector3

enum class FaceDirection(val normal: Vector3, val vertices: FloatArray) {

    FRONT(Vector3(0, 0, 1), floatArrayOf(
            0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f
    )),
    BACK(Vector3(0, 0, -1), floatArrayOf(
            1.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
    )),
    TOP(Vector3(0, 1, 0), floatArrayOf(
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f
    )),
    BOTTOM(Vector3(0, -1, 0), floatArrayOf(
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 1.0f
    )),
    RIGHT(Vector3(1, 0, 0), floatArrayOf(
            1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    )),
    LEFT(Vector3(-1, 0, 0),  floatArrayOf(
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f
    ));

    fun getOpposite(): FaceDirection {
        return when (this) {
            FRONT -> BACK
            BACK -> FRONT
            LEFT -> RIGHT
            RIGHT -> LEFT
            TOP -> BOTTOM
            BOTTOM -> TOP
        }
    }
}