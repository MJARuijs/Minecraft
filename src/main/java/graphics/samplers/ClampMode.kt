package graphics.samplers

import org.lwjgl.opengl.GL11.GL_REPEAT
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE

enum class ClampMode(val handle: Int) {

    REPEAT(GL_REPEAT),
    EDGE(GL_CLAMP_TO_EDGE)

}