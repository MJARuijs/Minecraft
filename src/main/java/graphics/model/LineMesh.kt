package graphics.model

import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Mesh
import graphics.model.mesh.Primitive

class LineMesh(segments: List<Segment>) : Mesh(
        Layout(Primitive.LINE, Attribute(0, 3)),
        segments.map(Segment::toArray).flatMap(FloatArray::toList).toFloatArray(),
        (segments.indices).flatMap { index -> listOf(2 * index, 2 * index + 1)}.toIntArray()
) {
    constructor(vararg segments: Segment) : this(segments.toList())
}