package graphics.shadows

import chunks.Chunk
import chunks.ChunkRenderer
import graphics.*
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram

object ShadowRenderer {

    private const val SHADOW_MAP_SIZE = 4096 * 5

    private val shadowProgram = ShaderProgram.load("shaders/entities/shadowBlock.vert", "shaders/entities/shadowBlock.frag")
    private val shadowBoxes = ArrayList<ShadowBox>()

    private val renderTarget = RenderTarget(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE)
    private val depthProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/debug/depth.frag")
    private val quad = Quad()
    private val sampler = Sampler(0)
    lateinit var depthTarget: RenderTarget

    operator fun plusAssign(box: ShadowBox) {
        shadowBoxes.add(box)
    }

    fun add(vararg boxes: ShadowBox) {
        shadowBoxes.addAll(boxes)
    }

    fun render(camera: Camera, sun: Sun, entities: List<Entity>, entityRenderer: EntityRenderer, chunks: ArrayList<Chunk>, renderToScreen: Boolean = false, chunkRenderer: ChunkRenderer): List<ShadowData> {
        val shadowData = ArrayList<ShadowData>()

        renderTarget.start()
        renderTarget.clear()

        for (box in shadowBoxes) {
            box.updateBox(camera, sun)
//            GraphicsContext.disable(GraphicsOption.FACE_CULLING)
            GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.ALPHA_BLENDING)

            shadowProgram.start()
            shadowProgram.set("projection", box.getProjectionMatrix())
            shadowProgram.set("view", box.getViewMatrix())

            chunkRenderer.renderBlack(chunks)
            shadowProgram.stop()

//            entityRenderer.renderColorLess(box.projectionMatrix, box.viewMatrix, entities)
            entityRenderer.renderColorLess(box.getProjectionMatrix(), box.getViewMatrix(), entities)

//            GraphicsContext.enable(GraphicsOption.FACE_CULLING)
            GraphicsContext.disable(GraphicsOption.DEPTH_TESTING, GraphicsOption.ALPHA_BLENDING)

            shadowData += ShadowData(
                    box.getProjectionMatrix(),
                    box.getViewMatrix(),
                    box.maxDistance,
                    renderTarget.getDepthTexture()
            )
        }

        if (renderToScreen) {
            depthTarget = RenderTargetManager.get()
            depthTarget.start()
            depthTarget.clear()
            sampler.bind(renderTarget.getDepthTexture())
            depthProgram.start()
            depthProgram.set("sampler", sampler.index)
            quad.draw()
            depthProgram.stop()
            depthTarget.renderToScreen()
            depthTarget.stop()
        } else {
            renderTarget.stop()
        }

        return shadowData
    }
}