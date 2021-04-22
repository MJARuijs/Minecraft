package userinterface

import math.Color
import math.vectors.Vector3
import math.vectors.Vector4

enum class UIColor(val color: Color) {

    WHITE(1.0f, 1.0f, 1.0f, 1.0f),
    GREY_LIGHT(WHITE.rgb() * 0.75f, 1.0f),
    GREY(0.5f, 0.5f, 0.5f, 1.0f),
    GREY_DARK(0.25f, 0.25f, 0.25f, 1.0f),
    BLACK(0f, 0f, 0f, 1.0f),

    RED_BRIGHT(1.0f, 0.0f, 0.0f, 1.0f),
    RED_LIGHT(0.75f, 0.0f, 0.0f, 1.0f),
    RED(0.5f, 0.0f, 0.0f, 1.0f),
    RED_DARK(0.25f, 0.0f, 0.0f, 1.0f),

    GREEN_BRIGHT(0.0f, 1.0f, 0.0f, 1.0f),
    GREEN_LIGHT(0.0f, 0.75f, 0.0f, 1.0f),
    GREEN(0.0f, 0.5f, 0.0f, 1.0f),
    GREEN_DARK(0.0f, 0.25f, 0.0f, 1.0f),

    BLUE_BRIGHT(0.0f, 0.0f, 1.0f, 1.0f),
    BLUE_LIGHT(0.0f, 0.0f, 0.75f, 1.0f),
    BLUE(0.0f, 0.0f, 0.5f, 1.0f),
    BLUE_DARK(0.0f, 0.0f, 0.25f, 1.0f),

    YELLOW_BRIGHT(1.0f, 1.0f, 0.0f, 1.0f),
    YELLOW_LIGHT(YELLOW_BRIGHT.rgb() * 0.75f, 1.0f),
    YELLOW(YELLOW_BRIGHT.rgb() * 0.5f, 1.0f),
    YELLOW_DARK(YELLOW_BRIGHT.rgb() * 0.25f, 1.0f),

    CYAN_BRIGHT(0.0f, 1.0f, 1.0f, 1.0f),
    CYAN_LIGHT(CYAN_BRIGHT.rgb() * 0.75f, 1.0f),
    CYAN(CYAN_BRIGHT.rgb() * 0.5f, 1.0f),
    CYAN_DARK(CYAN_BRIGHT.rgb() * 0.25f, 1.0f),

    PURPLE_BRIGHT(1.0f, 0.0f, 1.0f, 1.0f),
    PURPLE_LIGHT(PURPLE_BRIGHT.rgb() * 0.75f, 1.0f),
    PURPLE(PURPLE_BRIGHT.rgb() * 0.5f, 1.0f),
    PURPLE_DARK(PURPLE_BRIGHT.rgb() * 0.25f, 1.0f),

    TRANSPARENT(0f, 0f, 0f, 0f);

    constructor(r: Float, g: Float, b: Float, a: Float) : this(Color(r, g, b, a))

    constructor(rgb: Vector3, a: Float) : this(Color(rgb, a))

    fun getValues() = Vector4(color.r, color.g, color.b, color.a)

    fun rgb() = color.rgb()

}