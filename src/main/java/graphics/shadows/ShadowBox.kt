package graphics.shadows

import graphics.Camera
import graphics.lights.Sun
import graphics.rendertarget.RenderTargetManager
import math.matrices.Matrix4
import math.vectors.Vector3
import math.vectors.Vector4
import kotlin.math.*

class ShadowBox(camera: Camera, val shadowDistance: Float = 50f) {

    private val offset = 15.0f

    private val nearWidth = camera.zNear * tan(Math.toRadians(camera.fieldOfView.toDouble())).toFloat()
    private val farWidth = shadowDistance * tan(Math.toRadians(camera.fieldOfView.toDouble())).toFloat()

    private val nearHeight: Float
    private val farHeight: Float

    var projectionMatrix = Matrix4()
        private set

    var viewMatrix = Matrix4()
        private set

    var minX = 0.0f
    var maxX = 0.0f
    var minY = 0.0f
    var maxY = 0.0f
    var minZ = 0.0f
    var maxZ = 0.0f

    init {
        val renderTarget = RenderTargetManager.getDefault()
        nearHeight = nearWidth / renderTarget.getAspectRatio()
        farHeight = farWidth / renderTarget.getAspectRatio()
    }

    fun width() = abs(maxX - minX)

    fun height() = abs(maxY - minY)

    fun depth() = abs(maxZ - minZ)

//    fun height() = if (maxY > minY) maxY - minY else minY - maxY
//
//    fun width() = if (maxX > minX) maxX - minX else minX - maxX
//
//    fun depth() = if (maxZ > minZ) maxZ - minZ else maxZ - minZ

    fun update(camera: Camera, sun: Sun) {
        val position = camera.position

        val lightDirection = -sun.direction
        val horizontalDirection = -sun.direction.xz()

        horizontalDirection.normalize()
        lightDirection.normalize()

        val xRotation = asin(-lightDirection.y)
        val yRotation = if (horizontalDirection.x == 0.0f && horizontalDirection.y <= 0.0f){
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

        var nearRightUp = Vector3(nearX, nearY, -camera.zNear + offset)
        var nearLeftUp = Vector3(-nearX, nearY, -camera.zNear + offset)
        var nearLeftDown = Vector3(-nearX, -nearY, -camera.zNear + offset)
        var nearRightDown = Vector3(nearX, -nearY, -camera.zNear + offset)
        var farRightUp = Vector3(farX, farY, -shadowDistance)
        var farLeftUp = Vector3(-farX, farY, -shadowDistance)
        var farLeftDown = Vector3(-farX, -farY, -shadowDistance)
        var farRightDown = Vector3(farX, -farY, -shadowDistance)

        nearRightUp = (totalRotation dot Vector4(nearRightUp, 1.0f)).xyz()
        nearLeftUp = (totalRotation dot Vector4(nearLeftUp, 1.0f)).xyz()
        nearLeftDown = (totalRotation dot Vector4(nearLeftDown, 1.0f)).xyz()
        nearRightDown = (totalRotation dot Vector4(nearRightDown, 1.0f)).xyz()
        farRightUp = (totalRotation dot Vector4(farRightUp, 1.0f)).xyz()
        farLeftUp = (totalRotation dot Vector4(farLeftUp, 1.0f)).xyz()
        farLeftDown = (totalRotation dot Vector4(farLeftDown, 1.0f)).xyz()
        farRightDown = (totalRotation dot Vector4(farRightDown, 1.0f)).xyz()

        val minNearX = min(nearLeftDown.x, min(nearRightDown.x, min(nearLeftUp.x, nearRightUp.x)))
        val minFarX = min(farLeftDown.x, min(farRightDown.x, min(farLeftUp.x, farRightUp.x)))
        minX = min(minNearX, minFarX)

        val maxNearX = max(nearLeftDown.x, max(nearRightDown.x, max(nearLeftUp.x, nearRightUp.x)))
        val maxFarX = max(farLeftDown.x, max(farRightDown.x, max(farLeftUp.x, farRightUp.x)))
        maxX = max(maxNearX, maxFarX)

        val minNearY = min(nearLeftDown.y, min(nearRightDown.y, min(nearLeftUp.y, nearRightUp.y)))
        val minFarY = min(farLeftDown.y, min(farRightDown.y, min(farLeftUp.y, farRightUp.y)))
        minY = min(minNearY, minFarY)

        val maxNearY = max(nearLeftDown.y, max(nearRightDown.y, max(nearLeftUp.y, nearRightUp.y)))
        val maxFarY = max(farLeftDown.y, max(farRightDown.y, max(farLeftUp.y, farRightUp.y)))
        maxY = max(maxNearY, maxFarY)

        val minNearZ = min(nearLeftDown.z, min(nearRightDown.z, min(nearLeftUp.z, nearRightUp.z)))
        val minFarZ = min(farLeftDown.z, min(farRightDown.z, min(farLeftUp.z, farRightUp.z)))
        minZ = min(minNearZ, minFarZ)

        val maxNearZ = max(nearLeftDown.z, max(nearRightDown.z, max(nearLeftUp.z, nearRightUp.z)))
        val maxFarZ = max(farLeftDown.z, max(farRightDown.z, max(farLeftUp.z, farRightUp.z)))
        maxZ = max(maxNearZ, maxFarZ)

        val x = (maxX + minX) / 2.0f
        val y = (maxY + minY) / 2.0f
        val z = maxZ

        val translation = (inverseLightDirection dot Vector4(x, y, z, 1.0f)).xyz() + position

        viewMatrix = lightRotation.translate(-translation)

        projectionMatrix = Matrix4()
        projectionMatrix[0, 0] = 2.0f / width()
        projectionMatrix[1, 1] = 2.0f / height()
        projectionMatrix[2, 2] = -2.0f / depth()
        projectionMatrix[2, 3] = -1.0f
        projectionMatrix[3, 3] = 1.0f
    }

    var nearRightUp = Vector3()
    var nearLeftUp = Vector3()
    var nearLeftDown = Vector3()
    var nearRightDown = Vector3()
    var farRightUp = Vector3()
    var farLeftUp = Vector3()
    var farLeftDown = Vector3()
    var farRightDown = Vector3()
    var translation = Vector3()

    var nearRightUp2 = Vector3()
    var nearLeftUp2 = Vector3()
    var nearLeftDown2 = Vector3()
    var nearRightDown2 = Vector3()
    var farRightUp2 = Vector3()
    var farLeftUp2 = Vector3()
    var farLeftDown2 = Vector3()
    var farRightDown2 = Vector3()
    var translation2 = Vector3()

    fun updateBox(camera: Camera, sun: Sun) {
        val position = camera.position

        val lightDirection = -sun.direction
        val horizontalDirection = -sun.direction.xz()

        horizontalDirection.normalize()
        lightDirection.normalize()

        val xRotation = asin(-lightDirection.y)
        val yRotation = if (horizontalDirection.x == 0.0f && horizontalDirection.y <= 0.0f){
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

        val nearMinX = -nearWidth / 2.0f
        val nearMaxX = nearWidth / 2.0f

        val nearMinY = -nearHeight / 2.0f
        val nearMaxY = nearHeight / 2.0f

        val farMaxX = farWidth / 2.0f
        val farMinX = -farWidth / 2.0f

        val farMinY = -farHeight / 2.0f
        val farMaxY = farHeight / 2.0f

        nearRightUp = Vector3(nearMaxX, nearMaxY, -camera.zNear + offset)
        nearLeftUp = Vector3(nearMinX, nearMaxY, -camera.zNear + offset)
        nearLeftDown = Vector3(nearMinX, nearMinY, -camera.zNear + offset)
        nearRightDown = Vector3(nearMaxX, nearMinY, -camera.zNear + offset)
        farRightUp = Vector3(farMaxX, farMaxY, -shadowDistance)
        farLeftUp = Vector3(farMinX, farMaxY, -shadowDistance)
        farLeftDown = Vector3(farMinX, farMinY, -shadowDistance)
        farRightDown = Vector3(farMaxX, farMinY, -shadowDistance)

        nearRightUp = (totalRotation dot Vector4(nearRightUp, 1.0f)).xyz()
        nearLeftUp = (totalRotation dot Vector4(nearLeftUp, 1.0f)).xyz()
        nearLeftDown = (totalRotation dot Vector4(nearLeftDown, 1.0f)).xyz()
        nearRightDown = (totalRotation dot Vector4(nearRightDown, 1.0f)).xyz()
        farRightUp = (totalRotation dot Vector4(farRightUp, 1.0f)).xyz()
        farLeftUp = (totalRotation dot Vector4(farLeftUp, 1.0f)).xyz()
        farLeftDown = (totalRotation dot Vector4(farLeftDown, 1.0f)).xyz()
        farRightDown = (totalRotation dot Vector4(farRightDown, 1.0f)).xyz()

        val minNearX = min(nearLeftDown.x, min(nearRightDown.x, min(nearLeftUp.x, nearRightUp.x)))
        val minFarX = min(farLeftDown.x, min(farRightDown.x, min(farLeftUp.x, farRightUp.x)))
        minX = min(minNearX, minFarX)

        val maxNearX = max(nearLeftDown.x, max(nearRightDown.x, max(nearLeftUp.x, nearRightUp.x)))
        val maxFarX = max(farLeftDown.x, max(farRightDown.x, max(farLeftUp.x, farRightUp.x)))
        maxX = max(maxNearX, maxFarX)

        val minNearY = min(nearLeftDown.y, min(nearRightDown.y, min(nearLeftUp.y, nearRightUp.y)))
        val minFarY = min(farLeftDown.y, min(farRightDown.y, min(farLeftUp.y, farRightUp.y)))
        minY = min(minNearY, minFarY)

        val maxNearY = max(nearLeftDown.y, max(nearRightDown.y, max(nearLeftUp.y, nearRightUp.y)))
        val maxFarY = max(farLeftDown.y, max(farRightDown.y, max(farLeftUp.y, farRightUp.y)))
        maxY = max(maxNearY, maxFarY)

        val minNearZ = min(nearLeftDown.z, min(nearRightDown.z, min(nearLeftUp.z, nearRightUp.z)))
        val minFarZ = min(farLeftDown.z, min(farRightDown.z, min(farLeftUp.z, farRightUp.z)))
        minZ = min(minNearZ, minFarZ)

        val maxNearZ = max(nearLeftDown.z, max(nearRightDown.z, max(nearLeftUp.z, nearRightUp.z)))
        val maxFarZ = max(farLeftDown.z, max(farRightDown.z, max(farLeftUp.z, farRightUp.z)))
        maxZ = max(maxNearZ, maxFarZ)

//        nearRightUp = (inverseLightDirection dot Vector4(nearRightUp, 1.0f)).xyz()
//        nearLeftUp = (inverseLightDirection dot Vector4(nearLeftUp, 1.0f)).xyz()
//        nearLeftDown = (inverseLightDirection dot Vector4(nearLeftDown, 1.0f)).xyz()
//        nearRightDown = (inverseLightDirection dot Vector4(nearRightDown, 1.0f)).xyz()
//        farRightUp = (inverseLightDirection dot Vector4(farRightUp, 1.0f)).xyz()
//        farLeftUp = (inverseLightDirection dot Vector4(farLeftUp, 1.0f)).xyz()
//        farLeftDown = (inverseLightDirection dot Vector4(farLeftDown, 1.0f)).xyz()
//        farRightDown = (inverseLightDirection dot Vector4(farRightDown, 1.0f)).xyz()
//
//        nearRightUp += position
//        nearLeftUp += position
//        nearLeftDown += position
//        nearRightDown += position
//        farRightUp += position
//        farLeftUp += position
//        farLeftDown += position
//        farRightDown += position

        val x = (maxX + minX) / 2.0f
        val y = (maxY + minY) / 2.0f
        val z = maxZ

        translation = Vector3(x, y, z)
        translation = (inverseLightDirection dot Vector4(translation, 1.0f)).xyz()
        translation += position
//        println("$position $translation")
        updateViewMatrix(sun)
        updateProjectionMatrix()
    }

    private fun updateProjectionMatrix() {
        projectionMatrix = Matrix4()
//        projectionMatrix[0, 0] = 2.0f / 10
//        projectionMatrix[1, 1] = 2.0f / 10
//        projectionMatrix[2, 2] = -2.0f / 15
//        println("${width()} ${height()} ${depth()}")
        projectionMatrix[0, 0] = 2.0f / width()
        projectionMatrix[1, 1] = 2.0f / height()
        projectionMatrix[2, 2] = -2.0f / depth()
        projectionMatrix[2, 3] = -1.0f
        projectionMatrix[3, 3] = 1.0f
    }

    private fun updateViewMatrix(sun: Sun) {
        val lightDirection = -sun.direction
        val horizontalDirection = -sun.direction.xz()

        horizontalDirection.normalize()
        lightDirection.normalize()

        val xRotation = asin(-lightDirection.y)
        val yRotation = if (horizontalDirection.x == 0.0f && horizontalDirection.y <= 0.0f){
            0.0f
        } else if (horizontalDirection.x == 0.0f && horizontalDirection.y > 0.0f) {
            PI.toFloat()
        } else if (horizontalDirection.x < 0.0f) {
            -acos(-horizontalDirection.y)
        } else {
            acos(-horizontalDirection.y)
        }

        viewMatrix = Matrix4()
        viewMatrix = viewMatrix.rotateX(xRotation)
        viewMatrix = viewMatrix.rotateY(yRotation)
//        viewMatrix = viewMatrix.rotateY(PI.toFloat() / 2.0f)
        viewMatrix = viewMatrix.translate(-translation)
//        viewMatrix = viewMatrix.rotateY(PI.toFloat())
//        viewMatrix = viewMatrix.translate(Vector3(0.0f, 0.0f, 20.0f))
//        println(translation)
    }
}