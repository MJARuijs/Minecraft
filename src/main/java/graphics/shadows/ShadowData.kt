package graphics.shadows

import graphics.textures.DepthMap
import math.matrices.Matrix4
import math.vectors.Vector3

class ShadowData(val projectionMatrix: Matrix4, val viewMatrix: Matrix4, val shadowDistance: Float, val shadowMap: DepthMap) {

    fun getShadowMatrix(): Matrix4 {
        var shadowMatrix = Matrix4()
        shadowMatrix = shadowMatrix.translate(Vector3(0.5f, 0.5f, 0.5f))
        shadowMatrix = shadowMatrix.scale(Vector3(0.5f, 0.5f, 0.5f))
        return shadowMatrix dot projectionMatrix dot viewMatrix
    }

}