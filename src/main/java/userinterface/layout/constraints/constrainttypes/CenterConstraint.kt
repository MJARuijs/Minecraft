package userinterface.layout.constraints.constrainttypes

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.UIContainer
import userinterface.layout.constraints.ConstraintDirection

class CenterConstraint(direction: ConstraintDirection) : Constraint(direction) {
    
    override fun apply(translation: Vector2, scale: Vector2, parentTranslation: Vector2?, parentScale: Vector2?, parent: UIContainer?): Pair<Vector2, Vector2> {
        var referenceTranslation = Vector2()
        
        if (parentTranslation != null) {
            referenceTranslation = parentTranslation
        } else if (parent is MovableUIContainer) {
            referenceTranslation = parent.getTranslation()
        }
    
        when (direction) {
            ConstraintDirection.HORIZONTAL -> translation.x = referenceTranslation.x
            ConstraintDirection.VERTICAL -> translation.y = referenceTranslation.y
            else -> println("Invalid direction given for CenterConstraint!")
        }
        
        translation.roundToDecimal(5)
        
        return Pair(translation, scale)
    }
}