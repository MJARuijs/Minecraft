package graphics.model.animation

import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Joint(val name: String, val id: Int, val children: List<Joint> = arrayListOf(), private var localTransformation: Matrix4) {

    private var inverseBindMatrix = Matrix4()
    private var animatedTransform = Matrix4()

    fun initWorldTransformation(parentTransform: Matrix4) {
        val worldTransformation = parentTransform dot localTransformation
        animatedTransform = worldTransformation dot inverseBindMatrix

        for (child in children) {
            child.initWorldTransformation(worldTransformation)
        }
    }

    fun calculateAnimatedTransformation(currentWorldTransformation: Matrix4) {
        animatedTransform = currentWorldTransformation dot inverseBindMatrix
    }

    fun loadTransformation(shaderProgram: ShaderProgram) {
        shaderProgram.set("boneMatrices[$id]", animatedTransform)

        for (child in children) {
            child.loadTransformation(shaderProgram)
        }
    }

    fun setInverseBindMatrix(id: Int, inverseBindMatrix: Matrix4) {
        if (this.id == id) {
            this.inverseBindMatrix = inverseBindMatrix
        } else {
            for (child in children) {
                child.setInverseBindMatrix(id, inverseBindMatrix)
            }
        }
    }
}