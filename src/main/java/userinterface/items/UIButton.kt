package userinterface.items

import devices.Button
import devices.Mouse
import math.Color
import userinterface.UIColor
import userinterface.UniversalParameters
import userinterface.animation.Animation
import userinterface.animation.effects.Effect
import userinterface.items.backgrounds.Background
import userinterface.layout.constraints.ConstraintDirection
import userinterface.layout.constraints.ConstraintSet
import userinterface.layout.constraints.constrainttypes.CenterConstraint
import userinterface.layout.constraints.constrainttypes.RelativeConstraint
import userinterface.text.font.Font

open class UIButton(id: String, constraints: ConstraintSet, private var onClick: () -> Unit = {}, background: Background = UniversalParameters.BUTTON_BACKGROUND()) : Item(id, constraints, background) {

    private var isClicked = false
    private var isHovered = false
    private val hoverAnimations = ArrayList<Effect>()
    private val clickAnimations = ArrayList<Effect>()
    
    fun setOnClick(function: () -> Unit): UIButton {
        onClick = function
        return this
    }
    
    private fun setText(text: String, background: Background, color: Color, font: Font = UniversalParameters.defaultFont): UIButton {
        add(
            TextBox("${id}_text", ConstraintSet(
            CenterConstraint(ConstraintDirection.HORIZONTAL),
            CenterConstraint(ConstraintDirection.VERTICAL),
            RelativeConstraint(ConstraintDirection.HORIZONTAL, 1.0f),
            RelativeConstraint(ConstraintDirection.VERTICAL, 1.0f)
        ), text, 1.77f, 3f, background, color, font)
        )
        return this
    }
    
    fun setText(text: String, background: Background = UniversalParameters.TEXTBOX_BACKGROUND(), color: UIColor = UniversalParameters.TEXT_COLOR(), font: Font = UniversalParameters.defaultFont) = setText(text, background, color.color, font)
    
    fun addHoverEffects(vararg animation: Effect): UIButton {
        hoverAnimations += animation
        return this
    }
    
    fun addHoverEffects(effects: ArrayList<Effect>): UIButton {
        hoverAnimations += effects
        return this
    }
    
    fun addClickEffects(vararg animation: Effect): UIButton {
        clickAnimations += animation
        return this
    }
    
    fun addClickEffects(effects: ArrayList<Effect>): UIButton {
        clickAnimations += effects
        return this
    }
    
    override fun update(mouse: Mouse, aspectRatio: Float, deltaTime: Float): Boolean {
        if (super.update(mouse, aspectRatio, deltaTime)) {
            return true
        }

        val isNowHovered = isHovered(mouse, aspectRatio)

        if (isHovered && !isNowHovered) {
            val removeAnimations = ArrayList<Animation>()
            for (effect in hoverAnimations) {
                removeAnimations += effect.removeFrom(this)
            }
            animator += removeAnimations
        }
        
        if (!isHovered && isNowHovered) {
            val applyAnimations = ArrayList<Animation>()
            for (effect in hoverAnimations) {
                applyAnimations += effect.applyOn(this)
            }
            animator += applyAnimations
        }
        
        isHovered = isNowHovered
        
        if (isHovered && (mouse.isPressed(Button.LEFT) || mouse.isPressed(Button.LEFT))) {
            onClick()
            val applyAnimations = ArrayList<Animation>()
            for (effect in clickAnimations) {
                applyAnimations += effect.applyOn(this)
            }
            animator += applyAnimations
            isClicked = true
        }

        if (isClicked && (mouse.isReleased(Button.RIGHT) || mouse.isReleased(Button.LEFT))) {
            isClicked = false
            val removeAnimations = ArrayList<Animation>()
            for (effect in clickAnimations) {
                removeAnimations += effect.removeFrom(this)
            }
            animator += removeAnimations
        }

        if (isHovered || isClicked) {
            return true
        }
        return false
    }

    private fun isHovered(mouse: Mouse, aspectRatio: Float): Boolean {
        return isMouseOnButton(mouse, aspectRatio)
    }

    private fun isMouseOnButton(mouse: Mouse, aspectRatio: Float): Boolean {
        val minX = (getTranslation().x - getScale().x) / aspectRatio
        val maxX = (getTranslation().x + getScale().x) / aspectRatio
        val minY = getTranslation().y - getScale().y
        val maxY = getTranslation().y + getScale().y

        val scaledMouseX = mouse.x * 2.0f
        val scaledMouseY = mouse.y * 2.0f

        if (scaledMouseX < minX || scaledMouseX > maxX) {
            return false
        }
        if (scaledMouseY < minY || scaledMouseY > maxY) {
            return false
        }
        return true
    }
}