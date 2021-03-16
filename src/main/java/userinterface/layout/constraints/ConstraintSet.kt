package userinterface.layout.constraints

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.UIContainer
import userinterface.layout.constraints.constrainttypes.*

class ConstraintSet(val constraints: ArrayList<Constraint> = ArrayList()) {

    constructor(vararg constraints: Constraint) : this() {
        this.constraints.addAll(constraints)
    }

    private var translation = Vector2()
    private var scale = Vector2(1f, 1f)

    private val requiredIds = ArrayList<String>()

    init {
        determineRequiredIds()
    }

    fun add(constraint: Constraint, parent: MovableUIContainer? = null, parentTranslation: Vector2? = null, parentScale: Vector2? = null) {
        constraints += constraint
        apply(parent, parentTranslation, parentScale)
    }
    
    fun add(constraints: ArrayList<Constraint>, parent: MovableUIContainer? = null, parentTranslation: Vector2? = null, parentScale: Vector2? = null) {
        this.constraints += constraints
        apply(parent, parentTranslation, parentScale)
    }
    
    fun add(constraints: ConstraintSet, parent: MovableUIContainer? = null, parentTranslation: Vector2? = null, parentScale: Vector2? = null) {
        this.constraints += constraints.constraints
        apply(parent, parentTranslation, parentScale)
    }

    fun determineRequiredIds(): ArrayList<String> {
        requiredIds.clear()
        
        for (constraint in constraints) {
            if (constraint is PixelConstraint) {
                if (constraint.anchorId != "parent") {
                    requiredIds += constraint.anchorId
                }
            } else if (constraint is RelativeConstraint) {
                if (constraint.anchorId != "parent") {
                    requiredIds += constraint.anchorId
                }
            }
        }

        return requiredIds
    }

    fun getTranslation() = translation

    fun getScale() = scale

    fun translate(translation: Vector2) {
        this.translation += translation
    }
    
    fun place(placement: Vector2) {
        this.translation = placement
    }
    
    fun setScale(scale: Vector2) {
        this.scale = scale
    }

    fun getScale(scale: Vector2) {
        this.scale *= scale
    }
    
    fun addToScale(scale: Vector2) {
        this.scale += scale
    }
    
    fun updateConstraint(type: ConstraintType, direction: ConstraintDirection, newValue: Float) {
        for (constraint in constraints) {
            if (constraint.direction == direction) {
                when (type) {
                    ConstraintType.PIXEL -> (constraint as PixelConstraint).offset = newValue
                    ConstraintType.RELATIVE -> (constraint as RelativeConstraint).percentage = newValue
                    ConstraintType.ASPECT_RATIO -> (constraint as AspectRatioConstraint).aspectRatio = newValue
                    else -> {}
                }
            }
        }
    }
    
    fun apply(parent: UIContainer? = null, parentTranslation: Vector2? = null, parentScale: Vector2? = null) {
        constraints.sortWith(compareBy {
            return@compareBy when (it) {
                is PixelConstraint -> 1
                else -> -1
            }
        })
        
        for (constraint in constraints) {
            constraint.apply(translation, scale, parentTranslation, parentScale, parent)
        }
    }
    
    fun apply(parent: UIContainer? = null, parentDimensions: Pair<Vector2, Vector2>? = null) = apply(parent, parentDimensions?.first, parentDimensions?.second)
    
    private fun computeResult(parentTranslation: Vector2?, parentScale: Vector2?, parent: UIContainer?): Pair<Vector2, Vector2> {
        constraints.sortWith(compareBy {
            return@compareBy when (it) {
                is PixelConstraint -> 1
                else -> -1
            }
        })
        
        val resultTranslation = Vector2()
        val resultScale = Vector2()
        for (constraint in constraints) {
            constraint.apply(resultTranslation, resultScale, parentTranslation, parentScale, parent)
        }
        
        return Pair(resultTranslation, resultScale)
    }
    
    fun computeResult(parentDimensions: Pair<Vector2, Vector2>?, parent: UIContainer?) = computeResult(parentDimensions?.first, parentDimensions?.second, parent)
}