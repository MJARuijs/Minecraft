import chunks.*
import chunks.ChunkGenerator.Companion.TERRAIN_HEIGHT
import chunks.blocks.BlockType
import chunks.blocks.Selector
import devices.Button
import devices.Key
import devices.Timer
import devices.Window
import environment.sky.SkyBox
import graphics.*
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.LineMesh
import graphics.model.ModelCache
import graphics.model.Segment
import graphics.rendertarget.RenderTargetManager
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowBox
import graphics.shadows.ShadowData
import graphics.shadows.ShadowRenderer
import math.Color
import math.matrices.Matrix
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glColor3f
import org.lwjgl.opengl.GL11.glLineWidth
import tools.ToolMaterial
import tools.ToolType
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
import java.lang.Math.PI

object Main {

    private val window = Window("Minecraft", GraphicsContext::resize)
    private val keyboard = window.keyboard
    private val mouse = window.mouse
    private val timer = Timer()

    private const val lightValue = 0.75f
    private const val directionalValue = 0.5f

    private val ambientLight = AmbientLight(Color(lightValue, lightValue, lightValue))
    private val sun = Sun(Color(directionalValue, directionalValue, directionalValue), Vector3(0.0f, 0.0f, -1.0f))

    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, 0, 0))
//    private val player = Player(Vector3(-80, TERRAIN_HEIGHT, 0))

    private val chunkManager = ChunkManager(camera.position)
    private val cunkRenderer = ChunkRenderer()

    private val selector = Selector()
    private val skyBox = SkyBox("textures/sky/box", camera.zFar)

    private var chunks = ArrayList<Chunk>()

    private val ui = UserInterface(window.aspectRatio)
    private val page = UIPage("page")

    private var renderDepthMap = false

    val cube = Entity(ModelCache.get("models/block.obj"), Matrix4().translate(0.0f, 52f, 0.0f))
    lateinit var shadowBox: ShadowBox

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0.25f, 0.25f, 0.25f))
        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING)

        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))

        RenderTargetManager.init(window)
        shadowBox = ShadowBox(camera)
        ShadowRenderer += shadowBox

        val uiProgram = ShaderProgram.load("shaders/userinterface/user_interface.vert", "shaders/userinterface/user_interface.frag")
        val entityProgram = ShaderProgram.load("shaders/entities/entity.vert", "shaders/entities/entity.frag")
//        val shadowRenderTarget = RenderTargetManager.get()

        val depthTexture = Texture(Vector2(0.0f, 0.0f), Vector2(0.25f, 0.5f))

        val crossHair = Item("crossHair", ConstraintSet(
                CenterConstraint(ConstraintDirection.HORIZONTAL),
                CenterConstraint(ConstraintDirection.VERTICAL),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.05f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
        ), TexturedBackground("textures/userinterface/crosshair.jpg", null, UIColor.GREY))

        page += crossHair

        ui += page
        ui.showPage("page")


        val sampleSize = 40

        val fps = FloatArray(sampleSize)
        var i = 0

        timer.reset()
        mouse.capture()
        updateChunkManager()

        while (!window.isClosed()) {
            window.poll()

            processInput()

//            updateChunkManager()

//            if (mouse.isCaptured()) {
//                player.update(keyboard, mouse, timer.getDelta())
//            }
//            camera.followPlayer(player)
            val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera, false)
//            val selectedBlock = null

//            chunkRenderer.render(chunks, camera, ambientLight, directionalLight, selectedBlock)

            val shadows = ShadowRenderer.render(camera, sun, chunks, renderDepthMap, chunkRenderer)

            doMainRenderPass(selectedBlock, shadows)

            entityProgram.start()
            entityProgram.set("projection", camera.projectionMatrix)
            entityProgram.set("view", camera.viewMatrix)

//            println(camera.viewMatrix)
//            if (keyboard.isPressed(Key.L)) {
//                camera.rotation.y += PI.toFloat()
//            }
            cube.render(entityProgram)

            entityProgram.stop()

            glLineWidth(10f)
//            glColor3f(1.0f, 0.0f, 0.0f)
            lineProgram.start()
            lineProgram.set("projection", camera.projectionMatrix)
            lineProgram.set("view", camera.viewMatrix)
            lineProgram.set("model", Matrix4())
            lineProgram.set("color", Vector3(1.0f, 0.0f, 0.0f))
            lineMesh.draw()
            lineProgram.set("color", Vector3(1.0f, 1.0f, 0.0f))

            lineMesh2.draw()
            lineProgram.stop()

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)

            ShadowRenderer.render(camera, sun, chunks, renderDepthMap, chunkRenderer)

            window.synchronize()
            timer.update()

            fps[i] = 1f / timer.getDelta()
            i++
            if (i == sampleSize) {
                i = 0
                var totalFps = 0f
                for (j in 0 until sampleSize) {
                    totalFps += fps[j]
                }
//                println("Average fps: ${totalFps / sampleSize}")
            }
        }

        window.destroy()
    }

    val lineProgram = ShaderProgram.load("shaders/line.vert", "shaders/line.frag")
    var lineMesh = LineMesh()

    var lineMesh2 = LineMesh()

    private fun updateChunkManager() {
        chunkManager.updatePosition(camera.position)
        chunks = chunkManager.determineVisibleChunks()
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

        if (keyboard.isPressed(Key.F)) {
            println(camera.position)
        }

        if (keyboard.isPressed(Key.UP)) {
            chunkManager.setRenderDistance(chunkManager.getRenderDistance() + 1)
        }

        if (keyboard.isPressed(Key.T)) {
            renderDepthMap = !renderDepthMap
        }

        if (keyboard.isPressed(Key.DOWN)) {
            chunkManager.setRenderDistance(chunkManager.getRenderDistance() - 1)
        }

        if (mouse.isCaptured()) {
            if (mouse.isPressed(Button.LEFT)) {
                val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera, false)
                if (selectedBlock != null) {
                    for (chunk in chunks) {
                        if (chunk.containsBlock(selectedBlock.second)) {
                            chunk.startBreakingBlock(selectedBlock.second, ToolType.SHOVEL, ToolMaterial.GOLD)
                        }
                    }
                }
            }

            if (mouse.isPressed(Button.RIGHT) || keyboard.isPressed(Key.V)) {
                val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera, false)
                if (selectedBlock != null) {
                    val face = selector.determineSelectedFace(camera, selectedBlock.second) ?: return
                    val newPosition = chunkManager.newBlockPosition(selectedBlock.second, face)
                    for (chunk in chunks) {
                        if (chunk.containsBlock(newPosition)) {
                            chunk.addBlock(BlockType.GRASS, newPosition)
                        }
                    }
                }
            }

            if (mouse.isReleased(Button.LEFT)) {
                chunkManager.stopBreaking()
            }

            if (keyboard.isPressed(Key.R)) {
                camera.position = Vector3()
                camera.rotation = Vector3()
            }

            if (keyboard.isPressed(Key.K)) {
                cube.transformation = Matrix4().translate(shadowBox.translation)

                val segments = ArrayList<Segment>()
                segments += Segment(shadowBox.farLeftDown, shadowBox.farLeftUp)
                segments += Segment(shadowBox.farLeftDown, shadowBox.farRightDown)
                segments += Segment(shadowBox.farLeftDown, shadowBox.nearLeftDown)

                segments += Segment(shadowBox.nearRightUp, shadowBox.nearRightDown)
                segments += Segment(shadowBox.nearRightUp, shadowBox.farRightUp)
                segments += Segment(shadowBox.nearRightUp, shadowBox.nearLeftUp)

                segments += Segment(shadowBox.nearLeftUp, shadowBox.farLeftUp)
                segments += Segment(shadowBox.nearLeftUp, shadowBox.nearLeftDown)

                segments += Segment(shadowBox.nearLeftDown, shadowBox.nearRightDown)
                segments += Segment(shadowBox.nearRightDown, shadowBox.farRightDown)

                segments += Segment(shadowBox.farRightUp, shadowBox.farRightDown)
                segments += Segment(shadowBox.farRightUp, shadowBox.farLeftUp)

                lineMesh = LineMesh(segments)

                val segments2 = ArrayList<Segment>()

                var offset = Vector3(shadowBox.width() / 2.0f, shadowBox.height() / 2.0f, shadowBox.depth() / 2.0f)
                offset = camera.position

                segments2 += Segment(offset + Vector3(shadowBox.minX, shadowBox.minY, shadowBox.minZ), offset + Vector3(shadowBox.minX, shadowBox.minY, shadowBox.maxZ))
                segments2 += Segment(offset + Vector3(shadowBox.minX, shadowBox.minY, shadowBox.minZ), offset + Vector3(shadowBox.minX, shadowBox.minY, shadowBox.minZ))
                segments2 += Segment(offset + Vector3(shadowBox.minX, shadowBox.minY, shadowBox.minZ), offset + Vector3(shadowBox.maxX, shadowBox.minY, shadowBox.minZ))
                segments2 += Segment(offset + Vector3(shadowBox.maxX, shadowBox.maxY, shadowBox.maxZ), offset + Vector3(shadowBox.minX, shadowBox.maxY, shadowBox.maxZ))
                segments2 += Segment(offset + Vector3(shadowBox.maxX, shadowBox.maxY, shadowBox.maxZ), offset + Vector3(shadowBox.maxX, shadowBox.minY, shadowBox.maxZ))
                segments2 += Segment(offset + Vector3(shadowBox.maxX, shadowBox.maxY, shadowBox.maxZ), offset + Vector3(shadowBox.maxX, shadowBox.maxY, shadowBox.minZ))

                lineMesh2 = LineMesh(segments2)
                println(shadowBox.translation)
            }
        }
    }

    private fun doMainRenderPass(selectedBlock: Pair<Chunk, Vector3>?, shadows: List<ShadowData>) {
        RenderTargetManager.getDefault().start()
        RenderTargetManager.getDefault().clear()
        skyBox.render(camera)

        chunkRenderer.render(chunks, camera, ambientLight, sun, shadows, selectedBlock)
    }
}