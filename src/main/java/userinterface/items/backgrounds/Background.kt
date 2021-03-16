package userinterface.items.backgrounds

import graphics.shaders.ShaderProgram
import math.Color

abstract class Background(var cornerRadius: Float, var outline: Float = 0.0f, var outlineColor: Color = Color()) {
    
    init {
        if (cornerRadius < 0.0f) {
            cornerRadius = 0.0f
        }
        if (cornerRadius > 90.0f) {
            cornerRadius = 90.0f
        }
    }

    open fun setProperties(shaderProgram: ShaderProgram) {
        shaderProgram.set("cornerRadius", cornerRadius)
        shaderProgram.set("outline", outline)
        shaderProgram.set("outlineColor", outlineColor)
    }

}