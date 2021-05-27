package userinterface.items.backgrounds

import graphics.samplers.ClampMode
import graphics.samplers.SampleFilter
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import graphics.textures.TextureMap
import math.Color
import resources.images.ImageCache
import userinterface.UIColor

class TexturedBackground(private val textureMap: TextureMap, private var backgroundColor: Color? = null, var overlayColor: Color = UIColor.TRANSPARENT.color, cornerRadius: Float = 0.0f, outline: Float = 0.0f, outlineColor: Color = Color()) : Background(cornerRadius, outline, outlineColor) {

    constructor(texturePath: String, backgroundColor: UIColor? = null, overlayColor: UIColor = UIColor.TRANSPARENT, cornerRadius: Float = 0.0f, outline: Float = 0.0f, outlineColor: UIColor = UIColor.TRANSPARENT) : this(ImageMap(ImageCache.get(texturePath)), backgroundColor?.color, overlayColor.color, cornerRadius, outline, outlineColor.color)

    private val sampler = Sampler(0, false, SampleFilter.LINEAR, SampleFilter.NEAREST, ClampMode.EDGE, true)

    override fun setProperties(shaderProgram: ShaderProgram) {
        super.setProperties(shaderProgram)
        if (backgroundColor != null) {
            shaderProgram.set("color", backgroundColor!!)
            shaderProgram.set("hasBackground", true)
        } else {
            shaderProgram.set("hasBackground", false)
        }
        shaderProgram.set("hasOverlay", true)
        shaderProgram.set("overlayColor", overlayColor)
        shaderProgram.set("textured", true)
        shaderProgram.set("sampler", sampler.index)
        sampler.bind(textureMap)
    }

    override fun reset() {
        sampler.unbind()
    }
}