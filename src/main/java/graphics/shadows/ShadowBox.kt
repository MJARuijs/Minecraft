package graphics.shadows

import graphics.Camera
import graphics.lights.Sun
import graphics.rendertarget.RenderTargetManager
import math.matrices.Matrix4
import math.vectors.Vector3
import math.vectors.Vector4
import kotlin.math.*

class ShadowBox(camera: Camera, val maxDistance: Float = 50f) {

    private val offset = 15f
    private val nearWidth = camera.zNear * tan(Math.toRadians(camera.fieldOfView.toDouble()).toFloat())
    private val farWidth = maxDistance * tan(Math.toRadians(camera.fieldOfView.toDouble()).toFloat())

    private val nearHeight: Float
    private val farHeight: Float

    private var minX = 0.0f
    private var maxX = 0.0f
    private var minY = 0.0f
    private var maxY = 0.0f
    private var minZ = 0.0f
    private var maxZ = 0.0f

    private var projectionMatrix = Matrix4()
    private var viewMatrix = Matrix4()

    init {
        val defaultAspectRatio = RenderTargetManager.getDefault().getAspectRatio()
        nearHeight = nearWidth / defaultAspectRatio
        farHeight = farWidth / defaultAspectRatio
    }

    private fun height() = abs(maxY - minY)

    private fun width() = abs(maxX - minX)

    private fun depth() = abs(maxZ - minZ)

    fun updateBox(camera: Camera, sun: Sun) {
        minX = 0.0f
        maxX = 0.0f
        minY = 0.0f
        maxY = 0.0f
        minZ = 0.0f
        maxZ = 0.0f

        val position = camera.position

        val lightDirection = -sun.direction
        val horizontalDirection = -sun.direction.xz()

        horizontalDirection.normalize()
        lightDirection.normalize()

        val xRotation = asin(-lightDirection.y)
        val yRotation = if (horizontalDirection.x == 0.0f && horizontalDirection.y <= 0.0f) {
            0.0f
        } else if (horizontalDirection.x == 0.0f && horizontalDirection.y > 0.0f) {
            PI.toFloat()
        } else if (horizontalDirection.x < 0.0f) {
            -acos(-horizontalDirection.y)
        } else {
            acos(-horizontalDirection.y)
        }

        var lightRotation = Matrix4()
        lightRotation = lightRotation.rotateX(xRotation)
        lightRotation = lightRotation.rotateY(yRotation)

        var inverseLightDirection = Matrix4()
        inverseLightDirection = inverseLightDirection.rotateY(-yRotation)
        inverseLightDirection = inverseLightDirection.rotateX(-xRotation)

        val cameraRotation = camera.rotationMatrix
        val totalRotation = lightRotation dot cameraRotation

        val nearX = nearWidth / 2.0f
        val nearY = nearHeight / 2.0f

        val farX = farWidth / 2.0f
        val farY = farHeight / 2.0f

        val points = ArrayList<Vector3>()

        points += Vector3(nearX, nearY, -camera.zNear + offset)
        points += Vector3(-nearX, nearY, -camera.zNear + offset)
        points += Vector3(-nearX, -nearY, -camera.zNear + offset)
        points += Vector3(nearX, -nearY, -camera.zNear + offset)
        points += Vector3(farX, farY, -maxDistance)
        points += Vector3(-farX, farY, -maxDistance)
        points += Vector3(-farX, -farY, -maxDistance)
        points += Vector3(farX, -farY, -maxDistance)

        for (i in points.indices) {
            val point = (totalRotation dot Vector4(points[i], 1.0f)).xyz()

            if (point.x < minX) minX = point.x
            if (point.x > maxX) maxX = point.x
            if (point.y < minY) minY = point.y
            if (point.y > maxY) maxY = point.y
            if (point.z < minZ) minZ = point.z
            if (point.z > maxZ) maxZ = point.z
        }

        val x = (maxX + minX) / 2.0f
        val y = (maxY + minY) / 2.0f
        val z = maxZ

        val translation = (inverseLightDirection dot Vector4(x, y, z, 1.0f)).xyz()

        viewMatrix = lightRotation.translate(-translation - position)

        updateProjectionMatrix()

//        println("$xRotation $yRotation")


//        println("$x $y $z")
//        println(inverseLightDirection)
//        println(lightRotation)
//        println(viewMatrix)
//        println()
//        println()
//        println()
    }

    private fun updateProjectionMatrix() {
        projectionMatrix = Matrix4()
        projectionMatrix[0, 0] = 2.0f / width()
        projectionMatrix[1, 1] = 2.0f / height()
        projectionMatrix[2, 2] = -2.0f / depth()
        projectionMatrix[2, 3] = -1.0f
        projectionMatrix[3, 3] = 1.0f
    }

    fun getProjectionMatrix(): Matrix4 {
        return projectionMatrix
    }

    fun getViewMatrix(): Matrix4 {
        return viewMatrix
    }
}
