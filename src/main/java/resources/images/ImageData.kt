package resources.images

import resources.Resource
import java.nio.ByteBuffer

class ImageData(val width: Int, val height: Int, val data: ByteBuffer): Resource {

    override fun destroy() {
        data.clear()
    }

}