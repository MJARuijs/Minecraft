package chunks.blocks

import math.vectors.Vector2

class FaceMaterial(val texturePosition: Vector2, val face: Face = Face.ALL, val useOverlayColor: Boolean = false) {

    constructor(u: Int, v: Int, face: Face = Face.ALL, useOverlayColor: Boolean = false) : this(Vector2(u, v), face, useOverlayColor)
}