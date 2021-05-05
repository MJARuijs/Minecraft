package chunks

import chunks.blocks.BlockType
import math.Color

enum class Biome(val octaves: Int, val amplitude: Int, val smoothness: Int, val overlayColor: Color, vararg val blocks: Pair<BlockType, Int>) {

    PLANES(1, 3, 3, Color(50, 200, 50), Pair(BlockType.GRASS, 1), Pair(BlockType.DIRT, 3)),
    HILLS(1, 20, 3, Color(50, 200, 50), Pair(BlockType.GRASS, 1), Pair(BlockType.DIRT, 3)),
//    MOUNTAINS(3, 20, 5, Color(50, 200, 50), Pair(BlockType.STONE, 1)),
//    DESERT(1, 3, 3, Color(), Pair(BlockType.SAND, 3), Pair(BlockType.SAND_STONE, 3))

}