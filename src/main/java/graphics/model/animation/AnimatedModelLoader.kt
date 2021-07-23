package graphics.model.animation

import graphics.material.ColoredMaterial
import graphics.material.Material
import graphics.model.Shape
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Mesh
import graphics.model.mesh.Primitive
import math.Color
import math.Quaternion
import math.matrices.Matrix3
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import math.vectors.Vector4
import resources.Loader
import util.File
import kotlin.math.PI

class AnimatedModelLoader: Loader<AnimatedModel> {

    private class GeometryData(val materialId: String, val positions: ArrayList<Vector3>, val normals: ArrayList<Vector3>, val textureCoordinates: ArrayList<Vector2>, val indexData: IntArray, val attributes: List<Attribute>)

    private class MeshJointWeights(val jointIds: Vector4, val weights: Vector4)

    private class Attribute(val name: String, val offset: Int)

    private var currentBoneIndex = 0
    private val bones = ArrayList<Joint>()
    private val globalJoints = HashMap<String, Pair<Int, Matrix4>>()
    private val rotationMatrix = Matrix4().rotateX(-PI.toFloat() / 2f)
    private var transformationMatrix = Matrix4()

    override fun load(path: String): AnimatedModel {

        val file = File(path)
        val content = file.getContent()

        val materialContent = getContent("library_effects", content)
        val geometryContent = getContent("library_geometries", content)
        val shapeContent = getContent("library_visual_scenes", content)
        val jointContent = getContent("library_controllers", content)
        val keyframeContent = try {
            getContent("library_animations", content)
        } catch (e: Exception) {
            ""
        }

        val rootJoint = getJointHierarchy(shapeContent)
        indexJoints(rootJoint)

        val meshJointWeights = getJointMeshData(jointContent, rootJoint)

        val materials = parseMaterials(materialContent)
        val geometry = getGeometryData(geometryContent)

        val shapes = createAnimatedShapes(meshJointWeights, materials, geometry)

        val poses = if (keyframeContent.isNotBlank()) {
            loadPoses(keyframeContent)
        } else {
            listOf()
        }

        rootJoint.initWorldTransformation(Matrix4())

        return AnimatedModel(shapes, rootJoint, poses)
    }

    private fun getContent(name: String, content: String): String {
        val startIndex = content.indexOf("<$name>")
        val endIndex = content.indexOf("</$name>")
        return content.substring(startIndex, endIndex)
    }

    private fun getJointMeshData(content: String, rootJoint: Joint): HashMap<String, List<MeshJointWeights>> {
        val controllerContents = content.split("</controller>")

        val meshJointWeights = HashMap<String, List<MeshJointWeights>>()

        for (controllerContent in controllerContents) {
            if (controllerContent.isBlank()) {
                continue
            }

            val meshId = getString("<skin source", controllerContent).removePrefix("#")
            val requiredJointsContent = getTagContent("source", 0, controllerContent)
            val inverseBindMatrixContent = getTagContent("source", 1, controllerContent)
            val weightContent = getTagContent("source", 2, controllerContent)
            val vertexWeightContent = getTagContent("vertex_weights", 0, controllerContent)
            val influencingJoints = getArray("<Name_array", requiredJointsContent)
            val inverseBindMatrices = getFloatArray("<float_array", inverseBindMatrixContent)

            transformationMatrix = Matrix4(getFloatArray("<bind_shape_matrix>", controllerContent))

            for (i in influencingJoints.indices) {
                val requiredJoint = globalJoints[influencingJoints[i]] ?: continue
                if (!requiredJoint.second.isZeroMatrix()) {
                    continue
                }


                val values = inverseBindMatrices.copyOfRange(i * 16, (i + 1)* 16)
                val invBindMatrix = Matrix4(values)

                rootJoint.setInverseBindMatrix(requiredJoint.first, invBindMatrix)
            }

            val weights = getFloatArray("<float_array", weightContent)

            val weightsPerVertices = getIntArray("<vcount>", vertexWeightContent)
            val vertexJointWeights = getIntArray("<v>", vertexWeightContent)

            val finalWeights = ArrayList<MeshJointWeights>()

            var counter = 0

            for (i in weightsPerVertices.indices) {
                val weightsPerVertex = weightsPerVertices[i]

                val jointIndices = Vector4()
                val jointWeights = Vector4()

                for (j in counter until counter + weightsPerVertex) {
                    if (j - counter >= 4) {
                        continue
                    }
                    val localJointIndex = vertexJointWeights[j * 2]
                    val jointName = influencingJoints[localJointIndex]
                    val globalJointIndex = globalJoints[jointName]?.first ?: throw IllegalArgumentException("No global joint was found with Id: $jointName, necessary for mesh: $meshId")

                    jointIndices[j - counter] = globalJointIndex.toFloat()

                    val weightIndex = vertexJointWeights[j * 2 + 1]
                    val weight = weights[weightIndex]
                    jointWeights[j - counter] = weight
                }
                counter += weightsPerVertex
                finalWeights += MeshJointWeights(jointIndices, scale(jointWeights))
            }

            meshJointWeights[meshId] = finalWeights
        }

        return meshJointWeights
    }

    private fun createAnimatedShapes(meshJointWeights: HashMap<String, List<MeshJointWeights>>, materials: HashMap<String, Material>, geometries: HashMap<String, ArrayList<GeometryData>>): List<Shape> {
        val shapes = ArrayList<Shape>()

        for (geometry in geometries) {
            for (shapeData in geometry.value) {
                val material = materials[shapeData.materialId] ?: throw IllegalArgumentException("No material found for id: ${shapeData.materialId}")
                val mesh = createGeometry(Matrix4(), shapeData, meshJointWeights[geometry.key]!!)

                shapes += Shape(mesh, material)
            }
        }

        return shapes
    }

    private fun getJointHierarchy(content: String): Joint {
        val lines = content.split("\n")
        return getBoneData(lines, 0).first
    }

    private fun indexJoints(joint: Joint) {
        if (!globalJoints.contains(joint.name)) {
            globalJoints[joint.name] = Pair(globalJoints.size, Matrix4(FloatArray(16)))
        }
        for (child in joint.children) {
            indexJoints(child)
        }
    }

    private fun getBoneData(lines: List<String>, index: Int): Pair<Joint, Int> {
        val jointId = currentBoneIndex++
        var i = index
        var name = ""
        var localJointTransformation = Matrix4()
        val children = ArrayList<Joint>()

        while (true) {
            val line = lines[i].trim()

            if (line.startsWith("<node id")) {
                val type = getString("type", line)
                if (type == "JOINT") {

                    if (!name.isBlank()) {
                        if (getString("name", line) == name) {
                            i++
                            continue
                        } else {
                            val result = getBoneData(lines, i)
                            children += result.first
                            i = result.second
                        }
                    } else {
                        name = getString("sid", line)
                        val bindMatrixLine = lines[i + 1].trim()
                        localJointTransformation = Matrix4(getFloatArray("transform", bindMatrixLine))
                    }
                } else if (type == "NODE") {
                    i += 1
                }
            } else if (line.contains("</node>")) {
                if (jointId == 0) {
                    localJointTransformation = rotationMatrix dot localJointTransformation
                }
                val jointData = Joint(name, jointId, children, localJointTransformation)
                bones += jointData
                return Pair(jointData, i)
            }

            i++
        }
    }

    private fun getGeometryData(content: String): HashMap<String, ArrayList<GeometryData>> {
        val geometries = HashMap<String, ArrayList<GeometryData>>()
        val geometryContents = content.split("</geometry>")

        for (geometryContent in geometryContents) {
            if (geometryContent.isBlank()) {
                continue
            }

            val id = getId(geometryContent)

            val shapesStartIndex = geometryContent.indexOf("<triangles")

            val shapeContents = geometryContent.substring(shapesStartIndex).split("</triangles>")

            val shapes = ArrayList<GeometryData>()

            for (shapeContent in shapeContents) {
                if (!shapeContent.trim().startsWith("<triangles")) {
                    continue
                }

                val attributes = ArrayList<Attribute>()

                for (line in shapeContent.lines()) {
                    if (line.trim().startsWith("<input semantic")) {
                        val attributeName = getString("input semantic", line)
                        val offset = getString("offset", line).toInt()
                        attributes += Attribute(attributeName, offset)
                    }
                }

                val materialId = getString("<triangles material", shapeContent)

                val positions = getFloatArray("$id-positions-array", geometryContent)
                val normalValues = getFloatArray("$id-normals-array", geometryContent)
                val textureValues = try {
                    getFloatArray("$id-map-0-array", geometryContent)
                } catch (e: IllegalArgumentException) {
                    FloatArray(0)
                }

                val indices = getIntArray("<p>", shapeContent)

                val vertices = ArrayList<Vector3>()
                val normals = ArrayList<Vector3>()
                val textureCoordinates = ArrayList<Vector2>()

                for (i in positions.indices step 3) {
                    vertices += Vector3(positions[i], positions[i + 1], positions[i + 2])
                }

                for (i in normalValues.indices step 3) {
                    normals += Vector3(normalValues[i], normalValues[i + 1], normalValues[i + 2])
                }

                for (i in textureValues.indices step 2) {
                    textureCoordinates += Vector2(textureValues[i], textureValues[i + 1])
                }

                shapes += GeometryData(materialId, vertices, normals, textureCoordinates, indices, attributes)
            }

            geometries[id] = shapes
        }

        return geometries
    }

    private fun createGeometry(transformation: Matrix4, geometryData: GeometryData, meshJointWeights: List<MeshJointWeights>): Mesh {
        var indices = IntArray(0)

        val containsTextureCoordinates = geometryData.attributes.any { attribute -> attribute.name == "TEXCOORD" }

        val stepSize = geometryData.attributes.size

        val positionOffset = geometryData.attributes.find { attribute -> attribute.name == "VERTEX" }?.offset ?: -1
        val normalOffset = geometryData.attributes.find { attribute -> attribute.name == "NORMAL" }?.offset ?: -1
        val texCoordOffset = geometryData.attributes.find { attribute -> attribute.name == "TEXCOORD" }?.offset ?: -1

        var vertexData = FloatArray(0)

        for (i in geometryData.indexData.indices step stepSize) {

            val positionIndex = geometryData.indexData[i + positionOffset]
            val normalIndex = geometryData.indexData[i + normalOffset]

            val textureIndex = if (containsTextureCoordinates) {
                geometryData.indexData[i + texCoordOffset]
            } else {
                null
            }

            val position = (transformationMatrix.dot(geometryData.positions[positionIndex])).toArray()
            val normal = (Matrix3(transformationMatrix).dot(geometryData.normals[normalIndex])).toArray()
            vertexData += position
            vertexData += normal

            if (textureIndex != null) {
                vertexData += geometryData.textureCoordinates[textureIndex].toArray()
            }

            val boneIds = meshJointWeights[positionIndex].jointIds.toArray()
            val boneWeights = meshJointWeights[positionIndex].weights.toArray()
            vertexData += boneIds
            vertexData += boneWeights

            indices += indices.size
        }

        val attributes = arrayListOf(Attribute(0, 3), Attribute(1, 3), Attribute(2, 4), Attribute(3, 4))

        if (containsTextureCoordinates) {
            attributes += Attribute(2, 2)
        }

        return Mesh(Layout(Primitive.TRIANGLE, attributes), vertexData, indices)
    }

    private fun loadPoses(content: String): List<Pose> {
        val animationContents = content.split("</animation>")

        val poses = ArrayList<Pose>()

        val poseTransformations = ArrayList<HashMap<String, JointTransformation>>()
        for (animationContent in animationContents) {
            if (animationContent.isBlank()) {
                continue
            }

            val id = getString("<animation id", animationContent).removePrefix("Armature_").removeSuffix("_pose_matrix")
            var boneId = ""

            bones.forEach { bone ->
                if (id == bone.name) {
                    boneId = bone.name
                }
            }

            val poseContent = getTagContent("source", 1, animationContent)
            val poseData = getFloatArray("<float_array", poseContent)

            val numberOfMatrices = poseData.size / 16

            if (poseTransformations.size != numberOfMatrices) {
                for (i in 0 until numberOfMatrices) {
                    poseTransformations += HashMap()
                }
            }

            for (i in 0 until numberOfMatrices) {
                val poseMatrix = if (boneId == "Bone") {
                    rotationMatrix dot Matrix4(poseData.copyOfRange(i * 16, (i + 1) * 16))
                } else {
                    Matrix4(poseData.copyOfRange(i * 16, (i + 1) * 16))
                }

                val jointTransformation = JointTransformation(poseMatrix.getPosition(), Quaternion.fromMatrix(poseMatrix))
                poseTransformations[i][boneId] = jointTransformation
            }
        }

        for (jointTransforms in poseTransformations) {
            poses += Pose(jointTransforms)
        }

        return poses
    }

    private fun parseMaterials(content: String): HashMap<String, Material> {
        val materials = HashMap<String, Material>()

        val materialContents = content.split("</effect>")

        for (materialContent in materialContents) {
            if (materialContent.isBlank()) {
                continue
            }

            val id = getId(materialContent).replace("-effect", "-material")
            val diffuse = getColor("diffuse", materialContent)
            val specular = getColor("specular", materialContent)
            val shininess = try {
                getFloatValue("shininess", materialContent)
            } catch (e: IllegalArgumentException) {
                0.0f
            }

            val material = ColoredMaterial(diffuse, specular, shininess)
            materials[id] = material
        }

        return materials
    }

    private fun getId(content: String): String {
        val startIndex = content.indexOf("id=") + 4
        val endIndex = content.indexOf("\"", startIndex)
        return content.substring(startIndex, endIndex)
    }

    private fun getFloatValue(name: String, content: String): Float {
        val nameIndex = content.indexOf("\"$name\"")
        if (nameIndex == -1) {
            throw IllegalArgumentException("No value was found for parameter $name")
        }

        val startIndex = content.indexOf(">", nameIndex + name.length) + 1
        val endIndex = content.indexOf("<", startIndex + 1)
        return content.substring(startIndex, endIndex).toFloat()
    }

    private fun getColor(name: String, content: String): Color {
        val values = try {
            getFloatArray("<color sid=\"$name\">", content)
        } catch (e: IllegalArgumentException) {
            floatArrayOf(0f, 0f, 0f, 0f)
        }
        if (values.size != 4) {
            throw IllegalArgumentException("Invalid color: $name")
        }

        return Color(values[0], values[1], values[2], values[3])
    }

    private fun getArray(name: String, content: String): List<String> {
        val nameIndex = content.indexOf(name)
        if (nameIndex == -1) {
            throw IllegalArgumentException("No array found with name: $name \n\n $content")
        }

        val startIndex = content.indexOf(">", nameIndex ) + 1
        val endIndex = content.indexOf("<", startIndex + 1)

        return content.substring(startIndex, endIndex).trim().split(" ")
    }

    private fun getString(name: String, content: String): String {
        val nameIndex = content.indexOf(name)
        if (nameIndex == -1) {
            throw IllegalArgumentException("No attribute with name $name was found in $content")
        }

        val startIndex = content.indexOf("\"", nameIndex) + 1
        val endIndex = content.indexOf("\"", startIndex)

        return content.substring(startIndex, endIndex)
    }

    private fun getTagContent(tagName: String, occurrence: Int, content: String, isOneLine: Boolean = false): String {
        var searchIndex = 0
        var timesFound = 0

        while (true) {
            val startIndex = content.indexOf("<$tagName", searchIndex) + 1
            val endIndex = if (isOneLine) {
                content.indexOf("/>", startIndex)
            } else {
                content.indexOf("</$tagName>", startIndex)
            }

            if (timesFound == occurrence) {
                return content.substring(startIndex, endIndex)
            } else {
                timesFound++
                searchIndex = endIndex
            }
        }
    }

    private fun getFloatArray(name: String, content: String): FloatArray {
        return getArray(name, content).map(String::toFloat).toFloatArray()
    }

    private fun getIntArray(name: String, content: String): IntArray {
        return getArray(name, content).map(String::toInt).toIntArray()
    }

    private fun scale(weights: Vector4): Vector4 {
        val sum = weights.x + weights.y + weights.z + weights.w
        return weights / sum
    }

}