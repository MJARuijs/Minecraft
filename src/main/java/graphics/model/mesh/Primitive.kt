package graphics.model.mesh

import org.lwjgl.opengl.GL11.*

enum class Primitive(val code: Int) {
    POINT(GL_POINTS),
    LINE(GL_LINES),
    TRIANGLE(GL_TRIANGLES),
    QUAD(GL_QUADS)

}