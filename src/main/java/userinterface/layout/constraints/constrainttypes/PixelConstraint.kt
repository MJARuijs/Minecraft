package userinterface.layout.constraints.constrainttypes

import math.Axis
import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.UIContainer
import userinterface.UniversalParameters
import userinterface.layout.constraints.ConstraintDirection

class PixelConstraint(direction: ConstraintDirection, var offset: Float = 0.0f, var anchorId: String = "parent") : Constraint(direction) {

    override fun apply(translation: Vector2, scale: Vector2, parentTranslation: Vector2?, parentScale: Vector2?, parent: UIContainer?): Pair<Vector2, Vector2> {
        var referenceTranslation = Vector2()
        var referenceScale = Vector2(UniversalParameters.aspectRatio, 1.0f)
    
        if (parentTranslation != null && parentScale != null) {
            referenceTranslation = parentTranslation
            referenceScale = parentScale
        } else if (parent is MovableUIContainer) {
            referenceTranslation = parent.getTranslation()
            referenceScale = parent.getScale()
        }
        
        if (anchorId != "parent") {
            val sibling = parent?.findById(anchorId)
            if (sibling != null) {
                referenceTranslation = sibling.getGoalDimensions().first
                referenceScale = sibling.getGoalDimensions().second
            }
        }
    
        if (direction == ConstraintDirection.HORIZONTAL || direction == ConstraintDirection.VERTICAL) {
            println("Invalid direction given for PixelConstraint!")
        }
    
        if (direction == ConstraintDirection.TO_TOP) {
            val referenceTop = referenceScale.y + referenceTranslation.y
            calculateTranslation(translation, referenceTop, scale, referenceScale, Axis.Y, true)
        }
        if (direction == ConstraintDirection.TO_BOTTOM) {
            val referenceBottom = referenceTranslation.y - referenceScale.y
            calculateTranslation(translation, referenceBottom, scale, referenceScale, Axis.Y, false)
        }
        if (direction == ConstraintDirection.TO_LEFT) {
            val referenceLeft = referenceTranslation.x - referenceScale.x
            calculateTranslation(translation, referenceLeft, scale, referenceScale, Axis.X, false)
        }
        if (direction == ConstraintDirection.TO_RIGHT) {
            val referenceRight = referenceTranslation.x + referenceScale.x
            calculateTranslation(translation, referenceRight, scale, referenceScale, Axis.X, true)
        }
        translation.roundToDecimal(5)
        
        return Pair(translation, scale)
    }
    
    private fun calculateTranslation(translation: Vector2, reference: Float, scale: Vector2, referenceScale: Vector2, axis: Axis, signFlipped: Boolean) {
        val sign = if (signFlipped) -1 else 1
        
        if (anchorId == "parent") {
            translation[axis.index] = reference + scale[axis.index] * sign
            translation[axis.index] += offset * referenceScale[axis.index] * 2.0f * sign
        } else {
            translation[axis.index] = reference - scale[axis.index] * sign
            translation[axis.index] -= offset * referenceScale[axis.index] * 2.0f * sign
        }
    }
}