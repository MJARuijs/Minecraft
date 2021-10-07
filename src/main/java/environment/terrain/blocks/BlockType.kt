package environment.terrain.blocks

import environment.terrain.FaceTextures
import environment.terrain.chunks.FaceDirection
import game.tools.ToolType

enum class BlockType(val bestTool: ToolType, val hardness: Float, private var textureIndices: IntArray, private var normalIndices: IntArray, private var specularIndices: IntArray) {

    GRASS(ToolType.SHOVEL, 0.5f, FaceMaterial("grass_block_side"), FaceMaterial("grass_block_top", FaceDirection.TOP), FaceMaterial("dirt", FaceDirection.BOTTOM)),
    DIRT(ToolType.SHOVEL, 0.5f, FaceMaterial("dirt")),
    STONE(ToolType.PICK_AXE, 2f, FaceMaterial("stone")),
    COBBLE_STONE(ToolType.PICK_AXE, 3f, FaceMaterial("cobblestone")),
    DIAMOND_ORE(ToolType.PICK_AXE, 5f, FaceMaterial("diamond_ore"));
//    PLANKS(ToolType.AXE, 0f, intArrayOf(4, 4, 4, 4, 4, 4)),
//    TNT(ToolType.ANY, 0f, intArrayOf(8, 8, 9, 10, 8, 8));

    constructor(bestTool: ToolType, hardness: Float, vararg faceMaterials: FaceMaterial) : this(bestTool, hardness, intArrayOf(), intArrayOf(), intArrayOf()) {
        val generalTexture = faceMaterials.find { material -> material.side == null }
        if (generalTexture != null) {
            val textureIndex = FaceTextures.getTextureIndex(generalTexture.texturePath)
            val normalIndex = FaceTextures.getNormalIndex(generalTexture.texturePath)
            val specularIndex = FaceTextures.getSpecularIndex(generalTexture.texturePath)

            for (i in 0 until 6) {
                textureIndices += textureIndex
                normalIndices += normalIndex
                specularIndices += specularIndex
            }
        } else {
            for (i in 0 until 6) {
                textureIndices += -1
                normalIndices += -1
                specularIndices += -1
            }
        }

        for (material in faceMaterials) {
            if (material.side == null) {
                continue
            }

            textureIndices[FaceDirection.values().indexOf(material.side)] = FaceTextures.getTextureIndex(material.texturePath)
            normalIndices[FaceDirection.values().indexOf(material.side)] = FaceTextures.getNormalIndex(material.texturePath)
            specularIndices[FaceDirection.values().indexOf(material.side)] = FaceTextures.getSpecularIndex(material.texturePath)
        }
    }

    fun getTextureIndex(direction: FaceDirection): Int {
        return textureIndices[FaceDirection.values().indexOf(direction)]
    }

    fun getNormalIndex(direction: FaceDirection): Int {
        return normalIndices[FaceDirection.values().indexOf(direction)]
    }

    fun getSpecularIndex(direction: FaceDirection): Int {
        return specularIndices[FaceDirection.values().indexOf(direction)]
    }

}