package graphics.shaders

import graphics.samplers.Sampler
import math.Color
import math.matrices.Matrix2
import math.matrices.Matrix3
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import math.vectors.Vector4
import resources.Resource
import org.lwjgl.opengl.GL11.GL_TRUE
import org.lwjgl.opengl.GL20.*

class ShaderProgram(shaders: List<Shader>): Resource {

    companion object {

        fun load(vararg paths: String) = ShaderProgram(paths.map(ShaderCache::get))

    }

    constructor(vararg shaders: Shader): this(shaders.toList())

    private val handle = glCreateProgram()
    private val uniforms = HashMap<String, Int>()

    init {

        for (shader in shaders) {
            glAttachShader(handle, shader.handle)
        }

        glLinkProgram(handle)
        val linked = glGetProgrami(handle, GL_LINK_STATUS)
        if (linked != GL_TRUE) {
            val log = glGetProgramInfoLog(handle)
            throw IllegalArgumentException("Could not link shader program:\n$log")
        }

        glValidateProgram(handle)
        val validated = glGetProgrami(handle, GL_VALIDATE_STATUS)
        if (validated != GL_TRUE) {
            val log = glGetProgramInfoLog(handle)
            throw IllegalArgumentException("Could not validate shader program:\n$log")
        }
    }

    fun start() {
        glUseProgram(handle)
    }

    fun stop() {
        glUseProgram(0)
    }

    private fun getUniformLocation(name: String) = uniforms.computeIfAbsent(name) {
        glGetUniformLocation(handle, name)
    }

    private fun <T> set(name: String, value: T, setter: (Int, T) -> Unit) {
        val location = getUniformLocation(name)
        setter.invoke(location, value)
    }

    private fun <T, R> set(name: String, value1: T, value2: R, setter: (Int, T, R) -> Unit) {
        val location = getUniformLocation(name)
        setter.invoke(location, value1, value2)
    }

    fun set(name: String, value: Int) = set(name, value, ::glUniform1i)
    fun set(name: String, value: Float) = set(name, value, ::glUniform1f)

    fun set(name: String, vector: Vector2) = set(name, vector.toArray(), ::glUniform2fv)
    fun set(name: String, vector: Vector3) = set(name, vector.toArray(), ::glUniform3fv)
    fun set(name: String, vector: Vector4) = set(name, vector.toArray(), ::glUniform4fv)

    fun set(name: String, matrix: Matrix2) = set(name, true, matrix.elements, ::glUniformMatrix2fv)
    fun set(name: String, matrix: Matrix3) = set(name, true, matrix.elements, ::glUniformMatrix3fv)
    fun set(name: String, matrix: Matrix4) = set(name, true, matrix.elements, ::glUniformMatrix4fv)

    fun set(name: String, value: Boolean) = set(name, if (value) 1 else 0)
    fun set(name: String, color: Color) = set(name, color.toArray(), ::glUniform4fv)
    fun set(name: String, sampler: Sampler) = set(name, sampler.index)

    override fun destroy() {
        glDeleteProgram(handle)
    }

}