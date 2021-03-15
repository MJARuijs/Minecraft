package chunks.blocks

import math.vectors.Vector2

enum class BlockType(private vararg val texturePositions: Vector2) {

    AIR,
    BEDROCK(Vector2(1, 1)),
    DIRT(Vector2(2, 0)),
    STONE(Vector2(1, 0)),
    FURNACE(Vector2(12, 2), Vector2(13, 2), Vector2(13, 2), Vector2(13, 2), Vector2(14, 3), Vector2(14, 3)),
    TNT(Vector2(8, 0), Vector2(8, 0), Vector2(8, 0), Vector2(8, 0), Vector2(9, 0), Vector2(10, 0));

    fun getOffsets(): FloatArray {
        var textureOffsets = FloatArray(0)

        if (texturePositions.size == 1) {
            for (i in 0 until 6) {
                textureOffsets += texturePositions[0].x / 16.0f
                textureOffsets += texturePositions[0].y / 16.0f
            }
        } else if (texturePositions.size == 6) {
            for (i in 0 until 6) {
                textureOffsets += texturePositions[i].x / 16.0f
                textureOffsets += texturePositions[i].y / 16.0f
            }
        }

        return textureOffsets
    }

    companion object {
        private const val TEXTURE_COLUMNS = 16
        private const val TEXTURE_ROWS = 16
    }
}