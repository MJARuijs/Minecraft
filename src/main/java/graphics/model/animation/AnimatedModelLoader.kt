package graphics.model.animation

import graphics.material.ColoredMaterial
import graphics.material.Material
import graphics.model.Shape
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Mesh
import graphics.model.mesh.Primitive
import math.Color
import math.matrices.Matrix4
import math.vectors.Vector3
import resources.Loader
import util.File

class AnimatedModelLoader: Loader<AnimatedModel> {

    override fun load(path: String): AnimatedModel {

        val rootJoint = Joint("root", Matrix4(), listOf())

        val file = File(path)
        val content = file.getContent()

        val materialContent = getContent("library_effects", content)
        val geometryContent = getContent("library_geometries", content)
        val shapeContent = getContent("library_visual_scenes", content)

        val materials = parseMaterials(materialContent)
        val geometry = parseGeometry(geometryContent)
        val shapes = parseShapes(shapeContent, materials, geometry)

        return AnimatedModel(shapes, rootJoint)
    }

    private fun getContent(name: String, content: String): String {
        val startIndex = content.indexOf("<$name>")
        val endIndex = content.indexOf("</$name>")
        return content.substring(startIndex, endIndex)
    }

    private fun parseShapes(content: String, materials: HashMap<String, Material>, geometries: HashMap<String, Mesh>): List<Shape> {
        val shapes = ArrayList<Shape>()

        val shapeContents = content.split("</node>")

        for (shapeContent in shapeContents) {
            if (shapeContent.isBlank() || !shapeContent.contains("node")) {
                continue
            }

            val id = getString("node id", shapeContent)

            val transformation = getMatrix("transform", shapeContent)
            val geometryId = getString("instance_geometry url", shapeContent).removePrefix("#")
            val materialId = getString("instance_material", shapeContent).removePrefix("#")

            val mesh = geometries[geometryId] ?: throw IllegalArgumentException("Required geometry was not found: $geometryId")
            val material = materials[materialId] ?: throw IllegalArgumentException("Required material was not found: $materialId")
            shapes += Shape(mesh, material)
        }

        return shapes
    }

    private fun parseGeometry(content: String): HashMap<String, Mesh> {
        val geometries = HashMap<String, Mesh>()
        val geometryContents = content.split("</geometry>")

        for (geometryContent in geometryContents) {
            if (geometryContent.isBlank()) {
                continue
            }

            val id = getId(geometryContent)

            val positions = getFloatArray("$id-positions-array", geometryContent)
            val normalValues = getFloatArray("$id-normals-array", geometryContent)
            val textureValues = getFloatArray("$id-map-0-array", geometryContent)

            val indexData = getIntArray("p", geometryContent)
            for (i in indexData) {
                print("$i ")
            }
            println()
            println()
            println()

            val vertices = ArrayList<Vector3>()
            val normals = ArrayList<Vector3>()
//            var vertexData = FloatArray(0)

//            for (i in positions.indices step 3) {
//                vertices += Vector3(positions[i], positions[i + 1], positions[i + 2])
////                vertexData += Vector3(positions[i], positions[i + 1], positions[i + 2]).toArray()
//            }
//
//            for (i in normalValues.indices step 3) {
//                normals += Vector3(normalValues[i], normalValues[i + 1], normalValues[i + 2])
////                vertexData += Vector3(normalValues[i], normalValues[i + 1], normalValues[i + 2]).toArray()
//            }
//
//            for (i in 0 until vertices.size) {
//                vertexData += vertices[i].toArray()
//                vertexData += normals[i].toArray()
//            }

            val attributes = arrayListOf(Attribute(0, 3))

            if (normalValues.isNotEmpty()) {
                attributes += Attribute(1, 3)
            }

            if (textureValues.isNotEmpty()) {
//                attributes += Attribute(2, 2)
            }

            geometries[id] = Mesh(Layout(Primitive.TRIANGLE, attributes), positions, normalValues, floatArrayOf(), indexData)
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
            val shininess = getFloatValue("shininess", materialContent)

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
        val values = getFloatArray(name, content)
        if (values.size != 4) {
            throw IllegalArgumentException("Invalid color: $name")
        }

        return Color(values[0], values[1], values[2], values[3])
    }

    private fun getFloatArray(name: String, content: String): FloatArray {
        val nameIndex = content.indexOf("\"$name\"")
        if (nameIndex == -1) {
            return FloatArray(0)
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