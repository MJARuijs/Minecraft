package graphics.samplers

import graphics.textures.TextureMap
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS
import org.lwjgl.opengl.GL33.glSamplerParameterf
import org.lwjgl.opengl.GL33.glSamplerParameteri
import org.lwjgl.opengl.GL46.GL_MAX_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.GL46.GL_TEXTURE_MAX_ANISOTROPY

data class Sampler(
    val index: Int,
    private var magnification: SampleFilter = SampleFilter.NEAREST,
    private var minification: SampleFilter = SampleFilter.NEAREST,
    private var clamping: ClampMode = ClampMode.REPEAT,
    private var mipmapping: Boolean = true,
    private var anisotropy: Int = MAX_ANISOTROPY
) {

    companion object {
        const val MAX_INDEX = GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS
        val MAX_ANISOTROPY = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY).toInt()
    }

    init {

        if (index < 0 || index > MAX_INDEX) {
            throw Exception("Sampler index out of bounds. Must be between 0 and $MAX_INDEX.")
        }

        setMagnification(magnification)
        setMinification(minification)
        setClamping(clamping)
        setMipmapping(mipmapping)
        setAnisotropy(anisotropy)
    }

    fun bind(map: TextureMap) {
        glActiveTexture(GL13.GL_TEXTURE0 + index)
        glBindTexture(GL_TEXTURE_2D, map.handle)
        glActiveTexture(GL13.GL_TEXTURE0)
    }

    fun setMagnification(magnification: SampleFilter) {
        this.magnification = magnification
        glSamplerParameteri(index, GL_TEXTURE_MAG_FILTER, magnification.handle)
    }

    fun setMinification(minification: SampleFilter) {
        this.minification = minification
        glSamplerParameteri(index, GL_TEXTURE_MIN_FILTER, if (mipmapping) {
            when (minification) {
                SampleFilter.NEAREST -> GL_NEAREST_MIPMAP_NEAREST
                SampleFilter.LINEAR -> GL_LINEAR_MIPMAP_LINEAR
            }
        } else {
            minification.handle
        })
    }

    fun setClamping(clamping: ClampMode) {
        this.clamping = clamping
        glSamplerParameteri(index, GL_TEXTURE_WRAP_S, clamping.handle)
        glSamplerParameteri(index, GL_TEXTURE_WRAP_T, clamping.handle)
        glSamplerParameteri(index, GL_TEXTURE_WRAP_R, clamping.handle)
    }

    fun setMipmapping(mipmapping: Boolean) {
        this.mipmapping = mipmapping
        setMinification(minification)
    }

    fun setAnisotropy(anisotropy: Int) {
        this.anisotropy = anisotropy
        glSamplerParameterf(index, GL_TEXTURE_MAX_ANISOTROPY, anisotropy.toFloat())
    }

}