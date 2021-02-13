package graphics.model

import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Mesh
import graphics.model.mesh.Primitive

class Quad : Mesh(
        Layout(
                Primitive.TRIANGLE,
                Attribute(0, 2)
        ),
        floatArrayOf(
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f
        ),
        intArrayOf(
                0, 1, 2,
                0, 2, 3
        )
)