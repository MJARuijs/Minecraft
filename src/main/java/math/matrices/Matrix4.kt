package math.matrices

import math.Axis
import math.Quaternion
import math.vectors.Vector2
import math.vectors.Vector3
import math.vectors.Vector4
import kotlin.math.atan2
import kotlin.math.sqrt

class Matrix4(elements: FloatArray = generateIdentityElements(4)): Matrix<Matrix4>(4, elements) {

    constructor(matrix: Matrix2): this(Matrix3(matrix))

    constructor(matrix: Matrix3): this(floatArrayOf(
            matrix[0], matrix[1], matrix[2], 0.0f,
            matrix[3], matrix[4], matrix[5], 0.0f,
            matrix[6], matrix[7], matrix[8], 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    ))

    constructor(matrix: Matrix4): this(matrix.elements)

    override fun create(elements: FloatArray) = Matrix4(elements)

    infix fun dot(vector: Vector4): Vector4 {
        val result = Vector4()
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                result[r] += this[r, c] * vector[c]
            }
        }
        return result
    }

    infix fun dot(vector: Vector3) = Vector3(dot(Vector4(vector)))

    fun translate(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f) = transform(
            Matrix4(floatArrayOf(
                    1.0f, 0.0f, 0.0f, x,
                    0.0f, 1.0f, 0.0f, y,
                    0.0f, 0.0f, 1.0f, z,
                    0.0f, 0.0f, 0.0f, 1.0f
            ))
    )

    fun translate(vector: Vector2) = translate(vector.x, vector.y)

    fun translate(vector: Vector3) = translate(vector.x, vector.y, vector.z)

    fun scale(x: Float = 1.0f, y: Float = 1.0f, z: Float = 1.0f) = transform(
            Matrix4(floatArrayOf(
                    x, 0.0f, 0.0f, 0.0f,
                    0.0f, y, 0.0f, 0.0f,
                    0.0f, 0.0f, z, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
            ))
    )

    fun scale(vector: Vector2) = scale(vector.x, vector.y)

    fun scale(vector: Vector3) = scale(vector.x, vector.y, vector.z)

    fun rotate(quaternion: Quaternion): Matrix4 {
        val scale = quaternion.toMatrix().getScale()
        return transform(quaternion.toMatrix()).scale(Vector3(1f / scale.x, 1f / scale.y, 1f / scale.z))
    }

    fun rotateX(angle: Float) = rotate(Quaternion(Axis.X, angle))

    fun rotateY(angle: Float) = rotate(Quaternion(Axis.Y, angle))

    fun rotateZ(angle: Float) = rotate(Quaternion(Axis.Z, angle))

    fun rotate(vector: Vector3) = rotateX(vector.x).rotateY(vector.y).rotateZ(vector.z)

    fun scalePosition(scale: Vector3): Matrix4 {
        set(0, 3, get(0, 3) * scale.x)
        set(1, 3, get(1, 3) * scale.y)
        set(2, 3, get(2, 3) * scale.z)
        return this
    }

    fun getPosition(): Vector3 {
        val x = get(0, 3)
        val y = get(1, 3)
        val z = get(2, 3)
        return Vector3(x, y, z)
    }

    fun getRotation(): Vector3 {
        val scale = Vector3(1f / getScale().x, 1f / getScale().y, 1f / getScale().z)
        val scaledMatrix = this.scale(scale)
        val xRotation = atan2(scaledMatrix.get(2, 1), scaledMatrix.get(2, 2))
        val yRotation = atan2(-scaledMatrix.get(2, 0), sqrt(scaledMatrix.get(2,1 ) * scaledMatrix.get(2, 1) + scaledMatrix.get(2, 2) * scaledMatrix.get(2, 2)))
        val zRotation = atan2(scaledMatrix.get(1, 0), scaledMatrix.get(0, 0))
        return Vector3(xRotation, yRotation, zRotation)
    }

    fun getRotationMatrix(): Matrix4 {
        val scale = getScale()
        val rotation = Matrix4()
        rotation[0, 0] = get(0, 0) / scale.x
        rotation[1, 0] = get(1, 0) / scale.x
        rotation[2, 0] = get(2, 0) / scale.x

        rotation[0, 1] = get(0, 1) / scale.y
        rotation[1, 1] = get(1, 1) / scale.y
        rotation[2, 1] = get(2, 1) / scale.y

        rotation[0, 2] = get(0, 2) / scale.z
        rotation[1, 2] = get(1, 2) / scale.z
        rotation[2, 2] = get(2, 2) / scale.z
        return rotation
    }

    fun getScale(): Vector3 {
        val x = Vector3(get(0, 0), get(1, 0), get(2, 0))
        val y = Vector3(get(0, 1), get(1, 1), get(2, 1))
        val z = Vector3(get(0, 2), get(1, 2), get(2, 2))
        return Vector3(x.length(), y.length(), z.length())
    }

}
