package userinterface.animation

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.animation.animationtypes.TransitionType
import kotlin.math.abs

class XTransitionAnimation(val duration: Float, val item: MovableUIContainer, private val translation: Float, private val transitionType: TransitionType, onFinish: () -> Unit = {}) : Animation(onFinish) {
    
    private var speed = 0f
    private var finalPoint = 0f
    
    private var started = false
    
    override fun apply(deltaTime: Float): Boolean {
        if (!started) {
            started = true
            if (transitionType == TransitionType.MOVEMENT) {
                speed = translation / duration
                finalPoint = item.getTranslation().x + translation
            } else {
                speed = (translation - item.getGoalDimensions().first.x) / duration
                finalPoint = translation
            }
    
            item.setGoalTranslation(Vector2(finalPoint, item.getGoalTranslation().y))
        }
        
        val increase = deltaTime * speed
        if (abs(item.getTranslation().x - finalPoint) < abs(increase)) {
            item.place(Vector2(finalPoint, item.getTranslation().y))
            return true
        } else {
            item.translate(Vector2(increase, 0.0f))
        }
    
        return false
    }
}