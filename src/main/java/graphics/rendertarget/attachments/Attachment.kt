package graphics.rendertarget.attachments

interface Attachment {

    fun resize(width: Int, height: Int)

    fun matches(other: Any?): Boolean

    fun destroy()
}