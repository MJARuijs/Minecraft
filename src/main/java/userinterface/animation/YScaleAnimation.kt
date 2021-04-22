package userinterface.animation

import math.vectors.Vector2
import userinterface.MovableUIContainer
import userinterface.items.Item
import kotlin.math.abs

class YScaleAnimation(val duration: Float, val item: MovableUIContainer, val toScale: Float) : Animation() {
    
    private var speed = 0f
    private var finalScale = 0f
    private var started = false
    
    override fun apply(deltaTime: Float): Boolean {
        if (!started) {
            started = true
            speed = (toScale - item.getScale().y) / duration
            finalScale = toScale
        }
        val increase = deltaTime * speed
        
        if (abs(item.getScale().y - finalScale) < abs(increase)) {
            item.setScale(Vector2(item.getScale().x, finalScale))
            return true
        } else {
            item.addToScale(Vector2(0f, increase))
        }
        return false
    }
}