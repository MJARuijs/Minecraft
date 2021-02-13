package math.matrices

import math.Axis
import math.Quaternion
import math.vectors.Vector2
import math.vectors.Vector3
import math.vectors.Vector4

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

    fun rotate(quaternion: Quaternion) = transform(quaternion.toMatrix())

    fun rotateX(angle: Float) = rotate(Quaternion(Axis.X, angle))

    fun rotateY(angle: Float) = rotate(Quaternion(Axis.Y, angle))

    fun rotateZ(angle: Float) = rotate(Quaternion(Axis.Z, angle))

    fun rotate(vector: Vector3) = rotateX(vector.x).rotateY(vector.y).rotateZ(vector.z)

}