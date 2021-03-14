package chunks

import chunks.blocks.ChunkRenderer
import graphics.Camera
import graphics.rendertarget.RenderTargetManager
import org.lwjgl.opengl.GL11.glClearColor

class Selector {

    fun findSelectedItem(camera: Camera) {
        val fbo = RenderTargetManager.get()
        fbo.start()
        fbo.clear()
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        ChunkRenderer.renderColorCoded(camera)

        fbo.renderToScreen()
    }

}