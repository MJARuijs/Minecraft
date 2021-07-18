package graphics.model.animation

import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Joint(val name: String, val id: Int, val children: List<Joint> = arrayListOf(), var transformation: Matrix4 = Matrix4(), var inverseBindMatrix: Matrix4 = Matrix4()) {

    fun loadTransformation(shaderProgram: ShaderProgram, print: Boolean) {
        shaderProgram.set("boneMatrices[$id]", transformation)

        if (print) {
            if (name == "Torso_Bone") {
//                transformation[2, 3] += 10f
                println("$name $transformation")
            }
        }
        for (child in children) {
            child.loadTransformation(shaderProgram, print)
        }
    }

    fun setTransformation(id: Int, transformation: Matrix4) {
        if (this.id == id) {
            this.transformation = transformation
        } else {
            for (child in children) {
                child.setTransformation(id, transformation)
            }
        }
    }

    fun setInverseBindMatrix(id: Int, bindMatrix: Matrix4) {
        if (this.id == id) {
            inverseBindMatrix = bindMatrix
        } else {
            for (child in children) {
                child.setInverseBindMatrix(id, bindMatrix)
            }
        }
    }

}