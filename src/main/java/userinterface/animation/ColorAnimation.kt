package userinterface.animation

import math.Color
import math.vectors.Vector4
import userinterface.MovableUIContainer
import userinterface.animation.animationtypes.ColorAnimationType
import userinterface.items.backgrounds.ColorType
import userinterface.items.backgrounds.ColoredBackground
import userinterface.items.backgrounds.TexturedBackground
import kotlin.math.abs

class ColorAnimation(val duration: Float, val item: MovableUIContainer, private val otherColor: Color, private val type: ColorAnimationType, private val colorType: ColorType, private val ignoreAlpha: Boolean = true, onFinish: () -> Unit = {}) : Animation(onFinish) {

    private var rSpeed = 0.0f
    private var gSpeed = 0.0f
    private var bSpeed = 0.0f
    private var aSpeed = 0.0f
    
    private var started = false
    
    private var startColor = Color()
    private var goalColor = Color()

    override fun apply(deltaTime: Float): Boolean {
        if (!started) {
            startColor = if (colorType == ColorType.BACKGROUND_COLOR) {
                if (item.background is ColoredBackground) {
                    (item.background as ColoredBackground).color
                } else {
                    (item.background as TexturedBackground).overlayColor
                }
            } else {
                (item.background as ColoredBackground).outlineColor
            }
            
            goalColor = if (type == ColorAnimationType.CHANGE_TO_COLOR) {
                otherColor
            } else {
                startColor + otherColor
            }
    
            for (i in 0 until 4) {
                if (goalColor[i] < 0.0f) {
                    goalColor[i] = 0.0f
                }
                if (goalColor[i] > 1.0f) {
                    goalColor[i] = 1.0f
                }
            }
            
            if (ignoreAlpha) {
                if (type == ColorAnimationType.CHANGE_TO_COLOR) {
                
                } else {
                
                }
                goalColor.a = 1.0f
            }
            
            when (type) {
                ColorAnimationType.CHANGE_TO_COLOR -> {
                    rSpeed = (otherColor.r - startColor.r) / duration
                    gSpeed = (otherColor.g - startColor.g) / duration
                    bSpeed = (otherColor.b - startColor.b) / duration
                    aSpeed = (otherColor.a - startColor.a) / duration
                }
                ColorAnimationType.ADD_TO_COLOR -> {
                    rSpeed = (goalColor.r - startColor.r) / duration
                    gSpeed = (goalColor.g - startColor.g) / duration
                    bSpeed = (goalColor.b - startColor.b) / duration
                    aSpeed = (goalColor.a - startColor.a) / duration
                }
            }
            
            started = true
        }
        if (item.background is ColoredBackground) {
            if (colorType == ColorType.BACKGROUND_COLOR) {
                val currentColor = (item.background as ColoredBackground).color.copy()
                (item.background as ColoredBackground).color = increaseValues(currentColor, deltaTime)
                
                if ((item.background as ColoredBackground).color == goalColor) {
                    return true
                }
            } else if (colorType == ColorType.OUTLINE_COLOR) {
                (item.background as ColoredBackground).outlineColor = increaseValues(Color((item.background as ColoredBackground).outlineColor), deltaTime)
                if ((item.background as ColoredBackground).outlineColor == otherColor) {
                    return true
                }
            }
            return false
        } else {
            if (colorType == ColorType.BACKGROUND_COLOR) {
                (item.background as TexturedBackground).overlayColor = increaseValues(Color((item.background as TexturedBackground).overlayColor), deltaTime)
                if ((item.background as TexturedBackground).overlayColor == otherColor) {
                    return true
                }
            } else if (colorType == ColorType.OUTLINE_COLOR) {
                (item.background as TexturedBackground).outlineColor = increaseValues(Color((item.background as TexturedBackground).outlineColor), deltaTime)
                if ((item.background as TexturedBackground).outlineColor == otherColor) {
                    return true
                }
            }
            return false
        }
    }

    private fun increaseValues(currentColor: Color, deltaTime: Float): Color {
        val increaseValues = Vector4(
            deltaTime * rSpeed,
            deltaTime * gSpeed,
            deltaTime * bSpeed,
            deltaTime * aSpeed
        )

//        var newR = if (abs(currentColor.r - changeToColor.r) < abs(increaseValues[0])) {
//            changeToColor.r
//        } else {
//            currentColor.r + increaseValues[0]
//        }
//        var newG = if (abs(currentColor.g - changeToColor.g) < abs(increaseValues[1])) {
//            changeToColor.g
//        } else {
//            currentColor.g + increaseValues[1]
//        }
//        var newB = if (abs(currentColor.b - changeToColor.b) < abs(increaseValues[2])) {
//            changeToColor.b
//        } else {
//            currentColor.b + increaseValues[2]
//        }
//        var newA = if (abs(currentColor.a - changeToColor.a) < abs(increaseValues[3])) {
//            changeToColor.a
//        } else {
//            currentColor.a + increaseValues[3]
//        }
//        return Color(newR, newG, newB, newA)
        
        
        for (i in 0 until 4) {
//            println("Diff $i ${currentColor[i]} ${goalColor[i]} ${increaseValues[i]}")
            
            if (abs(currentColor[i] - goalColor[i]) <= abs(increaseValues[i])) {
//            if (abs(currentColor[i] - otherColor[i]) < abs(increaseValues[i])) {
                if (type == ColorAnimationType.CHANGE_TO_COLOR) {
                    currentColor[i] = otherColor[i]
                } else {
                    currentColor[i] = startColor[i] + otherColor[i]
//                    println("DONE $i")
                }
            } else {
//                println("NOPE $i")
                currentColor[i] = currentColor[i] + increaseValues[i]
//                println("RESULT ${currentColor[i]}")
            }
            
            if (currentColor[i] < 0.0f) {
                currentColor[i] = 0.0f
            }
            if (currentColor[i] > 1.0f) {
                currentColor[i] = 1.0f
            }
        }
        
        
        
        return currentColor
    }
}

