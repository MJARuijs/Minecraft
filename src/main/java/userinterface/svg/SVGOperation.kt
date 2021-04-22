package userinterface.svg

import math.vectors.Vector2
import kotlin.math.pow

class SVGOperation(val type: SVGOperationType, private val isAbsolute: Boolean, val values: FloatArray = floatArrayOf()) {
    
    private val smoothness = 100
 
    
    fun computeValues(startPoint: Vector2, computedPoints: ArrayList<Vector2>): Pair<ArrayList<Vector2>, Vector2> {
        val points = ArrayList<Vector2>()
        var point = if (isAbsolute) {
            Vector2()
        } else {
            startPoint
        }
        
        if (type == SVGOperationType.CLOSE_PATH) {
            point = computedPoints[0]
            return Pair(points, point)
        }
        
        if (type == SVGOperationType.HORIZONTAL_LINE) {
            val delta = values[0]
            point.x += delta
            points += point
            return Pair(points, point)
        }
        
        if (type == SVGOperationType.VERTICAL_LINE) {
            val delta = values[0]
            point.y -= delta
            points += point
            return Pair(points, point)
        }
        
        if (type == SVGOperationType.BEZIER_CURVE) {
            if (isAbsolute) {
                val controlPoint1 = Vector2(values[0], values[1]) - startPoint
                val controlPoint2 = Vector2(values[2], values[3]) - startPoint
                val endPoint = Vector2(values[4], values[5]) - startPoint

                for (j in 0 until smoothness) {
                    val smoothness = j.toFloat() / smoothness.toFloat()
                    val newPoint = point * (1 - smoothness).pow(3) +
                            controlPoint1 * 3.0f * (1.0f - smoothness).pow(2) * smoothness +
                            controlPoint2 * 3.0f * (1.0f - smoothness) * smoothness.pow(2) +
                            endPoint * smoothness.pow(3)

                    points += startPoint + newPoint
                }

                points += endPoint + startPoint
                return Pair(points, endPoint + startPoint)
            } else {
                val controlPoint1 = Vector2(values[0], values[1]) + point
                val controlPoint2 = Vector2(values[2], values[3]) + point
                val endPoint = Vector2(values[4], values[5]) + point
                
                for (j in 0 until smoothness) {
                    val smoothness = j.toFloat() / smoothness.toFloat()
                    val newPoint = point * (1 - smoothness).pow(3) +
                            controlPoint1 * 3.0f * (1.0f - smoothness).pow(2) * smoothness +
                            controlPoint2 * 3.0f * (1.0f - smoothness) * smoothness.pow(2) +
                            endPoint * smoothness.pow(3)
        
                    points += newPoint
                }
                points += endPoint
                return Pair(points, endPoint)
            }
        }
        
        for (i in values.indices step 2) {
            val x = values[i]
            val y = values[i + 1]
            
            if (isAbsolute) {
                point = Vector2()
            }
            
            when (type) {
                SVGOperationType.MOVE -> {
                    point.x += x
                    point.y += y
                    if (computedPoints.isEmpty()) {
                        points += Vector2(point)
                    }
                }
                SVGOperationType.LINE -> {
                    point.x += x
                    point.y += y
                    points += Vector2(point)
                }
            }
        }
        
        return Pair(points, point)
    }
}