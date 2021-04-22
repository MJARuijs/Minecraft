package userinterface.text

import graphics.samplers.ClampMode
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import math.Color
import math.vectors.Vector2
import userinterface.text.font.Font
import userinterface.text.line.Character.Companion.LINE_HEIGHT
import userinterface.text.line.Line
import userinterface.text.line.Word

class Text(val text: String, private val fontSize: Float, private val maxLineWidth: Float = 1.0f, private val font: Font, var color: Color, private val aspectRatio: Float, var translation: Vector2 = Vector2(), var scale: Float = 1.0f) {

    private val sampler = Sampler(0, clamping = ClampMode.EDGE)
    private val spaceWidth = font.getSpaceWidth() * fontSize

    private val textQuad: TextQuad

    init {
        val lines = createLines()
        textQuad = createTextMesh(lines)
    }
    
    fun draw(shaderProgram: ShaderProgram, aspectRatio: Float) {
        shaderProgram.start()
        shaderProgram.set("textureAtlas", sampler.index)
        shaderProgram.set("translation", translation)
        shaderProgram.set("aspectRatio", aspectRatio)
        shaderProgram.set("scale", scale)
        shaderProgram.set("color", color)
        
        sampler.bind(font.textureAtlas)
        textQuad.draw()
        
        shaderProgram.stop()
    }
    
    private fun createLines(): List<Line> {
        val lines = ArrayList<Line>()
        
        var currentLine = Line(maxLineWidth)
        var currentWord = Word(fontSize)
        
        for (character in text) {
            if (character == ' ') {
                if (currentLine.addWord(currentWord, spaceWidth)) {
                    currentWord = Word(fontSize)
                } else {
                    lines += currentLine
                    currentLine = Line(maxLineWidth, currentWord)
                    currentWord = Word(fontSize)
                }
            } else {
                currentWord += font.getCharacter(character)
            }
        }
        
        if (currentLine.addWord(currentWord, spaceWidth)) {
            lines += currentLine
        } else {
            lines += currentLine
            lines += Line(maxLineWidth, currentWord)
        }
        
        return lines
    }
    
    private fun createTextMesh(lines: List<Line>): TextQuad {
        var xCursor = 0.0f
        var yCursor = 0.0f
        var vertices = FloatArray(0)
        var texCoords = FloatArray(0)
        
        for (line in lines) {
            for (word in line.words) {
                for (character in word.characters) {
                    
//                    println("$fontSize ${character.xOffset}, ${character.yOffset}, ${character.quadWidth}, ${character.quadHeight}")
                    
                    val x = (xCursor + character.xOffset * fontSize)
                    val y = (yCursor + character.yOffset * fontSize)
    
                    val letterMaxX = x + (character.quadWidth * fontSize)
                    val letterMaxY = y + (character.quadHeight * fontSize)
                    
                    val properX = (2 * x) - 1
                    val properY = (-2 * y) + 1
                    val properMaxX = (2 * letterMaxX) - 1
                    val properMaxY = (-2 * letterMaxY) + 1
                    
//                    println("$xCursor, $properX, $properY, $properMaxX, $properMaxY")
    
//                    vertices += floatArrayOf(
//                        x, -y,
//                        x, -letterMaxY,
//                        letterMaxX, -letterMaxY,
//                        letterMaxX, -letterMaxY,
//                        letterMaxX, -y,
//                        x, -y
//                    )
    
                    vertices += floatArrayOf(
                        properX, properY,
                        properX, properMaxY,
                        properMaxX, properMaxY,
                        properMaxX, properMaxY,
                        properMaxX, properY,
                        properX, properY
                    )
                    
                    texCoords += floatArrayOf(
                        character.x, character.y,
                        character.x, character.yMaxTexCoord,
                        character.xMaxTexCoord, character.yMaxTexCoord,
                        character.xMaxTexCoord, character.yMaxTexCoord,
                        character.xMaxTexCoord, character.y,
                        character.x, character.y
                    )
    
                    xCursor += character.advance * fontSize
                }
                xCursor += spaceWidth
            }

            xCursor = 0.0f
            yCursor += LINE_HEIGHT * fontSize
        }
        
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        
        for (i in vertices.indices step 2) {
            val x = vertices[i]
            val y = vertices[i + 1]
            
            if (x < minX) {
                minX = x
            }
            if (x > maxX) {
                maxX = x
            }
            if (y < minY) {
                minY = y
            }
            if (y > maxY) {
                maxY = y
            }
        }
        
        val xDifference = (maxX - minX)
        val yDifference = (minY - maxY)
        
//        println(xDifference)
//        println(yDifference)
    
        var repositionedVertices = FloatArray(0)
    
        for (i in vertices.indices step 2) {
            val x = vertices[i]
            val y = vertices[i + 1]
            
            repositionedVertices += x - xDifference / 2.0f
            repositionedVertices += y - yDifference / 2.0f
        }
        return TextQuad(vertices, texCoords)
    }

    fun destroy() {
        textQuad.destroy()
    }

}