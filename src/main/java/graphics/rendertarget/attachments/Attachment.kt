package graphics.rendertarget.attachments

interface Attachment {

    fun resize(width: Int, height: Int)

    fun destroy()
}