package userinterface.animation

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.animation.animationtypes.TransitionType
import kotlin.math.abs

class YTransitionAnimation(val duration: Float, val item: MovableUIContainer, val translation: Float, val transitionType: TransitionType, onFinish: () -> Unit = {}) : Animation(onFinish) {
    
    private var started = false
    private var speed = 0.0f
    private var finalPoint = 0.0f
    
    override fun apply(deltaTime: Float): Boolean {
        if (!started) {
            started = true
            if (transitionType == TransitionType.MOVEMENT) {
                speed = translation / duration
                finalPoint = item.getTranslation().y + translation
            } else {
                speed = (translation - item.getGoalDimensions().first.y) / duration
                finalPoint = translation
            }
    
            item.setGoalTranslation(Vector2(item.getGoalTranslation().x, finalPoint))
        }
        val increase = deltaTime * speed
        
        if (abs(item.getTranslation().y - finalPoint) < abs(increase)) {
            item.place(Vector2(item.getTranslation().x, finalPoint))
            return true
        } else {
            item.translate(Vector2(0f, increase))
        }
        return false
    }
}