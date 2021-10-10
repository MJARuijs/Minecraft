package graphics

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_MULTISAMPLE

enum class GraphicsOption(val index: Int) {

    FACE_CULLING(GL_CULL_FACE),
    DEPTH_TESTING(GL_DEPTH_TEST),
    STENCIL_TESTING(GL_STENCIL_TEST),
    ALPHA_BLENDING(GL_BLEND),
    TEXTURE_MAPPING(GL_TEXTURE),
    MULTI_SAMPLE(GL_MULTISAMPLE),
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