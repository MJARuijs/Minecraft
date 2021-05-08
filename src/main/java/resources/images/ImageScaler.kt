package resources.images

import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBImageResize
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

class ImageScaler {

    fun scale(image: ImageData, newWidth: Int, newHeight: Int): ImageData {
        val pixels = createByteBuffer(newWidth * newHeight * 4)
//        STBImageResize.stbir_resize_uint8(
//                image.data,
//                image.width,
//                image.height,
//                image.width * 4,
//                pixels,
//                newWidth,
//                newHeight,
//                newWidth * 4,
//                STBImageResize.STBIR_TYPE_UINT8,
//                4,
//                4,
//                0,
//                STBImageResize.STBIR_EDGE_ZERO,
//                STBImageResize.STBIR_EDGE_ZERO,
//                STBImageResize.STBIR_FILTER_TRIANGLE,
//                STBImageResize.STBIR_FILTER_TRIANGLE,
//                STBImageResize.STBIR_COLORSPACE_SRGB
//        )

        STBImageResize.stbir_resize_uint8(
                image.data,
                image.width,
                image.height,
                image.width * 4,
                pixels,
                newWidth,
                newHeight,
                newWidth * 4,
                4
        )


        STBImageWrite.stbi_write_png("test.png", newWidth, newHeight, 4, image.data, 4 * newWidth)
        return ImageData(newWidth, newHeight, pixels)
    }

}