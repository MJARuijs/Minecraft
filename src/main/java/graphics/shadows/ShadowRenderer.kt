package graphics.shadows

import chunks.Chunk
import chunks.ChunkRenderer
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.Quad
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram

object ShadowRenderer {

    private val shadowProgram = ShaderProgram.load("shaders/entities/shadowBlock.vert", "shaders/entities/shadowBlock.frag")
    private val shadowBoxes = ArrayList<ShadowBox>()

    private val renderTarget = RenderTarget(2048, 2048)
    private val depthProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/debug/depth.frag")
    private val quad = Quad()
    private val sampler = Sampler(0)

    operator fun plusAssign(box: ShadowBox) {
        shadowBoxes.add(box)
    }

    fun add(vararg boxes: ShadowBox) {
        shadowBoxes.addAll(boxes)
    }

    fun render(camera: Camera, sun: Sun, chunks: ArrayList<Chunk>, renderToScreen: Boolean = false, chunkRenderer: ChunkRenderer): List<ShadowData> {
        val shadowData = ArrayList<ShadowData>()

        renderTarget.start()
        renderTarget.clear()

        for (box in shadowBoxes) {
            box.updateBox(camera, sun)

            shadowProgram.start()
            shadowProgram.set("projection", box.projectionMatrix)
            shadowProgram.set("view", box.viewMatrix)

            chunkRenderer.renderBlack(chunks)

            shadowProgram.stop()

            shadowData += ShadowData(
                    box.projectionMatrix,
                    box.viewMatrix,
                    box.shadowDistance,
                    renderTarget.getDepthTexture()
            )
        }

        if (renderToScreen) {
            val depthTarget = RenderTargetManager.get()
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