package chunks

import chunks.blocks.BlockType
import math.vectors.Vector3

data class ChunkData(val x: Int, val z: Int, val highestBlock: Int, val biome: Biome, val blocks: ArrayList<Pair<BlockType, Vector3>> = ArrayList())