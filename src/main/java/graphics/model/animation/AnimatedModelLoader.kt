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
import resources.Loader
import util.File
import kotlin.math.PI

class AnimatedModelLoader: Loader<AnimatedModel> {

    private class ShapeData(val materialId: String, val geometryId: String, val transformation: Matrix4)

    private class GeometryData(val positions: ArrayList<Vector3>, val normals: ArrayList<Vector3>, val textureCoordinates: ArrayList<Vector2>, val indexData: IntArray)

    private class JointData(val name: String = "", val transformation: Matrix4 = Matrix4(), val requiredMaterialId: String = "", val children: ArrayList<JointData> = ArrayList()) {

        operator fun plusAssign(child: JointData) {
            children += child
        }

    }

    private val jointIndices = HashMap<String, Int>()
    private val rotationMatrix = Matrix4().rotateX(-PI.toFloat() / 2f)

    override fun load(path: String): AnimatedModel {

        val rootJoint = Bone("root", Matrix4(), listOf())

        val file = File(path)
        val content = file.getContent()

        val materialContent = getContent("library_effects", content)
        val geometryContent = getContent("library_geometries", content)
        val shapeContent = getContent("library_visual_scenes", content)
        val jointContent = getContent("library_controllers", content)

        val riggedShapeRequirements = getJointHierarchy(shapeContent)
        indexJoints(riggedShapeRequirements)

        val materials = parseMaterials(materialContent)

        val geometry = getGeometryData(geometryContent)

//        val shapes = createShapes(riggedShapeRequirements, materials, geometry)


        return AnimatedModel(listOf(), rootJoint)
    }

    private fun getContent(name: String, content: String): String {
        val startIndex = content.indexOf("<$name>")
        val endIndex = content.indexOf("</$name>")
        return content.substring(startIndex, endIndex)
    }

    private fun getJointMeshData(content: String) {
        val controllerContents = content.split("</controller>")

        for (controllerContent in controllerContents) {
            if (controllerContent.isBlank()) {
                continue
            }

            println(controllerContent)
            println()
            println()
            println()
        }
    }

    private fun createShapes(requirements: List<ShapeData>, materials: HashMap<String, Material>, geometries: HashMap<String, GeometryData>): List<Shape> {
        val shapes = ArrayList<Shape>()

        for (requirement in requirements) {
            val material = materials[requirement.materialId] ?: throw IllegalArgumentException("No material found for id: ${requirement.materialId}")
            val shape = createGeometry(requirement.transformation, geometries[requirement.geometryId] ?: throw IllegalArgumentException("No geometry found for id: ${requirement.geometryId}"))

            shapes += Shape(shape, material)
        }

        return shapes
    }

    private fun getJointHierarchy(content: String): JointData {
        val lines = content.split("\n")

        val jointData = getBoneData(lines, 0)
//        println()
//        println()
//        printJointData(jointData.first, 0)
        return jointData.first
    }

    private fun indexJoints(joint: JointData) {
        if (!jointIndices.contains(joint.name)) {
            jointIndices[joint.name] = jointIndices.size
        }
        for (child in joint.children) {
            indexJoints(child)
        }
    }

    private fun printJointData(data: JointData, i: Int) {
        println("${data.name} ${data.requiredMaterialId} ${data.children.size}")
        for (child in data.children) {
            printJointData(child, i + 1)
        }
    }

    private fun getBoneData(lines: List<String>, index: Int): Pair<JointData, Int> {
        var i = index
        var transformationMatrix = Matrix4()
        var name = ""
        val children = ArrayList<JointData>()

        while (true) {
            val line = lines[i].trim()

            if (line.startsWith("<node id")) {
                val type = getString("type", line)
                if (type == "NODE") {

                    val transformationMatrixLine = lines[i + 1].trim()

                    transformationMatrix = try {
                        getMatrix("transform", transformationMatrixLine)
                    } catch (e: IllegalArgumentException) {
                        i++
                        continue
                    }

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
                } else if (type == "JOINT") {
                    i += 1
                }
            } else if (line.contains("<instance_material")) {
                val materialId = getString("instance_material", line)
                val jointData = JointData(name, transformationMatrix, materialId, children)
                return Pair(jointData, i)
            }

            i++
        }
    }

    private fun getShapeRequirements(content: String): List<ShapeData> {
        val requirements = ArrayList<ShapeData>()

        val shapeContents = content.split("</node>")

        for (shapeContent in shapeContents) {
            if (shapeContent.isBlank() || !shapeContent.contains("node")) {
                continue
            }

            val transformation = rotationMatrix dot getMatrix("transform", shapeContent)
            val geometryId = getString("instance_geometry url", shapeContent).removePrefix("#")
            val materialId = getString("instance_material", shapeContent).removePrefix("#")

            requirements += ShapeData(materialId, geometryId, transformation)
        }

        return requirements
    }

    private fun createGeometry(transformation: Matrix4, geometryData: GeometryData): Mesh {
        val data = HashMap<Triple<Int, Int, Int?>, Int>()
        var indices = IntArray(0)

        val containsTextureCoordinates = geometryData.textureCoordinates.isNotEmpty()

        val stepSize = if (containsTextureCoordinates) 3 else 2

        var vertexData = FloatArray(0)

        for (i in geometryData.indexData.indices step stepSize) {

            val positionIndex = geometryData.indexData[i]
            val normalIndex = geometryData.indexData[i + 1]

            val textureIndex = if (stepSize == 3) {
                geometryData.indexData[i + 2]
            } else {
                null
            }

            val index = data[Triple(positionIndex, normalIndex, textureIndex)]
            if (index == null) {
                vertexData += transformation.dot(geometryData.positions[positionIndex]).toArray()
                vertexData += Matrix3(transformation).dot(geometryData.normals[normalIndex]).toArray()
                if (textureIndex != null) {
                    vertexData += geometryData.textureCoordinates[textureIndex].toArray()
                }
                val newIndex = indices.size
                data[Triple(positionIndex, normalIndex, textureIndex)] = newIndex
                indices += newIndex
            } else {
                println("NON-NULL INDEX")
                indices += index
            }
        }

        val attributes = arrayListOf(Attribute(0, 3), Attribute(1, 3))

        if (containsTextureCoordinates) {
            attributes += Attribute(2, 2)
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

            val positions = getFloatArray("$id-positions-array", geometryContent)
            val normalValues = getFloatArray("$id-normals-array", geometryContent)
            val textureValues = try {
                getFloatArray("$id-map-0-array", geometryContent)
            } catch (e: IllegalArgumentException) {
                FloatArray(0)
            }

            val indices = getIntArray("p", geometryContent)

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

            geometries[id] = GeometryData(vertices, normals, textureCoordinates, indices)
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
            getFloatArray(name, content)
        } catch (e: IllegalArgumentException) {
            floatArrayOf(0f, 0f, 0f, 0f)
        }
        if (values.size != 4) {
            throw IllegalArgumentException("Invalid color: $name")
        }

        return Color(values[0], values[1], values[2], values[3])
    }

    private fun getFloatArray(name: String, content: String): FloatArray {
        val nameIndex = content.indexOf("\"$name\"")
        if (nameIndex == -1) {
            throw IllegalArgumentException("No floatarray found with name: $name $content")
        }

        val startIndex = content.indexOf(">", nameIndex + name.length) + 1
        val endIndex = content.indexOf("<", startIndex + 1)

        val values = content.substring(startIndex, endIndex).split(" ")

        return values.map(String::toFloat).toFloatArray()
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

    private fun getIntArray(tag: String, content: String): IntArray {
        val nameIndex = content.indexOf("<$tag>")
        if (nameIndex == -1) {
            return IntArray(0)
        }

        val startIndex = content.indexOf(">", nameIndex + tag.length) + 1
        val endIndex = content.indexOf("<", startIndex + 1)

        val values = content.substring(startIndex, endIndex).split(" ")

        return values.map(String::toInt).toIntArray()
    }

    private fun getMatrix(name: String, content: String): Matrix4 {
        val values = getFloatArray(name, content)
        return Matrix4(values)
    }

}