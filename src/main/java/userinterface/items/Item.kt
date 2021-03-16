package userinterface.items

import graphics.shaders.ShaderProgram
import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.UIColor
import userinterface.UniversalParameters
import userinterface.layout.constraints.ConstraintSet
import userinterface.items.backgrounds.Background
import userinterface.items.backgrounds.ColoredBackground
import userinterface.items.backgrounds.TexturedBackground

open class Item(id: String, constraints: ConstraintSet, background: Background = UniversalParameters.ITEM_BACKGROUND()) : MovableUIContainer(id, constraints, background) {
    
    val baseBackgroundColor = when (background) {
        is ColoredBackground -> background.color
        is TexturedBackground -> background.overlayColor
        else -> UIColor.TRANSPARENT.color
    }
    
    init {
        if (requiredIds().contains(id)) {
            println("UI ERROR: item with id: $id is dependent of itself")
        }
    }
    
    open fun draw(shaderProgram: ShaderProgram, iconProgram: ShaderProgram, textProgram: ShaderProgram, aspectRatio: Float, parent: MovableUIContainer?) {
        shaderProgram.set("translation", getTranslation())
        shaderProgram.set("scale", getScale())
        
        shaderProgram.set("allowedToOverdraw", parent?.isChildBoundless(id) ?: true)
        shaderProgram.set("parentTranslation", parent?.getTranslation() ?: Vector2())
        shaderProgram.set("parentScale", parent?.getScale() ?: Vector2(1.0f, 1.0f))

        background.setProperties(shaderProgram)

        quad.draw()

        children.forEach { child ->
            child.draw(shaderProgram, iconProgram, textProgram, aspectRatio, this)
        }
    }
}