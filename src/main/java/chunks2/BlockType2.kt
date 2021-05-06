package chunks2

import tools.ToolType

enum class BlockType2(val bestTool: ToolType, val hardness: Float, val textureIndices: IntArray) {

    GRASS(ToolType.SHOVEL, 0.5f, intArrayOf(3, 3, 0, 2, 3, 3)),
    DIRT(ToolType.SHOVEL, 0.5f, intArrayOf(2, 2, 2, 2, 2, 2)),
    STONE(ToolType.PICK_AXE, 2f, intArrayOf(1, 1, 1, 1, 1, 1)),
    PLANKS(ToolType.AXE, 0f, intArrayOf(4, 4, 4, 4, 4, 4)),
    TNT(ToolType.ANY, 0f, intArrayOf(8, 8, 9, 10, 8, 8));

    operator fun get(direction: FaceDirection): Int {
        return textureIndices[FaceDirection.values().indexOf(direction)]
    }

}