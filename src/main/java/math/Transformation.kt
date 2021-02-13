package math

import math.matrices.Matrix4
import math.vectors.Vector3

data class Transformation(
        var parent: Transformation? = null,
        var translation: Vector3 = Vector3(),
        var rotation: Quaternion = Quaternion(),
        var scale: Vector3 = Vector3(1.0f, 1.0f, 1.0f)
) {

    constructor(
            position: Vector3 = Vector3(),
            rotation: Quaternion = Quaternion(),
            scale: Vector3 = Vector3(1.0f, 1.0f, 1.0f)
    ): this(null, position, rotation, scale)

    fun getMatrix(): Matrix4 {
        val matrix = Matrix4()
                .translate(translation)
                .rotate(rotation)
                .scale(scale)
        return parent?.getMatrix()?.dot(matrix) ?: matrix
    }

    fun place(translation: Vector3): Transformation {
        this.translation = translation
        return this
    }

    fun move(delta: Vector3): Transformation {
        val rotationMatrix = Matrix4()
        rotationMatrix.rotate(rotation)
        translation += rotationMatrix.dot(delta)
        return this
    }

    fun turn(rotation: Quaternion) {
        this.rotation = rotation
    }

    fun rotate(delta: Quaternion) {
        rotation *= delta
    }

    fun resize(scale: Vector3) {
        this.scale = scale
    }

    fun scale(delta: Vector3) {
        scale *= delta
    }

    fun reset() {
        translation = Vector3()
        rotation = Quaternion()
        scale = Vector3(1.0f, 1.0f, 1.0f)
    }

}