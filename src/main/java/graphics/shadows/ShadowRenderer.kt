package graphics.shadows

import environment.terrain.chunks.Chunk
import environment.terrain.chunks.ChunkRenderer
import graphics.*
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.lights.Sun
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType

object ShadowRenderer {

    private const val SHADOW_MAP_SIZE = 4096 * 4

    private val renderTarget = RenderTargetManager.getAvailableTarget(AttachmentType.DEPTH_TEXTURE, width = SHADOW_MAP_SIZE, height = SHADOW_MAP_SIZE)
    private val shadowBoxes = ArrayList<ShadowBox>()

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
}