package graphics.model.mesh

import org.lwjgl.opengl.GL11.*

enum class DataType(val code: Int, val size: Int) {

    FLOAT(GL_FLOAT, 4),
    INT(GL_INT, 4),
    UINT(GL_UNSIGNED_INT, 4),
    BYTE(GL_BYTE, 1),
    UBYTE(GL_UNSIGNED_BYTE, 1)

}