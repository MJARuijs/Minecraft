package graphics.model.animation

import graphics.model.ModelLoader
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Joint(val name: String, val id: Int, val children: List<Joint> = arrayListOf(), private var localTransformation: Matrix4) {

    private val model = ModelLoader().load("models/sphere.dae")

    var worldTransformation = Matrix4()

    var inverseBindMatrix = Matrix4()
    var animatedTransform = Matrix4()

    fun initWorldTransformation(parentTransform: Matrix4) {
        worldTransformation = parentTransform dot localTransformation
        animatedTransform = worldTransformation dot inverseBindMatrix
        for (child in children) {
            child.initWorldTransformation(worldTransformation)
        }
    }

    fun getJoints(): List<Joint> {
        val children = ArrayList<Joint>()
        children += this
        for (child in this.children) {
            children += child.getJoints()
        }
        return children
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", worldTransformation)
        model.render(shaderProgram)
    }

    fun loadTransformation(shaderProgram: ShaderProgram, print: Boolean) {
        shaderProgram.set("boneMatrices[$id]", animatedTransform)

        for (child in children) {
            child.loadTransformation(shaderProgram, print)
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