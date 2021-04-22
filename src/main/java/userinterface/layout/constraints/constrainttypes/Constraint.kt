package userinterface.layout.constraints.constrainttypes

import math.vectors.Vector2
import userinterface.UIContainer
import userinterface.layout.constraints.ConstraintDirection

abstract class Constraint(var direction: ConstraintDirection) {
    
    abstract fun apply(translation: Vector2, scale: Vector2, parentTranslation: Vector2?, parentScale: Vector2?, parent: UIContainer?): Pair<Vector2, Vector2>
    
}