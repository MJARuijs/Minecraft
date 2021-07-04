package graphics.model.animation

import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Bone(val name: String, val id: Int, var transformation: Matrix4, val children: List<Bone> = arrayListOf()) {

    fun loadTransformation(shaderProgram: ShaderProgram) {
        shaderProgram.set("boneMatrices[$id]", transformation)
//        println("Setting to shader: $id $name $transformation")
        for (child in children) {
            child.loadTransformation(shaderProgram)
        }
    }

    fun setTransformation(id: Int, transformation: Matrix4) {
        if (this.id == id) {
//            println("Applying to $id $name ")
            this.transformation = transformation
        } else {
            for (child in children) {
                child.setTransformation(id, transformation)
            }
        }
    }

}