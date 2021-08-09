import devices.Window
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.model.animation.model.Joint
import graphics.rendertarget.RenderTargetManager
import math.Color
import math.matrices.Matrix4
import math.vectors.Vector3
import userinterface.UniversalParameters
import userinterface.text.font.FontLoader

object Test {

    data class TestModel(val rootJoint: Joint) {

        constructor(model: TestModel): this((model.rootJoint.copy()))

        fun translate(a: Vector3) {
            rootJoint.initWorldTransformation(Matrix4().translate(a))
        }

    }

    class TestEntity(val model: TestModel) {

        fun translate(a: Vector3) {
            model.translate(a)
        }

    }

    private val window = Window("Minecraft", GraphicsContext::resize)

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0f, 0f, 0f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING, GraphicsOption.MULTI_SAMPLE)

        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))
        RenderTargetManager.init(window)
        val t1 = get()


        val e1 = TestEntity(TestModel(t1))
        val e2 = TestEntity(TestModel(t1))
//        println(e1.model.rootJoint.worldTransformation)
//        println()
        e1.translate(Vector3(0f, 3f, 0f))

        println(e1.model.rootJoint.worldTransformation)
        println(e2.model.rootJoint.worldTransformation)

    }

    fun get(): TestModel {
        val childJoint = Joint("child", 1, arrayListOf(), Matrix4().translate(0f, 0f, -2f))
        val rootJoint = Joint("root", 0, arrayListOf(childJoint), Matrix4().translate(4f, 0f, 0f))
        rootJoint.initWorldTransformation(Matrix4())
//        println(rootJoint.worldTransformation)
        return TestModel(rootJoint)
    }

}