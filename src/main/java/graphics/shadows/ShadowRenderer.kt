package graphics.shadows

import chunks2.Chunk
import chunks2.ChunkRenderer
import graphics.*
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.shaders.ShaderProgram

object ShadowRenderer {

    private const val SHADOW_MAP_SIZE = 4096 * 4
    private val shadowProgram = ShaderProgram.load("shaders/environment/terrain/shadowBlock.vert", "shaders/environment/terrain/shadowBlock.frag")

    private val shadowBoxes = ArrayList<ShadowBox>()

    private val renderTarget = RenderTarget(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE)

    operator fun plusAssign(box: ShadowBox) {
        shadowBoxes.add(box)
    }

    fun add(vararg boxes: ShadowBox) {
        shadowBoxes.addAll(boxes)
    }

    fun render(camera: Camera, sun: Sun, entities: List<Entity>, entityRenderer: EntityRenderer, chunks: ArrayList<Chunk>, chunkRenderer: ChunkRenderer): List<ShadowData> {
        val shadowData = ArrayList<ShadowData>()

        renderTarget.start()
        renderTarget.clear()

        for (box in shadowBoxes) {
            box.updateBox(camera, sun)

            chunkRenderer.renderBlack(chunks, box.getProjectionMatrix(), box.getViewMatrix())
            entityRenderer.renderColorLess(box.getProjectionMatrix(), box.getViewMatrix(), entities)

            shadowData += ShadowData(
                    box.getProjectionMatrix(),
                    box.getViewMatrix(),
                    box.maxDistance,
                    renderTarget.getDepthTexture()
            )
        }

        renderTarget.stop()

        return shadowData
    }

    fun render(camera: Camera, sun: Sun, entities: List<Entity>, entityRenderer: EntityRenderer, chunks: ArrayList<chunks.Chunk>, chunkRenderer: chunks.ChunkRenderer): List<ShadowData> {
        val shadowData = ArrayList<ShadowData>()

        renderTarget.start()
        renderTarget.clear()
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.ALPHA_BLENDING)

        for (box in shadowBoxes) {
            box.updateBox(camera, sun)

            shadowProgram.start()
            shadowProgram.set("projection", box.getProjectionMatrix())
            shadowProgram.set("view", box.getViewMatrix())

            chunkRenderer.renderBlack(chunks)

            shadowProgram.stop()
            entityRenderer.renderColorLess(box.getProjectionMatrix(), box.getViewMatrix(), entities)

            shadowData += ShadowData(
                    box.getProjectionMatrix(),
                    box.getViewMatrix(),
                    box.maxDistance,
                    renderTarget.getDepthTexture()
            )
        }
        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING, GraphicsOption.ALPHA_BLENDING)

        renderTarget.stop()

        return shadowData
    }
}