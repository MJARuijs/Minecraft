package graphics.model.animation

import graphics.material.ColoredMaterial
import graphics.material.Material
import graphics.model.Shape
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Mesh
import graphics.model.mesh.Primitive
import math.Color
import math.matrices.Matrix3
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import math.vectors.Vector4
import resources.Loader
import util.File
import kotlin.math.PI

class AnimatedModelLoader: Loader<AnimatedModel> {

    private class GeometryData(val materialId: String, val positions: ArrayList<Vector3>, val normals: ArrayList<Vector3>, val textureCoordinates: ArrayList<Vector2>, val indexData: IntArray)

    private class MeshJointWeights(val jointIds: Vector4, val weights: Vector4)

    private var currentBoneIndex = 0
    private val bones = ArrayList<Bone>()
    private val globalJoints = HashMap<String, Pair<Int, Matrix4>>()
    private val rotationMatrix = Matrix4().rotateX(-PI.toFloat() / 2f)

    override fun load(path: String): AnimatedModel {

        val file = File(path)
        val content = file.getContent()

        val materialContent = getContent("library_effects", content)
        val geometryContent = getContent("library_geometries", content)
        val shapeContent = getContent("library_visual_scenes", content)
        val jointContent = getContent("library_controllers", content)

        val rootJoint = getJointHierarchy(shapeContent)
        indexJoints(rootJoint)

        val meshJointWeights = getJointMeshData(jointContent, rootJoint)

        val materials = parseMaterials(materialContent)
        val geometry = getGeometryData(geometryContent)

        val shapes = createAnimatedShapes(meshJointWeights, materials, geometry)
        return AnimatedModel(shapes, rootJoint)
    }

    private fun getContent(name: String, content: String): String {
        val startIndex = content.indexOf("<$name>")
        val endIndex = content.indexOf("</$name>")
        return content.substring(startIndex, endIndex)
    }

    private fun getJointMeshData(content: String, rootJoint: Bone): HashMap<String, List<MeshJointWeights>> {
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
            val bindMatrix = rotationMatrix dot Matrix4(getFloatArray("<bind_shape_matrix>", controllerContent))
            val influencingJoints = getArray("<Name_array", requiredJointsContent)

            val inverseBindMatrices = getFloatArray("<float_array", inverseBindMatrixContent)

            for (i in influencingJoints.indices) {
                val requiredJoint = globalJoints[influencingJoints[i]] ?: continue
                if (!requiredJoint.second.isZeroMatrix()) {
                    continue
                }

                val values = inverseBindMatrices.copyOfRange(i * 16, i * 16 + 16)
                val invBindMatrix = Matrix4(values)
                rootJoint.setTransformation(requiredJoint.first, bindMatrix)
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
                    val globalJointIndex = globalJoints[jointName]?.first ?: throw IllegalArgumentException("No global joint was found with Id: $jointName")

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

    private fun createAnimatedShapes(meshJointWeights: HashMap<String, List<MeshJointWeights>>, materials: HashMap<String, Material>, geometries: HashMap<String, GeometryData>): List<Shape> {
        val shapes = ArrayList<Shape>()

        for (geometry in geometries) {
            val material = materials[geometry.value.materialId] ?: throw IllegalArgumentException("No material found for id: ${geometry.value.materialId}")
            val mesh = createGeometry(rotationMatrix, geometry.value, meshJointWeights[geometry.key]!!)

            shapes += Shape(mesh, material)
        }

        return shapes
    }

    private fun getJointHierarchy(content: String): Bone {
        val lines = content.split("\n")
        return getBoneData(lines, 0).first
    }

    private fun indexJoints(joint: Bone) {
        if (!globalJoints.contains(joint.name)) {
            globalJoints[joint.name] = Pair(globalJoints.size, Matrix4(FloatArray(16)))
        }
        for (child in joint.children) {
            indexJoints(child)
        }
    }

    private fun getBoneData(lines: List<String>, index: Int): Pair<Bone, Int> {
        val boneId = currentBoneIndex++
        var i = index
        var name = ""
        val children = ArrayList<Bone>()

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
                        name = getString("name", line)
                    }
                } else if (type == "NODE") {
                    i += 1
                }
            } else if (line.contains("</instance_controller")) {
                val jointData = Bone(name, boneId, Matrix4(FloatArray(16)), children)
                bones += jointData
                return Pair(jointData, i)
            }

            i++
            if (i >= lines.size) {
                val jointData = Bone(name, boneId, Matrix4(FloatArray(16)), children)
                bones += jointData
                return Pair(jointData, i)
            }
        }
    }

    private fun createGeometry(transformation: Matrix4, geometryData: GeometryData, meshJointWeights: List<MeshJointWeights>): Mesh {
        var indices = IntArray(0)

        val containsTextureCoordinates = geometryData.textureCoordinates.isNotEmpty()

        val stepSize = if (containsTextureCoordinates) 3 else 2

        var vertexData = FloatArray(0)

        for ((j, i) in (geometryData.indexData.indices step stepSize).withIndex()) {

            val positionIndex = geometryData.indexData[i]
            val normalIndex = geometryData.indexData[i + 1]

            val textureIndex = if (stepSize == 3) {
                geometryData.indexData[i + 2]
            } else {
                null
            }

            vertexData += transformation.dot(geometryData.positions[positionIndex]).toArray()
            vertexData += Matrix3(transformation).dot(geometryData.normals[normalIndex]).toArray()

            if (textureIndex != null) {
                vertexData += geometryData.textureCoordinates[textureIndex].toArray()
            }

            vertexData +=meshJointWeights[positionIndex].jointIds.toArray()
            vertexData +=meshJointWeights[positionIndex].weights.toArray()

            indices += indices.size
        }

        val attributes = arrayListOf(Attribute(0, 3), Attribute(1, 3), Attribute(2, 4), Attribute(3, 4))

        if (containsTextureCoordinates) {
//            attributes += Attribute(2, 2)
        }

        return Mesh(Layout(Primitive.TRIANGLE, attributes), vertexData, indices)
    }

    private fun getGeometryData(content: String): HashMap<String, GeometryData> {
        val geometries = HashMap<String, GeometryData>()
        val geometryContents = content.split("</geometry>")

        for (geometryContent in geometryContents) {
            if (geometryContent.isBlank()) {
                continue
            }

            val id = getId(geometryContent)

            val materialId = getString("<triangles material", geometryContent)
            val positions = getFloatArray("$id-positions-array", geometryContent)
            val normalValues = getFloatArray("$id-normals-array", geometryContent)
            val textureValues = try {
                getFloatArray("$id-map-0-array", geometryContent)
            } catch (e: IllegalArgumentException) {
                FloatArray(0)
            }

            val indices = getIntArray("<p>", geometryContent)

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

            geometries[id] = GeometryData(materialId, vertices, normals, textureCoordinates, indices)
        }

        return geometries
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

    private fun getTagContent(tagName: String, occurrence: Int, content: String): String {
        var searchIndex = 0
        var timesFound = 0

        while (true) {
            val startIndex = content.indexOf("<$tagName", searchIndex) + 1
            val endIndex = content.indexOf("</$tagName>", startIndex)

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