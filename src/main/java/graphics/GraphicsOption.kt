package graphics

import org.lwjgl.opengl.GL11.*

enum class GraphicsOption(val index: Int) {

    FACE_CULLING(GL_CULL_FACE),
    DEPTH_TESTING(GL_DEPTH_TEST),
    ALPHA_BLENDING(GL_BLEND),
    TEXTURE_MAPPING(GL_TEXTURE),
    DISTANCE_CLIPPING(GL_CLIP_PLANE0),
    DEPTH_BUFFER_BIT(GL_DEPTH_BUFFER_BIT),
    COLOR_BUFFER_BIT(GL_COLOR_BUFFER_BIT);

    fun enable() {
        if (!glIsEnabled(index)) {
            glEnable(index)
        }
    }

    fun disable() {
        if (glIsEnabled(index)) {
            glDisable(index)
        }
    }

}