package userinterface.text

import math.vectors.Vector4

class MetaData(val size: Int, padding: Vector4, val lineHeight: Int, val base: Int, val scaleW: Int, val scaleH: Int) {

    val paddingTop = padding[0]

    val paddingRight = padding[1]

    val paddingBottom = padding[2]

    val paddingLeft = padding[3]

    val paddingWidth = paddingLeft + paddingRight

    val paddingHeight = paddingTop + paddingBottom
}