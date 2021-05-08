package resources.images

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBImage
import resources.Loader
import util.File

class ImageLoader: Loader<ImageData> {

    override fun load(path: String): ImageData {
//        val scaler = ImageScaler()

        val file = File(path)
        val absolutePath = file.getPath()

        val widthBuffer = BufferUtils.createIntBuffer(1)
        val heightBuffer = BufferUtils.createIntBuffer(1)
        val channelBuffer = BufferUtils.createIntBuffer(1)

        val pixels = STBImage.stbi_load(absolutePath, widthBuffer, heightBuffer, channelBuffer, 4)
                ?: throw IllegalArgumentException("Could not find texture file: $path")

        val width = widthBuffer.get()
        val height = heightBuffer.get()
//        return scaler.scale(ImageData(width, height, pixels), width * 2, height * 2)
        return ImageData(width, height, pixels)
    }

}