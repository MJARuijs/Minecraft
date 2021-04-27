package graphics.model

import math.vectors.Vector3

class Segment(var a: Vector3, var b: Vector3) {

    fun toArray() = a.toArray() + b.toArray()

}