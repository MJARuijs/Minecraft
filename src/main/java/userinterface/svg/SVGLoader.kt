package userinterface.svg

import math.vectors.Vector2
import resources.Loader
import util.File

class SVGLoader : Loader<SVGMesh> {
    
    private val operationsPattern = "\\s*(?<operation>[a-zA-Z]\\s*,?\\s*(-|[0-9]|\\.|\\s)*)".toRegex().toPattern()
    private val pointPattern = "(?<x>-?[0-9]+(\\.[0-9]+)?)\\s*,?\\s*(?<y>-?[0-9]+(\\.[0-9]+)?)?\\s*".toRegex().toPattern()
    private val triangulation = Triangulation()

    private var size = 1.0f

    fun load(path: String, size: Float): SVGMesh {
        this.size = size
        return load(path)
    }

    override fun load(path: String): SVGMesh {
        val file = File(path)
        val content = file.getContent()
        val pointData = one(content)
        
        val verticesToBeTriangulated = pointData.first
        val test = pointData.second
        if (verticesToBeTriangulated.first() == verticesToBeTriangulated.last()) {
            verticesToBeTriangulated.removeAt(verticesToBeTriangulated.size - 1)
        }
        
        val triangulatedPoints = triangulation.process(verticesToBeTriangulated) ?: throw IllegalArgumentException("")
        val vertices = ArrayList<Vector2>()
        triangulatedPoints.forEach { point ->
            vertices += point + Vector2(0.0f, 0.5f)
        }

        test.forEach { point ->
            vertices += point + Vector2(0.0f, 0.5f)
        }

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        for (vertex in vertices) {
            val x = vertex.x
            val y = vertex.y

            if (x < minX) {
                minX = x
            }
            if (x > maxX) {
                maxX = x
            }
            if ((y) < (minY)) {
                minY = y
            }
            if ((y) > (maxY)) {
                maxY = y
            }
        }

        val xDifference = maxX - (maxX - minX) / 2.0f
        val yDifference = maxY - (maxY - minY) / 2.0f

        val centeredVertices = ArrayList<Vector2>()
        vertices.forEach { vertex ->
            centeredVertices += Vector2(vertex.x - xDifference, vertex.y + yDifference)
        }

        return SVGMesh(centeredVertices, maxX - xDifference, maxY - yDifference)
    }
    
    private fun one(content: String): Pair<ArrayList<Vector2>, ArrayList<Vector2>> {
        val viewBox = getValueOfAttribute("viewBox", content)
        val viewBoxValues = viewBox.split(' ')
        val height: Float
        val width: Float
    
        if (viewBoxValues.size < 4) {
            height = getValueOfAttribute("height", content).toFloat()
            width = getValueOfAttribute("width", content).toFloat()
        } else {
            height = viewBoxValues[2].toFloat()
            width = viewBoxValues[3].toFloat()
        }
        
        var pathValues = getValueOfAttribute("path", content)
        
        var operationsMatcher = operationsPattern.matcher(pathValues)
        val operations = ArrayList<SVGOperation>()
        
        while (operationsMatcher.find()) {
            val operation = operationsMatcher.group("operation")
            operations += parseOperation(operation, width, height)
            
            pathValues = pathValues.removePrefix(operation)
            operationsMatcher = operationsPattern.matcher(pathValues)
        }
        
        val verticesToBeTriangulated = ArrayList<Vector2>()
        val triangulatedVertices = ArrayList<Vector2>()
        
        var currentPoint = Vector2()
        
        for (operation in operations) {
            val operationData = operation.computeValues(currentPoint, verticesToBeTriangulated)
            currentPoint = operationData.second
            val points = operationData.first

            if (operation.type == SVGOperationType.BEZIER_CURVE) {
                verticesToBeTriangulated += Vector2(currentPoint)
                
                val pointsToBeTriangulated = ArrayList<Vector2>()
                for (point in points) {
                    pointsToBeTriangulated += point
                }
                
                val triangulatedPoints = triangulation.process(pointsToBeTriangulated) ?: throw IllegalArgumentException("")
                triangulatedVertices += triangulatedPoints
            } else {
                for (point in points) {
                    verticesToBeTriangulated += Vector2(point)
                }
            }
        }
        
        return Pair(verticesToBeTriangulated, triangulatedVertices)
    }
    
    private fun parseOperation(input: String, width: Float, height: Float): SVGOperation {
        val operationId = input.first { char -> char.isLetter() }
        
        val operationType = SVGOperationType.fromId(operationId)
        
        var operationPoints = input.removePrefix(operationId.toString())
        var pointMatcher = pointPattern.matcher(operationPoints)
        var backup = operationPoints
        var points = FloatArray(0)
        
        while (pointMatcher.find()) {
            val pointX = pointMatcher.group("x")
            val pointY = pointMatcher.group("y")
            
            points += (pointX.toFloat() * size / width / 1.77f)
            if (pointY != null) {
                points += -((pointY.toFloat() * size) / height)
            }
    
            operationPoints = operationPoints.trim().removePrefix(pointX).trim()

            if (pointY != null) {
                operationPoints = operationPoints.trim().removePrefix(pointY).trim()
            }
    
            operationPoints = operationPoints.trim()

            if (operationPoints == backup) {
                println("Breaking: $operationPoints")
                break
            }
    
            backup = operationPoints
            pointMatcher = pointPattern.matcher(operationPoints)
        }
        
        return SVGOperation(operationType, operationId.isUpperCase(), points)
    }
    
    private fun getValueOfAttribute(attributeName: String, line: String): String {
        val startIndex = line.indexOf('"', line.indexOf(attributeName)) + 1
        val endIndex = line.indexOf('"', startIndex)
        return line.substring(startIndex, endIndex)
    }
}