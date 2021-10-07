package environment.sky

import game.camera.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.model.mesh.*
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import resources.images.ImageCache
import resources.images.ImageData
import kotlin.math.sqrt

class SkyBox(path: String, cameraFarPlane: Float) {

    private val shaderProgram = ShaderProgram.load("shaders/environment/sky/skybox.vert", "shaders/environment/sky/skybox.frag")
    private val cube = MeshCache.get("models/box.dae")

    private val handle = glGenTextures()
    private val distance = sqrt((cameraFarPlane * cameraFarPlane) / 3.0f)

    init {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, handle)

        val correct = "front"

        val texturesData = ArrayList<ImageData>()
        texturesData += ImageCache.get("$path/$correct.jpg")
        texturesData += ImageCache.get("$path/$correct.jpg")
        texturesData += ImageCache.get("$path/top.jpg")
        texturesData += ImageCache.get("$path/bottom.jpg")
        texturesData += ImageCache.get("$path/$correct.jpg")
        texturesData += ImageCache.get("$path/$correct.jpg")

        for ((i, textureData) in texturesData.withIndex()) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, textureData.width, textureData.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData.data)
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0)
    }

    fun render(camera: Camera) {
        GraphicsContext.disable(GraphicsOption.FACE_CULLING)
        shaderProgram.start()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, handle)

        shaderProgram.set("cubeMap", 0)
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("distance", distance)

        cube.draw()
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0)
        shaderProgram.stop()
        GraphicsContext.enable(GraphicsOption.FACE_CULLING)
    }
}