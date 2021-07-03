package graphics.model.animation

import graphics.model.ModelLoader
import graphics.model.Shape
import math.matrices.Matrix4
import org.lwjgl.assimp.*
import resources.Loader
import util.File
import java.nio.charset.StandardCharsets
import kotlin.math.PI

class AnimatedModelLoader2: Loader<AnimatedModel> {

    private val rotation = Matrix4().rotateX(PI.toFloat() / 2.0f)

    override fun load(path: String): AnimatedModel {
        val scene = loadScene(path)
        val rootNode = scene.mRootNode() ?: throw IllegalArgumentException("No rootNode was found")

        val shapes = getShapes(scene, rootNode)

        val rootBone = try {
            getBoneData(scene)
        } catch (e: Exception) {
            throw Exception("No root bone was found. Are you sure this model is rigged? $path")
        }

        return AnimatedModel(shapes, rootBone!!)
    }

    private fun loadScene(path: String) = Assimp.aiImportFile(
            File(path).getPath(),
            Assimp.aiProcess_Triangulate or Assimp.aiProcess_OptimizeGraph or Assimp.aiProcess_RemoveRedundantMaterials
    ) ?: throw Exception("Could not load scene: $path")

    private fun getShapes(scene: AIScene, rootNode: AINode): List<Shape> {

        return ModelLoader().parseShapes(scene, rootNode, true)
    }

    private fun getBoneData(scene: AIScene): Bone? {
        val rootNode = scene.mRootNode() ?: return null
        val rootName = rootNode.mName().toString()
        val bindMatrix = parseMatrix(rootNode.mTransformation())

        val rootBone = Bone(rootName, bindMatrix)
        println("ROOT BONE: $rootName, $bindMatrix")

        print(rootNode)

//        for (i in 0 until rootNode.mNumChildren()) {
//            val childBone = AIBone.create(rootNode.mChildren()!!.get(i))
//            val childNode = AINode.create(rootNode.mChildren()!!.get(i))
//            val childName = StandardCharsets.UTF_8.decode(childBone.mName().data()).toString()
//            println("Child: $childName")
////            childBone.
//        }
        return rootBone
    }

    private fun print(node: AINode) {

        for (i in 0 until node.mNumChildren()) {

            val childBone = AIBone.create(node.mChildren()!!.get(i))
            val childName = StandardCharsets.UTF_8.decode(childBone.mName().data()).toString()
            val childNode = AINode.create(node.mChildren()!!.get(i))

            println("Child: $childName ${parseMatrix(childBone.mOffsetMatrix())} ${rotation dot parseMatrix(childNode.mTransformation())} ")
            println()

            print(childNode)

//            childBone.
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