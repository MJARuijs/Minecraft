package graphics.shaders

import resources.Loader
import util.File

class ShaderLoader: Loader<Shader> {

    private fun getType(file: File): ShaderType {
        val extension = file.getExtension()
        return when {
            extension.contains("vert", true) -> ShaderType.VERTEX
            extension.contains("frag", true) -> ShaderType.FRAGMENT
            extension.contains("geom", true) -> ShaderType.GEOMETRY
            else -> throw Exception("Unknown shader extension: $extension")
        }
    }

    override fun load(path: String): Shader {
        val file = File(path)
        val type = getType(file)
        var source = ""

        file.getLines().forEach { line -> source += "$line\n" }
        return Shader(type, source)
    }

}