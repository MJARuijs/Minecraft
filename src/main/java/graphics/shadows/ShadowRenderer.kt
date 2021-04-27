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
import math.matrices.Matrix4
import math.vectors.Vector3
import org.lwjgl.opengl.GL11.glClearColor
import java.lang.Math.PI

object ShadowRenderer {

    private val shadowProgram = ShaderProgram.load("shaders/entities/shadowBlock.vert", "shaders/entities/shadowBlock.frag")
    private val shadowBoxes = ArrayList<ShadowBox>()

    private val renderTarget = RenderTarget(512, 512)
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
            shadowProgram.set("projection", camera.projectionMatrix)
            val mat = Matrix4().rotateY(PI.toFloat()).translate(Vector3(0.0f, 0.0f, -20.0f))
            shadowProgram.set("view", mat)
            println(box.viewMatrix)

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
            glClearColor(0.0f, 1.0f, 1.0f, 1.0f)

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