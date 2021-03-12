package chunks.blocks

import graphics.textures.ImageMap
import resources.images.ImageCache

enum class BlockType(val textures: ImageMap? = null) {

    AIR,
    BEDROCK(ImageMap(ImageCache.get("textures/blocks/bedrock.png"))),
    DIRT(ImageMap(ImageCache.get("textures/blocks/dirt.jpg")))

}