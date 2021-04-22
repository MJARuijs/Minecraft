package userinterface.svg

import math.vectors.Vector2
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*
import resources.Resource

class SVGMesh(private var points: ArrayList<Vector2>, val xSize: Float, val ySize: Float) : Resource {

    private val vao = glGenVertexArrays()
    private val vbo = glGenBuffers()
    
    init {
        var vertices = FloatArray(0)
        points.forEach { point ->
            vertices += point.toArray()
        }

        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun draw() {
        glBindVertexArray(vao)

        glDrawArrays(GL11.GL_TRIANGLES, 0, points.size)
        glBindVertexArray(0)
    }

    override fun destroy() {
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }
}