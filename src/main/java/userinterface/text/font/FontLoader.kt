package userinterface.text.font

import graphics.textures.ImageMap
import math.vectors.Vector4
import resources.Loader
import resources.images.ImageCache
import userinterface.text.line.Character
import userinterface.text.MetaData
import util.File

class FontLoader(private val aspectRatio: Float) : Loader<Font> {

    override fun load(path: String): Font {

        val fileName = when {
            path.contains(".png") -> {
                val startIndex = path.indexOf('/') + 1
                val endIndex = path.lastIndexOf('.')
                path.substring(startIndex, endIndex)
            }
            path.contains("optimized_") -> {
                val startIndex = path.lastIndexOf('_') + 1
                val endIndex = path.lastIndexOf('.')
                path.substring(startIndex, endIndex)
            }
            else -> {
                throw NonExistingFontException("No font files were found at path: $path")
            }
        }

        val dashIndex = path.lastIndexOf('/') + 1
        val directory = path.substring(0, dashIndex)

        val fontPath = "${directory}optimized_$fileName.fnt"
        val texturePath = "${directory}$fileName.png"

        val fontFile = File(fontPath)
        val lines = fontFile.getLines()

        val fontProperties = lines[0].split(" ")
        val size = fontProperties[0].toInt()

        val padding = Vector4().fromString(fontProperties[1])

        val lineHeight = fontProperties[2].toInt()
        val base = fontProperties[3].toInt()
        val scaleW = fontProperties[4].toInt()
        val scaleH = fontProperties[5].toInt()

        val metaData = MetaData(size, padding, lineHeight, base, scaleW, scaleH)

        val characters = ArrayList<Character>()
        for (i in 1 until lines.size) {
            characters += Character.fromLine(lines[i], metaData, aspectRatio)
        }

        val texture = ImageMap(ImageCache.get(texturePath))

        return Font(texture, characters)
    }

}