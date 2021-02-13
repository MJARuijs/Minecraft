package util

import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Retrieve a file from the provided location and prepare methods for reading the samples contained within the file.
 * @param location the file's location.
 * @param charset the file's character encoding (default UTF-8).
 * @throws Exception the file could not be found or accessed.
 * @constructor
 */
class File(location: String, createIfAbsent: Boolean = false, private val charset: Charset = UTF_8) {

    private var path: Path

    init {
        try {
            val loader = ClassLoader.getSystemClassLoader()
            val url = loader.getResource(location) ?: throw Exception("Could not find file: $location")
            val uri = url.toURI()
            path = Paths.get(uri)
        } catch (exception: Exception) {
            if (createIfAbsent) {
                println(location)
//                File("resources/$location").createNewFile()
                val uriTest = Thread.currentThread().contextClassLoader.getDefinedPackage("fonts/arial.fnt")
                println(uriTest.name)
//                File(uriTest).createNewFile()
                val loader = ClassLoader.getSystemClassLoader()
                val url = loader.getResource(location) ?: throw Exception("Could not find file: $location")
                val uri = url.toURI()
                path = Paths.get(uri)
            } else {
                throw Exception("Could not retrieve file: $location", exception)
            }
        }
    }

    /**
     * Retrieve the full path leading to the file.
     * @return the full file path.
     */
    fun getPath() = path.toAbsolutePath().toString()

    /**
     * @return the file's extension.
     */
    fun getExtension() = getPath().substringAfterLast('.', "")

    /**
     * Read all bytes from the file.
     * @return the bytes read from the file.
     * @throws Exception the file could not be accessed or read.
     */
    fun getBytes() = Files.readAllBytes(path) ?: throw Exception("Could not read all bytes")

    /**
     * Read the textual content from the file.
     * @return the content read from the file.
     * @throws Exception the file could not be accessed or read.
     */
    fun getContent() = String(getBytes(), charset)

    /**
     * Read all userinterface.text lines from the file.
     * @return the lines read from the file.
     * @throws Exception the file could not be accessed or read.
     */
    fun getLines() = Files.readAllLines(path, charset) ?: throw Exception("Could not read all bytes")

    fun write(lines: ArrayList<String>) {
        val writer = FileWriter(path.toFile())
        writer.write(lines.joinToString("\n"))
        writer.close()
    }

    fun createIfAbsent(): util.File {
        File(path.toString()).createNewFile()
        return this
    }
}