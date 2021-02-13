package graphics.material

import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import math.Color

class TexturedMaterial(private val imageMap: ImageMap, private val overlayColor: Color = Color(0.0f, 0.0f, 0.0f, 0.0f)) : Material() {

    private val sampler = Sampler(0)

    override fun setProperties(shaderProgram: ShaderProgram) {
        sampler.bind(imageMap)

        shaderProgram.set("texture", sampler.index)
        shaderProgram.set("overlayColor", overlayColor)
    }
}