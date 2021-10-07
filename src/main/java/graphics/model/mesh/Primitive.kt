package graphics.model.mesh

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL32.GL_TRIANGLES_ADJACENCY

enum class Primitive(val code: Int) {
    POINT(GL_POINTS),
    LINE(GL_LINES),
    TRIANGLE(GL_TRIANGLES),
    TRIANGLE_ADJACENCY(GL_TRIANGLES_ADJACENCY),
    QUAD(GL_QUADS)

}