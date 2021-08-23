package graphics.model

import graphics.material.ColoredMaterial
import graphics.material.Material
import graphics.model.mesh.MeshCache
import math.Color
import math.matrices.Matrix4
import org.lwjgl.BufferUtils
import org.lwjgl.assimp.*
import org.lwjgl.assimp.Assimp.*
import resources.Loader
import util.File

class ModelLoader: Loader<Model> {

    override fun load(path: String): Model {
        val scene = loadScene(path)
        val root = scene.mRootNode() ?: throw Exception("Scene does not contain root node")
        val shapes = parseShapes(scene, root)

        return Model(shapes)
    }

    private fun loadScene(path: String) = aiImportFile(
            File(path).getPath(),
            aiProcess_Triangulate or aiProcess_OptimizeGraph or aiProcess_RemoveRedundantMaterials
    ) ?: throw Exception("Could not load scene: $path")

    private fun parseShapes(scene: AIScene, node: AINode): List<Shape> {
        val materials = ArrayList<Material>()
        val aiMaterials = scene.mMaterials()
        for (index in 0 until scene.mNumMaterials()) {
            materials += parseMaterial(AIMaterial.create(aiMaterials!!.get(index)))
        }

        val shapes = ArrayList<Shape>()
        val aiMeshes = scene.mMeshes()
        for (i in 0 until scene.mNumMeshes()) {

            val aiTransformation = node.mTransformation()
            val aiMesh = AIMesh.create(aiMeshes!!.get(i))

            val material = materials[aiMesh.mMaterialIndex()]
            val transformation = parseMatrix(aiTransformation)
            shapes += Shape(MeshCache.get(aiMesh, transformation), material)
        }

        return shapes
    }

    private fun parseMaterial(aiMaterial: AIMaterial) = ColoredMaterial(
            getColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE),
            getColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR),
            getFloat(aiMaterial, AI_MATKEY_SHININESS)
    )

    private fun getColor(material: AIMaterial, key: String): Color {
        val aiColor = AIColor4D.create()
        val result = aiGetMaterialColor(material, key, aiTextureType_NONE, 0, aiColor)
        return if (result == 0) {
            Color(aiColor.r(), aiColor.g(), aiColor.b(), aiColor.a())
        } else {
            Color()
        }
    }

    @Suppress("SameParameterValue")
    private fun getFloat(material: AIMaterial, key: String): Float {
        val intBuffer = BufferUtils.createIntBuffer(1)
        val floatBuffer = BufferUtils.createFloatBuffer(1)
        val result = aiGetMaterialFloatArray(material, key, aiTextureType_NONE, 1, floatBuffer, intBuffer)
        return if (result == 0) {
            floatBuffer.get()
        } else {
            50.0f
        }
    }

    private fun parseMatrix(aiMatrix: AIMatrix4x4): Matrix4 {
        return Matrix4(floatArrayOf(
                aiMatrix.a1(), aiMatrix.a2(), aiMatrix.a3(), aiMatrix.a4(),
                aiMatrix.b1(), aiMatrix.b2(), aiMatrix.b3(), aiMatrix.b4(),
                aiMatrix.c1(), aiMatrix.c2(), aiMatrix.c3(), aiMatrix.c4(),
                aiMatrix.d1(), aiMatrix.d2(), aiMatrix.d3(), aiMatrix.d4())
        )
    }

}