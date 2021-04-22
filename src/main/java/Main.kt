import chunks.*
import chunks.ChunkGenerator.Companion.TERRAIN_HEIGHT
import chunks.blocks.BlockType
import chunks.blocks.Selector
import devices.Button
import devices.Key
import devices.Timer
import devices.Window
import environment.sky.SkyBox
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.DirectionalLight
import graphics.rendertarget.RenderTargetManager
import math.Color
import math.vectors.Vector3
import tools.ToolMaterial
import tools.ToolType
import userinterface.UIColor
import userinterface.UIPage
import userinterface.UniversalParameters
import userinterface.UserInterface
import userinterface.items.Item
import userinterface.items.TextBox
import userinterface.items.backgrounds.TexturedBackground
import userinterface.layout.constraints.ConstraintDirection
import userinterface.layout.constraints.ConstraintSet
import userinterface.layout.constraints.constrainttypes.AspectRatioConstraint
import userinterface.layout.constraints.constrainttypes.CenterConstraint
import userinterface.layout.constraints.constrainttypes.PixelConstraint
import userinterface.layout.constraints.constrainttypes.RelativeConstraint
import userinterface.text.font.FontLoader

object Main {
    private val window = Window("Minecraft", GraphicsContext::resize)
    private val keyboard = window.keyboard
    private val mouse = window.mouse
    private val timer = Timer()

    private val lightValue = 0.75f
    private val directionalValue = 0.5f

    private val ambientLight = AmbientLight(Color(lightValue, lightValue, lightValue))
    private val directionalLight = DirectionalLight(Color(directionalValue, directionalValue, directionalValue), Vector3(0.5f, 0.5f, 0.5f))

    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(-50, TERRAIN_HEIGHT, -14))
//    private val player = Player(Vector3(-80, TERRAIN_HEIGHT, 0))

    private val chunkManager = ChunkManager(camera.position)
    private val chunkRenderer = ChunkRenderer()

    private val selector = Selector()
    private val skyBox = SkyBox("textures/sky/box", camera.zFar)

    private var chunks = ArrayList<Chunk>()

    private val ui = UserInterface(window.aspectRatio)
    private val page = UIPage("page")

    private var renderColored = false

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0.25f, 0.25f, 0.25f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING)
        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))

        RenderTargetManager.init(window)

        val textBox = TextBox("fps", ConstraintSet(
                PixelConstraint(ConstraintDirection.TO_LEFT),
                PixelConstraint(ConstraintDirection.TO_TOP),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.1f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.5f)
        ), "fps", window.aspectRatio, 1.0f)

        val crossHair = Item("crossHair", ConstraintSet(
                CenterConstraint(ConstraintDirection.HORIZONTAL),
                CenterConstraint(ConstraintDirection.VERTICAL),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.05f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
        ), TexturedBackground("textures/userinterface/crosshair.jpg", null, UIColor.GREY))

        page += textBox
        page += crossHair

        ui += page
        ui.showPage("page")

        Thread {
//            val chunk = ChunkGenerator().generate(0, 0, Biome.PLANES, 0)
//            chunks.add(chunk)
        }.start()

        val sampleSize = 40

        val fps = FloatArray(sampleSize)
        var i = 0

        timer.reset()
        mouse.capture()
        while (!window.isClosed()) {
            window.poll()

            processInput()

            updateChunkManager()

//            if (mouse.isCaptured()) {
//                player.update(keyboard, mouse, timer.getDelta())
//            }
//            camera.followPlayer(player)

            val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera, renderColored)
            doMainRenderPass(selectedBlock)

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)

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
            renderColored = !renderColored
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
                            chunk.startBreakingBlock(selectedBlock.second, ToolType.PICK_AXE, ToolMaterial.GOLD)
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
        }
    }

    private fun doMainRenderPass(selectedBlock: Pair<Chunk, Vector3>?) {
        RenderTargetManager.getDefault().start()
        RenderTargetManager.getDefault().clear()
        skyBox.render(camera)

        chunkRenderer.render(chunks, camera, ambientLight, directionalLight, selectedBlock)
    }
}