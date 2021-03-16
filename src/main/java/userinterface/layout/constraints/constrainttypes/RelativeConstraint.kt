package userinterface.layout.constraints.constrainttypes

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.UIContainer
import userinterface.UniversalParameters
import userinterface.layout.constraints.ConstraintDirection

class RelativeConstraint(direction: ConstraintDirection, var percentage: Float, val anchorId: String = "parent", private val relativePercentage: Boolean = true) : Constraint(direction) {

    override fun apply(translation: Vector2, scale: Vector2, parentTranslation: Vector2?, parentScale: Vector2?, parent: UIContainer?): Pair<Vector2, Vector2> {
        var referenceScale = Vector2(UniversalParameters.aspectRatio, 1.0f)
        
        if (parentScale != null) {
            referenceScale = parentScale
        } else if (parent is MovableUIContainer) {
            referenceScale = parent.getScale()
        }
        
        if (anchorId != "parent") {
            referenceScale = parent?.findById(anchorId)?.getGoalDimensions()?.second ?: referenceScale
        }
    
        if (direction == ConstraintDirection.HORIZONTAL) {
            if (relativePercentage) {
                scale.x = referenceScale.x * percentage
            } else {
                scale.x = percentage
            }
        }
    
        if (direction == ConstraintDirection.VERTICAL) {
            if (relativePercentage) {
                scale.y = referenceScale.y * percentage
            } else {
                scale.y = percentage
            }
        }
        
        scale.roundToDecimal(5)
        
        return Pair(translation, scale)
    }
}