package graphics.model.animation

import math.matrices.Matrix4

class Joint(val id: String, val transformation: Matrix4, val children: List<Joint>) {
}