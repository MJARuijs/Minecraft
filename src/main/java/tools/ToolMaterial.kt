package tools

enum class ToolMaterial(val speedMultiplier: Float) {

    DEFAULT(1.0f),
    WOOD(2.0f),
    STONE(4.0f),
    IRON(6.0f),
    GOLD(12.0f),
    DIAMOND(8.0f)
}