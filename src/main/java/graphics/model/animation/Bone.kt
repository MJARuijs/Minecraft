package graphics.model.animation

import math.matrices.Matrix4

class Bone(val id: String, val transformation: Matrix4, val children: List<Bone> = arrayListOf()) {
}