package graphics.model.animation.model

import graphics.model.ModelLoader
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Joint(val name: String, val id: Int, val children: List<Joint> = arrayListOf(), private val localTransformation: Matrix4) {

    private val model = ModelLoader().load("models/sphere.dae")

    private var worldTransformation = Matrix4()
    private var inverseBindMatrix = Matrix4()
    private var animatedTransform = Matrix4()

    fun getDefaultTransformation() = localTransformation

    fun initWorldTransformation(parentTransform: Matrix4) {
        worldTransformation = parentTransform dot localTransformation
        animatedTransform = worldTransformation dot inverseBindMatrix

        for (child in children) {
            child.initWorldTransformation(worldTransformation)
        }
    }

    fun calculateAnimatedTransformation(currentWorldTransformation: Matrix4) {
        worldTransformation = currentWorldTransformation
//        println("$name $currentWorldTransformation")
        animatedTransform = currentWorldTransformation dot inverseBindMatrix
    }

    fun loadTransformation(shaderProgram: ShaderProgram) {
        shaderProgram.set("boneMatrices[$id]", animatedTransform)

//        println("$name $animatedTransform")

        for (child in children) {
            child.loadTransformation(shaderProgram)
        }
    }

    fun setInverseBindMatrix(inverseBindMatrix: Matrix4) {
        this.inverseBindMatrix = inverseBindMatrix
    }

    fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", worldTransformation)
        model.render(shaderProgram)
    }

    fun getJoints(): List<Joint> {
        val children = ArrayList<Joint>()
        children += this
        for (child in this.children) {
            children += child.getJoints()
        }
        return children
    }

    fun copy(): Joint {
        val joint = Joint(name, id, children.map { child -> child.copy() }, localTransformation)
        joint.inverseBindMatrix = inverseBindMatrix
        joint.worldTransformation = worldTransformation
        joint.animatedTransform = animatedTransform
        return joint
    }
}