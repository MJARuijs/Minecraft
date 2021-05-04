package chunks.blocks

import tools.ToolType

enum class BlockType2(val bestTool: ToolType, val hardness: Float, val textureIndices: IntArray) {

    GRASS(ToolType.SHOVEL, 0.5f, intArrayOf(3, 3, 0, 2, 3, 3)),
    DIRT(ToolType.SHOVEL, 0.5f, intArrayOf(2, 2, 2, 2, 2, 2)),
    STONE(ToolType.PICK_AXE, 2f, intArrayOf(1, 1, 1, 1, 1, 1))

}