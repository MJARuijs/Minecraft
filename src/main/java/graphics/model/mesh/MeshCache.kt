package graphics.model.mesh

import math.matrices.Matrix4
import org.lwjgl.assimp.AIMesh
import resources.Cache

object MeshCache : Cache<Mesh>(MeshLoader()) {

    fun get(aiMesh: AIMesh, transformation: Matrix4): Mesh {
        return (loader as MeshLoader).parseData(aiMesh, transformation)
    }

}