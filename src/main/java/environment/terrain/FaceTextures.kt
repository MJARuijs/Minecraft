package environment.terrain

import graphics.test.TextureArray
import resources.images.ImageCache
import resources.images.ImageData
import java.io.File

object FaceTextures {

    val colorMaps = ArrayList<ImageData>()
    val normalMaps = ArrayList<Pair<Int, ImageData?>>()
    val specularMaps = ArrayList<Pair<Int, ImageData?>>()

    var t: TextureArray? = null

    fun load(path: String) {
        val pathFile = File("$path/colorMaps")
        if (!pathFile.exists()) {
            pathFile.mkdir()
        }

        val fileDirectories = pathFile.list() ?: throw IllegalArgumentException("Directory at $path does not exist")

        for ((i, directory) in fileDirectories.sorted().withIndex()) {
            if (directory.endsWith("mcmeta")) {
                continue
            }

            val colorMap = ImageCache.get("textures/blocks/colorMaps/$directory")
            val normalMap = try {
                ImageCache.get("textures/blocks/normalMaps/$directory")
            } catch (e: Exception) {
                null
            }

            val specularMap = try {
                ImageCache.get("textures/blocks/specularMaps/$directory")
            } catch (e: Exception) {
                null
            }

            colorMaps += colorMap
            normalMaps += if (normalMap == null) {
                Pair(-1, normalMap)
            } else {
                Pair(i, normalMap)
            }

            specularMaps += if (specularMap == null) {
                Pair(-1, specularMap)
            } else {
                Pair(i, specularMap)
            }

        }

        t = TextureArray(colorMaps)

        println("DONE")
    }

}