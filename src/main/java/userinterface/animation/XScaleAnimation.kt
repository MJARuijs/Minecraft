package userinterface.animation

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.items.Item
import kotlin.math.abs

class XScaleAnimation(val duration: Float, val item: MovableUIContainer, val toScale: Float) : Animation() {
    
    private var started = false
    
    private var speed = 0f
    private var finalScale = 0f
    
    override fun apply(deltaTime: Float): Boolean {
        if (!started) {
            speed = (toScale - item.getScale().x) / duration
            finalScale = toScale
            started = true
        }
        val increase = deltaTime * speed
        
        if (abs(item.getScale().x - finalScale) < abs(increase)) {
            item.setScale(Vector2(finalScale, item.getScale().y))
            return true
        } else {
            item.addToScale(Vector2(increase, 0.0f))
        }
        
        return false
    }
}