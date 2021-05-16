package environment.terrain

import graphics.test.TextureArray
import org.w3c.dom.Text
import resources.images.ImageCache
import resources.images.ImageData
import java.io.File

class FaceTextures(path: String) {

    init {
        val pathFile = File("$path/colorMaps")
        if (!pathFile.exists()) {
            pathFile.mkdir()
        }

        val fileDirectories = pathFile.list() ?: throw IllegalArgumentException("Directory at $path does not exist")

        for ((i, directory) in fileDirectories.sorted().withIndex()) {
            if (directory.endsWith("mcmeta")) {
                continue
            }

            val normalDirectory = directory.removeSuffix(".png") + "_n.png"
            val specularDirectory = directory.removeSuffix(".png") + "_s.png"

            val colorMap = ImageCache.get("textures/blocks/colorMaps/$directory")
            val normalMap = try {
                ImageCache.get("textures/blocks/normalMaps/$normalDirectory")
            } catch (e: Exception) {
                null
            }

            val specularMap = try {
                ImageCache.get("textures/blocks/specularMaps/$specularDirectory")
            } catch (e: Exception) {
                null
            }

            colorMaps += Pair(directory.trim(), colorMap)

            if (normalMap != null) {
                normalMaps += Pair(normalDirectory, normalMap)
            }

            if (specularMap != null) {
                specularMaps += Pair(specularDirectory, specularMap)
            }
        }

        textures = TextureArray(colorMaps.map { data -> data.second })
        normals = TextureArray(normalMaps.map { data -> data.second })
        speculars = TextureArray(specularMaps.map { data -> data.second })
    }

    companion object {

        val colorMaps = ArrayList<Pair<String, ImageData>>()
        val normalMaps = ArrayList<Pair<String, ImageData>>()
        val specularMaps = ArrayList<Pair<String, ImageData>>()

        var textures: TextureArray? = null
        var normals: TextureArray? = null
        var speculars: TextureArray? = null

        fun getTextureIndex(path: String): Int {
            val texturePath = "$path.png"

            if (colorMaps.any { colorMap -> colorMap.first == texturePath }) {
                return colorMaps.indexOf(colorMaps.find { colorMap -> colorMap.first == texturePath })
            }

            return -1
        }

        fun getNormalIndex(path: String): Int {
            val normalPath = "${path}_n.png"

            if (normalMaps.any { normalMap -> normalMap.first == normalPath }) {
                return normalMaps.indexOf(normalMaps.find { normalMap -> normalMap.first == normalPath })
            }

            return -1
        }

        fun getSpecularIndex(path: String): Int {
            val specularPath = "${path}_s.png"

            if (specularMaps.any { specularMap -> specularMap.first == specularPath }) {
                return specularMaps.indexOf(specularMaps.find { specularMap -> specularMap.first == specularPath })
            }

            return -1
        }
    }
}