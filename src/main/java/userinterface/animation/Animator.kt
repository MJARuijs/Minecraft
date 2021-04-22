package userinterface.animation

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.animation.animationtypes.ColorAnimationType
import userinterface.animation.animationtypes.TransitionType
import userinterface.items.Item
import userinterface.layout.UILayout
import userinterface.layout.constraints.ConstraintSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class Animator(private val item: MovableUIContainer) {

    private val animations = ArrayList<Animation>()
    private val animationQueue = LinkedList<List<Animation>>()
    private val postPonedChildren = ConcurrentHashMap<String, MovableUIContainer>()
    private val computedChildren = ArrayList<String>()
    
    operator fun plusAssign(animations: List<Animation>) {
        animationQueue += animations
    }

    fun apply(parent: MovableUIContainer?, constraints: ConstraintSet, duration: Float, vararg extraAnimations: Animation) {
        val animations = ArrayList<Animation>()
        animations.addAll(extraAnimations)
        apply(parent, constraints, duration, animations)
    }
    
    fun apply(parent: MovableUIContainer?, constraints: ConstraintSet, duration: Float, extraAnimations: ArrayList<Animation> = ArrayList()) {
        val newDimensions = constraints.computeResult(parent?.getGoalDimensions(), parent)
        animateLayoutTransition(item, duration, newDimensions, extraAnimations)
    }

    fun apply(children: ArrayList<Item>, layout: UILayout, duration: Float) {
        postPonedChildren.clear()
        computedChildren.clear()

        for (child in children) {
            applyChild(child, layout, duration)
        }
        
        val itemChanges = layout.getColorChange(item.id)
        if (itemChanges != null) {
            val colorAnimation = ColorAnimation(duration, item, itemChanges.first.color, ColorAnimationType.CHANGE_TO_COLOR, itemChanges.second)
            animationQueue += arrayListOf<Animation>(colorAnimation)
        }
    }

    private fun applyChild(child: MovableUIContainer, layout: UILayout, duration: Float) {
        val childConstraints = layout.getConstraintsChange(child.id) ?: child.constraints
        val requiredIds = childConstraints.determineRequiredIds()
        
        if (computedChildren.containsAll(requiredIds)) {
            val childDimensions = childConstraints.computeResult(item.getGoalDimensions(), item)
            child.setGoalTranslation(childDimensions.first)
            child.setGoalScale(childDimensions.second)

            computedChildren += child.id

            if (postPonedChildren.containsKey(child.id)) {
                postPonedChildren.remove(child.id)
            }
            for (postPonedChild in postPonedChildren) {
                applyChild(postPonedChild.value, layout, duration)
            }
            animateLayoutTransition(child, duration, child.getGoalDimensions())
        } else {
            postPonedChildren[child.id] = child
        }
        child.apply(layout, duration)
    }

    fun update(deltaTime: Float) {
        val removableAnimations = ArrayList<Animation>()
        
        animations.forEach { animation ->
            if (animation.apply(deltaTime)) {
                animation.onFinish()
                removableAnimations += animation
            }
        }

        if (removableAnimations.isNotEmpty()) {
            animations.removeAll(removableAnimations)
        }
        
        if (animations.isEmpty() && animationQueue.isNotEmpty()) {
            val queuedAnimation = animationQueue.poll()
            animations += queuedAnimation
        }
    }

    private fun animateLayoutTransition(item: MovableUIContainer, duration: Float, newDimensions: Pair<Vector2, Vector2>, extraAnimations: ArrayList<Animation> = ArrayList()) = animateLayoutTransition(item, duration, newDimensions.first, newDimensions.second, extraAnimations)
    
    private fun animateLayoutTransition(item: MovableUIContainer, duration: Float, newTranslation: Vector2, newScale: Vector2, extraAnimations: ArrayList<Animation> = ArrayList()) {
        val requiredAnimations = ArrayList<Animation>()
        requiredAnimations.addAll(extraAnimations)
        
        if (newTranslation.x != item.getTranslation().x) {
            requiredAnimations += XTransitionAnimation(duration, item, newTranslation.x, TransitionType.PLACEMENT)
        }

        if (newTranslation.y != item.getTranslation().y) {
            requiredAnimations += YTransitionAnimation(duration, item, newTranslation.y, TransitionType.PLACEMENT)
        }

        if (newScale.x != item.getScale().x) {
            requiredAnimations += XScaleAnimation(duration, item, newScale.x)
        }

        if (newScale.y != item.getScale().y) {
            requiredAnimations += YScaleAnimation(duration, item, newScale.y)
        }
        
        animationQueue += requiredAnimations
    }
}