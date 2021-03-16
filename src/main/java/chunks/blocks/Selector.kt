package chunks.blocks

import chunks.Chunk
import chunks.ChunkGenerator.CHUNK_SIZE
import chunks.ChunkRenderer
import devices.Window
import graphics.Camera
import graphics.rendertarget.RenderTargetManager
import graphics.shaders.ShaderProgram
import math.vectors.Vector3
import math.vectors.Vector4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import util.FloatUtils
import kotlin.math.roundToInt

class Selector {

    private val reach = 8.0f
    private var lastSelected: Pair<Chunk, Vector3>? = null

    private val shaderProgram = ShaderProgram.load("shaders/line.vert", "shaders/line.frag")

    private val meshes = ArrayList<LineMesh>()

    fun getLastSelected() = lastSelected

    fun findSelectedItem(window: Window, chunkRenderer: ChunkRenderer, chunks: ArrayList<Chunk>, camera: Camera): Pair<Chunk, Vector3>? {
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

        val stepSize = chunkRenderer.renderSubset(camera, reachableChunks) { visibleBlock ->
            (visibleBlock.second - camera.position).length() < reach
        }

        val x = window.width / 2
        val y = window.height / 2

        val pixelData = BufferUtils.createFloatBuffer(3)
        GL11.glReadPixels(
                x,
                y,
                1,
                1,
                GL11.GL_RGB,
                GL11.GL_FLOAT,
                pixelData
        )

        fbo.stop()
        val r = FloatUtils.roundToDecimal(pixelData.get(), 3)
        val g = FloatUtils.roundToDecimal(pixelData.get(), 3)
        val b = FloatUtils.roundToDecimal(pixelData.get(), 3)

        val id = decodeId(r, g, b, stepSize)

        if (id == -1) {
            lastSelected = null
            return  null
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

    fun determineSelectedFace(camera: Camera, position: Vector3): Face {
        val clipCoords = Vector4(0f, 0f, -1f, 1f)
        val eyeSpace = camera.projectionMatrix.inverse().dot(clipCoords)
        eyeSpace.z = -1f
        eyeSpace.w = 0f
        val ray = camera.viewMatrix.inverse().dot(eyeSpace).xyz().normal()

        val rx = camera.position.x
        val ry = camera.position.y
        val rz = camera.position.z

//        val vx = ray.x
//        val vy = ray.y
        val vz = ray.z

        //R0 = camera.position
        //D = ray

        val direction = (camera.position - position).normal()
        val ray2 = position + direction * 0.5f
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
//            println(distance)

            if (distance < smallestDistance) {
                smallestDistance = distance
                closestFace = face
            }


            // P0 = position - face.positionOffset
            // S1 = Vector3(1.0, 0.0, 0.0)
            // S2 = Vector3(0.0, 1.0, 0.0)
            // P = camera.position() + a * ray


            // P0P
            // a = ((P0 - camera.position).dot(face.normal) / (ray.dot(face.normal))

//            val s1 = face.sideOne
//            val s2 = face.sideTwo
//
            val p0 = position - face.positionOffset
            val a = ((p0 - camera.position).dot(face.normal) / (ray.dot(face.normal)))
//


//            println(p)
//
//            val p0p = p0 * p
//
//            val q1 = s1 * ((p0p.dot(s1)) / (s1.length()))
//            val q2 = s2 * ((p0p.dot(s2)) / (s2.length()))
//
//            println("$face $p")
//
//            if (q1.length() <= s1.length() && q2.length() <= s2.length()) {
//                println("WON $face")
//                meshes += LineMesh(floatArrayOf(camera.position.x, camera.position.y, camera.position.z, p.x, p.y, p.z))
////                meshes += LineMesh(floatArrayOf(0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f))
//            }

//            println("$face $p0 $p $a $position")


//            val t = (a * (x0 - rx) + b * (y0 - ry) + c * (x0 - rz)) / (a * vx + b * vy + c * vz)

        }

        println(closestFace)

        return Face.TOP
    }

    fun render(camera: Camera) {
        shaderProgram.start()
        glLineWidth(2.0f)
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        for (mesh in meshes) {
            mesh.draw()
        }
        shaderProgram.stop()

    }

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