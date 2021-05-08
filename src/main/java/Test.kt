import resources.images.ImageCache
import resources.images.ImageScaler

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val resizer = ImageScaler()

        val image = ImageCache.get("textures/blocks/blocks.png")
        resizer.scale(image, 256, 256)
    }

}