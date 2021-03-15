package chunks.blocks

import chunks.Chunk
import chunks.ChunkRenderer
import devices.Window
import graphics.Camera
import graphics.rendertarget.RenderTargetManager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glClearColor
import util.FloatUtils
import kotlin.math.roundToInt

class Selector {

    fun findSelectedItem(window: Window, chunkRenderer: ChunkRenderer, chunks: ArrayList<Chunk>, camera: Camera) {
        val fbo = RenderTargetManager.get()
        fbo.start()
        fbo.clear()
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        val stepSize = chunkRenderer.renderColorCoded(chunks, camera)

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

        val r = FloatUtils.roundToDecimal(pixelData.get(), 3)
        val g = FloatUtils.roundToDecimal(pixelData.get(), 3)
        val b = FloatUtils.roundToDecimal(pixelData.get(), 3)

        val id = decodeId(r, g, b, stepSize)
        println(id)
        println("$r, $g, $b")
        println()
        println()
        fbo.renderToScreen()
//        fbo.stop()
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