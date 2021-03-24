package chunks.blocks

import chunks.Chunk
import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.ChunkRenderer
import devices.Window
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.rendertarget.RenderTargetManager
import math.vectors.Vector3
import math.vectors.Vector4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import util.FloatUtils
import kotlin.math.roundToInt

class Selector {

    private val reach = 8.0f
    private var lastSelected: Pair<Chunk, Vector3>? = null

    fun getLastSelected() = lastSelected

    fun findSelectedItem(window: Window, chunkRenderer: ChunkRenderer, chunks: ArrayList<Chunk>, camera: Camera, render: Boolean): Pair<Chunk, Vector3>? {
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING)
        val fbo = RenderTargetManager.get()
        fbo.start()
        fbo.clear()
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        val reachableChunks = ArrayList<Chunk>()

        for (chunk in chunks) {
            if ((camera.position.xz() - chunk.getCenter()).length() <= CHUNK_SIZE * 2) {
                reachableChunks += chunk
            }
        }

        val stepSize = chunkRenderer.renderSubset(camera, reachableChunks) { blockPosition ->
            (blockPosition - camera.position).length() < reach
        }

        val x = window.width / 2
        val y = window.height / 2

        val pixelData = BufferUtils.createFloatBuffer(3)
        glReadPixels(x, y, 1, 1, GL_RGB, GL_FLOAT, pixelData)

        if (render) {
            fbo.renderToScreen()
        } else {
            fbo.stop()
        }

        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING)

        val r = FloatUtils.roundToDecimal(pixelData.get(), 3)
        val g = FloatUtils.roundToDecimal(pixelData.get(), 3)
        val b = FloatUtils.roundToDecimal(pixelData.get(), 3)

        val id = decodeId(r, g, b, stepSize)

        if (id == -1) {
            lastSelected = null
            return null
        }

        var blockCount = 0

        for (chunk in reachableChunks) {
            if (id < chunk.getSubsetSize() + blockCount) {
                val blockPosition = chunk.getSubsetPosition(id - blockCount)
                lastSelected = Pair(chunk, blockPosition)
                return lastSelected
            } else {
                blockCount += chunk.getSubsetSize()
            }

        }

        return lastSelected
    }

    fun determineSelectedFace(camera: Camera, position: Vector3): Face? {
        val clipCoords = Vector4(0f, 0f, -1f, 1f)
        val eyeSpace = camera.projectionMatrix.inverse().dot(clipCoords)
        eyeSpace.z = -1f
        eyeSpace.w = 0f
        val ray = camera.viewMatrix.inverse().dot(eyeSpace).xyz().normal()
        val toCameraDistance = (camera.position - position).length()

        var smallestDistance = Float.MAX_VALUE
        var closestFace: Face? = null

        for (face in Face.values()) {
            if ((face.normal * -1f).dot(ray) < 0.0f) {
                continue
            }

            val facePosition = position + face.normal * 0.5f

            val p = camera.position + ray * (toCameraDistance - 0.5f)
            val distance = (facePosition - p).length()

            if (distance < smallestDistance) {
                smallestDistance = distance
                closestFace = face
            }
        }

        return closestFace
    }

//    fun render(camera: Camera) {
//        shaderProgram.start()
//        glLineWidth(2.0f)
//        shaderProgram.set("projection", camera.projectionMatrix)
//        shaderProgram.set("view", camera.viewMatrix)
//        for (mesh in meshes) {
//            mesh.draw()
//        }
//        shaderProgram.stop()
//    }

    private fun decodeId(r: Float, g: Float, b: Float, stepSize: Float): Int {
        if (g == 0.0f && b == 0.0f) {
            return (r / stepSize / 4.0f).roundToInt()
        }
        if (r == 1.0f && b == 0.0f) {
            return ((g + 1.0f) / stepSize / 4.0f).roundToInt()
        }
        if (r == 0.0f && g == 1.0f) {
            return ((b + 2.0f) / stepSize / 4.0f).roundToInt()
        }
        if (g == 0.0f && b == 1.0f) {
            return ((r + 3.0f) / stepSize / 4.0f).roundToInt()
        }
        return -1
    }
}