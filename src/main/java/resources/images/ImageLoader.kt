package resources.images

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBImage
import resources.Loader
import util.File
import java.nio.ByteBuffer

class ImageLoader: Loader<ImageData> {

    override fun load(path: String): ImageData {

        val file = File(path)
        val absolutePath = file.getPath()

        val widthBuffer = BufferUtils.createIntBuffer(1)
        val heightBuffer = BufferUtils.createIntBuffer(1)
        val channelBuffer = BufferUtils.createIntBuffer(1)

        val pixels = STBImage.stbi_load(absolutePath, widthBuffer, heightBuffer, channelBuffer, 4)
                ?: throw IllegalArgumentException("Could not find texture file: $path")
        
        return ImageData(widthBuffer.get(), heightBuffer.get(), pixels)
    }

}