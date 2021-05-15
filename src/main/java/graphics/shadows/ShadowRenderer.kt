package graphics.shadows

import environment.terrain.chunks.Chunk
import environment.terrain.chunks.ChunkRenderer
import graphics.*
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.lights.Sun
import graphics.renderer.RenderData
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType

class ShadowRenderer {

    private val renderTarget = RenderTargetManager.getAvailableTarget(AttachmentType.DEPTH_TEXTURE, width = SHADOW_MAP_SIZE, height = SHADOW_MAP_SIZE)

    fun render(camera: Camera, sun: Sun, shadowBoxes: List<ShadowBox>, renderData: List<RenderData>): List<ShadowData> {
        val shadowData = ArrayList<ShadowData>()

        renderTarget.start()
        renderTarget.clear()

        for (box in shadowBoxes) {
            box.updateBox(camera, sun)

            renderData.forEach {
                it.renderer.renderBlack(it.data, box.getProjectionMatrix(), box.getViewMatrix())
            }

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

    companion object {
        private const val SHADOW_MAP_SIZE = 4096 * 4
    }
}