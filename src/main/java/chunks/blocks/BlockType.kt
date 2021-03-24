package chunks.blocks

enum class BlockType(private vararg val faceMaterials: FaceMaterial) {

    AIR,
    STONE(FaceMaterial(1, 0)),
    DIRT(FaceMaterial(2, 0)),
    GRASS(FaceMaterial(0, 0, Face.TOP, true),
            FaceMaterial(2, 0, Face.BOTTOM, false),
            FaceMaterial(3, 0)
    ),
    PLANKS(FaceMaterial(4, 0)),
    STONE_SLABS(
            FaceMaterial(5, 0),
            FaceMaterial(6, 0, Face.TOP, false),
            FaceMaterial(6, 0, Face.BOTTOM, false)
    ),
    BRICKS(FaceMaterial(7, 0)),
    TNT(
            FaceMaterial(8, 0),
            FaceMaterial(9, 0, Face.TOP, false),
            FaceMaterial(10, 0, Face.BOTTOM, false)
    ),
    COBBLESTONE(FaceMaterial(0, 1)),
    BEDROCK(FaceMaterial(1, 1)),
    SAND(FaceMaterial(2, 1)),
    GRAVEL(FaceMaterial(3, 1)),
    WOOD_LOG(FaceMaterial(4, 1), FaceMaterial(5, 1, Face.TOP, false), FaceMaterial(5, 1, Face.BOTTOM, false)),
    IRON_BLOCK(FaceMaterial(6, 1)),
    GOLD_BLOCK(FaceMaterial(7, 1)),
    EMERALD_BLOCK(FaceMaterial(8, 1)),
    GOLD_ORE(FaceMaterial(0, 2)),
    IRON_ORE(FaceMaterial(1, 2)),
    COAL_ORE(FaceMaterial(2, 2)),
    BOOK_SHELF(FaceMaterial(3, 2)),
    MOSSY_COBBLESTONE(FaceMaterial(4, 2)),
    OBSIDIAN(FaceMaterial(5, 2)),
    SPONGE(FaceMaterial(0, 3)),
    GLASS(FaceMaterial(1, 3)),
    DIAMOND_ORE(FaceMaterial(2, 3)),
    REDSTONE_ORE(FaceMaterial(3, 3)),
    SPARSE_LEAVES(FaceMaterial(4, 3)),
    DENSE_LEAVES(FaceMaterial(5, 3)),
    STONE_BRICKS(FaceMaterial(6, 3)),
    SPAWNER(FaceMaterial(1, 4)),
    SNOW_BLOCK(FaceMaterial(2, 4)),
    SNOW_DIRT(FaceMaterial(4, 4), FaceMaterial(2, 0, Face.BOTTOM), FaceMaterial(2, 4, Face.TOP)),
    ICE(FaceMaterial(3, 4)),
    CACTUS_TOP(FaceMaterial(6, 4), FaceMaterial(5, 4, Face.TOP), FaceMaterial(7, 4, Face.BOTTOM)),
    CACTUS_PART(FaceMaterial(6, 4), FaceMaterial(7, 4, Face.TOP), FaceMaterial(7, 4, Face.BOTTOM)),
    CLAY(FaceMaterial(8, 4)),
    CRAFTING_TABLE(FaceMaterial(11, 2, Face.TOP), FaceMaterial(11, 3, Face.FRONT), FaceMaterial(11, 3, Face.BACK), FaceMaterial(12, 3, Face.LEFT), FaceMaterial(12, 3, Face.RIGHT), FaceMaterial(4, 0, Face.BOTTOM)),

    WHITE_WOOL(FaceMaterial(0, 4)),
    BLACK_WOOL(FaceMaterial(1, 7)),
    DARK_GREY_WOOL(FaceMaterial(2, 7)),
    RED_WOOL(FaceMaterial(1, 8)),
    PINK_WOOL(FaceMaterial(2, 8)),
    DARK_GREEN_WOOL(FaceMaterial(1, 9)),
    LIGHT_GREEN_WOOL(FaceMaterial(2, 9)),
    BROWN_WOOL(FaceMaterial(1, 10)),
    YELLOW_WOOL(FaceMaterial(2, 10)),
    DARK_BLUE_WOOL(FaceMaterial(1, 11)),
    LIGHT_BLUE_WOOL(FaceMaterial(2, 11)),
    PURPLE_WOOL(FaceMaterial(1, 12)),
    MAGENTA_WOOL(FaceMaterial(2, 12)),
    CYAN_WOOL(FaceMaterial(1, 13)),
    ORANGE_WOOL(FaceMaterial(2, 13)),
    LIGHT_GREY_WOOL(FaceMaterial(1, 14));

    fun getOffsets(): FloatArray {
        val textureOffsets = FloatArray(18)

        for (material in faceMaterials) {
            if (material.face == Face.ALL) {
                for (i in 0 until 18 step 3) {
                    textureOffsets[i] = material.texturePosition.x / 16.0f
                    textureOffsets[i + 1] = material.texturePosition.y / 16.0f
                    textureOffsets[i + 2] = if (material.useOverlayColor) 1.0f else 0.0f
                }
            }
        }

        for (i in 0 until faceMaterials.size * 3 step 3) {
            val material = faceMaterials[i / 3]
            if (material.face != Face.ALL) {
                when (material.face) {
                    Face.FRONT -> {
                        textureOffsets[0] = material.texturePosition.x / 16.0f
                        textureOffsets[1] = material.texturePosition.y / 16.0f
                        textureOffsets[2] = if (material.useOverlayColor) 1.0f else 0.0f
                    }
                    Face.BACK -> {
                        textureOffsets[3] = material.texturePosition.x / 16.0f
                        textureOffsets[4] = material.texturePosition.y / 16.0f
                        textureOffsets[5] = if (material.useOverlayColor) 1.0f else 0.0f
                    }
                    Face.RIGHT -> {
                        textureOffsets[6] = material.texturePosition.x / 16.0f
                        textureOffsets[7] = material.texturePosition.y / 16.0f
                        textureOffsets[8] = if (material.useOverlayColor) 1.0f else 0.0f

                    }
                    Face.LEFT -> {
                        textureOffsets[9] = material.texturePosition.x / 16.0f
                        textureOffsets[10] = material.texturePosition.y / 16.0f
                        textureOffsets[11] = if (material.useOverlayColor) 1.0f else 0.0f
                    }
                    Face.TOP -> {
                        textureOffsets[12] = material.texturePosition.x / 16.0f
                        textureOffsets[13] = material.texturePosition.y / 16.0f
                        textureOffsets[14] = if (material.useOverlayColor) 1.0f else 0.0f
                    }
                    Face.BOTTOM -> {
                        textureOffsets[15] = material.texturePosition.x / 16.0f
                        textureOffsets[16] = material.texturePosition.y / 16.0f
                        textureOffsets[17] = if (material.useOverlayColor) 1.0f else 0.0f
                    }
                    Face.ALL -> {}
                }

            }
        }

        return textureOffsets
    }

    companion object {
        private const val TEXTURE_COLUMNS = 16
        private const val TEXTURE_ROWS = 16
    }
}