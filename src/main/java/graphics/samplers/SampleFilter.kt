package graphics.samplers

import org.lwjgl.opengl.GL11.GL_LINEAR
import org.lwjgl.opengl.GL11.GL_NEAREST

enum class SampleFilter(val handle: Int) {

    NEAREST(GL_NEAREST),
    LINEAR(GL_LINEAR)

}