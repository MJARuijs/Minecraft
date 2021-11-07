import devices.Key
import devices.Timer
import devices.Window
import environment.sky.SkyBox
import environment.terrain.chunks.Chunk
import environment.terrain.chunks.ChunkGenerator
import environment.terrain.chunks.ChunkManager
import environment.terrain.chunks.ChunkRenderer
import game.player.Player
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.entity.AnimatedEntity
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.animation.LoopEffect
import graphics.model.animation.model.AnimatedModelCache
import graphics.renderer.RenderData
import graphics.renderer.RenderEngine
import graphics.renderer.RenderType
import graphics.rendertarget.RenderTargetManager
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowBox
import math.Color
import math.matrices.Matrix4
import math.vectors.Vector3
import userinterface.UIColor
import userinterface.UIPage
import userinterface.UniversalParameters
import userinterface.UserInterface
import userinterface.items.Item
import userinterface.items.backgrounds.TexturedBackground
import userinterface.layout.constraints.ConstraintDirection
import userinterface.layout.constraints.ConstraintSet
import userinterface.layout.constraints.constrainttypes.AspectRatioConstraint
import userinterface.layout.constraints.constrainttypes.CenterConstraint
import userinterface.layout.constraints.constrainttypes.RelativeConstraint
import userinterface.text.font.FontLoader

object Main {

    private val window = Window("Minecraft", GraphicsContext::resize)
    private val keyboard = window.keyboard
    private val mouse = window.mouse
    private val timer = Timer()

    private const val lightValue = 0.75f
    private const val directionalValue = 0.5f

    private val ambientLight = AmbientLight(Color(lightValue, lightValue, lightValue))
    private val sun = Sun(Color(directionalValue, directionalValue, directionalValue), Vector3(1.0f, 1.0f, -1.0f))

    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, ChunkGenerator.TERRAIN_HEIGHT + 2, 5))
//    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, 0, 5))

    private val chunkManager = ChunkManager(camera.position)
    private val chunkRenderer = ChunkRenderer()

    private val renderEngine = RenderEngine()
    private val entities = ArrayList<Entity>()
    private val entityRenderer = EntityRenderer()

    private val skyBox = SkyBox("textures/sky/box", camera.zFar)

    private var chunks = ArrayList<Chunk>()

    private val ui = UserInterface(window.aspectRatio)
    private val page = UIPage("page")

    private const val sampleSize = 40
    private var printPerformance = false
    private val fps = FloatArray(sampleSize)

    private var controlPlayer = false

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0f, 0f, 0f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING, GraphicsOption.MULTI_SAMPLE)

        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))
        RenderTargetManager.init(window)

        renderEngine += ShadowBox(camera)

        val crossHair = Item("crossHair", ConstraintSet(
                CenterConstraint(ConstraintDirection.HORIZONTAL),
                CenterConstraint(ConstraintDirection.VERTICAL),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.05f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
        ), TexturedBackground("textures/userinterface/crosshair.jpg", null, UIColor.GREY))

        page += crossHair

        ui += page
        ui.showPage("page")

        val animatedModel = AnimatedModelCache.get("models/animatedPlayer.dae")

        animatedModel.addAnimation("walking", listOf(
                Pair(1, 400),
                Pair(2, 800)
        ), LoopEffect.REVERSE)

        val player = Player(animatedModel, Matrix4().translate(Vector3(0, ChunkGenerator.TERRAIN_HEIGHT + 1, 0)))
        val secondPlayer = AnimatedEntity(animatedModel, Matrix4().translate(Vector3(3, ChunkGenerator.TERRAIN_HEIGHT + 1, 0)))

        entities += player
        entities += secondPlayer

        val boneProgram = ShaderProgram.load("shaders/debug/bone.vert", "shaders/debug/bone.frag")

        var i = 0

        timer.reset()
        mouse.capture()

        while (!window.isClosed()) {
            window.poll()

            processInput()

            chunks = chunkManager.update(camera.position, mouse, camera)

            entities.forEach { entity -> entity.update(timer.getDelta()) }

            renderEngine.render(camera, ambientLight, sun, skyBox, arrayListOf(
                    RenderData(entities, entityRenderer, RenderType.FORWARD),
                    RenderData(chunks, chunkRenderer, RenderType.FORWARD)
            ))

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)

            window.synchronize()
            timer.update()

            if (printPerformance) {
                i = updatePerformance(i)
            }
        }

    }

    private fun processInput() {
        if (keyboard.isPressed(Key.ESCAPE)) {
            mouse.toggle()
        }

        if (keyboard.isPressed(Key.F1) || keyboard.isPressed(Key.KP1)) {
            window.close()
        }

        if (mouse.isCaptured()) {
            camera.update(keyboard, mouse, timer.getDelta())
        }

        if (keyboard.isPressed(Key.UP)) {
            chunkManager.setRenderDistance(chunkManager.getRenderDistance() + 1)
        }

        if (keyboard.isPressed(Key.DOWN)) {
            chunkManager.setRenderDistance(chunkManager.getRenderDistance() - 1)
        }

        if (keyboard.isPressed(Key.P)) {
            printPerformance = !printPerformance
        }

        if (keyboard.isPressed(Key.F2)) {
            controlPlayer = !controlPlayer
        }
    }

    private fun updatePerformance(i: Int): Int {
        fps[i] = 1f / timer.getDelta()
        if (i + 1 == sampleSize) {
            var totalFps = 0f
            for (j in 0 until sampleSize) {
                totalFps += fps[j]
            }
            println("Average fps: ${totalFps / sampleSize}")
            return 0
        }
        return i + 1
    }

    private fun renderJoints(boneProgram: ShaderProgram, player: AnimatedEntity) {
        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING)
        boneProgram.start()
        boneProgram.set("projection", camera.projectionMatrix)
        boneProgram.set("view", camera.viewMatrix)

        val joints = player.model.getJoints()
        for (joint in joints) {
            joint.render(boneProgram)
        }

        boneProgram.stop()
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING)
    }
}