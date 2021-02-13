package chunks.blocks

import chunks.Chunk
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.DirectionalLight
import graphics.shaders.ShaderProgram

object ChunkRenderer {

    private val shaderProgram = ShaderProgram.load("shaders/entities/block.vert", "shaders/entities/block.frag")
    private val chunks = ArrayList<Chunk>()

    operator fun plusAssign(chunk: Chunk) {
        chunks += chunk
    }

    fun render(camera: Camera, ambientLight: AmbientLight, directionalLight: DirectionalLight) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING)
        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("blockTextures[0]", 0)
        shaderProgram.set("blockTextures[1]", 1)

        ambientLight.apply(shaderProgram)
        directionalLight.apply(shaderProgram)

        for (chunk in chunks) {
            chunk.render()
        }

        shaderProgram.stop()
        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING)
    }
}