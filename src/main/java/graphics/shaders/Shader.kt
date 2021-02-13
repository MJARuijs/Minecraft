package graphics.shaders

import resources.Resource
import org.lwjgl.opengl.GL11.GL_TRUE
import org.lwjgl.opengl.GL20.*

class Shader(type: ShaderType, source: String): Resource {

    val handle = glCreateShader(type.index)

    init {
        glShaderSource(handle, source)

        glCompileShader(handle)
        val compiled = glGetShaderi(handle, GL_COMPILE_STATUS)
        if (compiled != GL_TRUE) {
            val log = glGetShaderInfoLog(handle, glGetShaderi(handle, GL_INFO_LOG_LENGTH))
            throw IllegalArgumentException("Could not compile shader: $type\n$log")
        }
    }

    override fun destroy() {
        glDeleteShader(handle)
    }

}