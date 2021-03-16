package userinterface.layout.constraints.constrainttypes

import math.vectors.Vector2
import userinterface.UIContainer
import userinterface.layout.constraints.ConstraintDirection

class AspectRatioConstraint(direction: ConstraintDirection, var aspectRatio: Float) : Constraint(direction) {
    
    override fun apply(translation: Vector2, scale: Vector2, parentTranslation: Vector2?, parentScale: Vector2?, parent: UIContainer?): Pair<Vector2, Vector2> {
        if (direction == ConstraintDirection.VERTICAL) {
            scale.y = scale.x * aspectRatio
        }
        
        if (direction == ConstraintDirection.HORIZONTAL) {
            scale.x = scale.y * aspectRatio
        }
        
        scale.roundToDecimal(5)
        
        return Pair(translation, scale)
    }
}