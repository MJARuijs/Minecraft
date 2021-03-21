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
import player.Player
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

    private val ambientLight = AmbientLight(Color(0.25f, 0.25f, 0.25f))
    private val directionalLight = DirectionalLight(Color(1.0f, 1.0f, 1.0f), Vector3(0.5f, 0.5f, 0.5f))

    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, ChunkGenerator.TERRAIN_HEIGHT, 0))

    private val chunkManager = ChunkManager()
    private val chunkRenderer = ChunkRenderer()

    private val selector = Selector()
    private val skyBox = SkyBox("textures/sky/box", camera.zFar)

    private var chunks = ArrayList<Chunk>()

    private val ui = UserInterface(window.aspectRatio)
    private val page = UIPage("page")

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0.25f, 0.25f, 0.25f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING)
        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))

        RenderTargetManager.init(window)

        val player = Player(Vector3(0, TERRAIN_HEIGHT, 0))

        timer.reset()
        mouse.release()

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
            val chunk = ChunkGenerator().generate(0, 0, Biome.PLANES, 0)
            chunks.add(chunk)
        }.start()

        while (!window.isClosed()) {
            window.poll()

            processInput()

//            updateChunkManager()

            player.update(keyboard, mouse, timer.getDelta())
            camera.followPlayer(player)

            val selectedBlock = selector.getLastSelected()
            doMainRenderPass(selectedBlock)

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)

            window.synchronize()
            timer.update()
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

//        if (mouse.isCaptured()) {
//            camera.update(keyboard, mouse, timer.getDelta())
//        }

        if (keyboard.isPressed(Key.F)) {
            println(camera.position.xz())
        }

        if (mouse.isCaptured()) {
            if (mouse.isPressed(Button.LEFT)) {
                val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera)
                if (selectedBlock != null) {
                    for (chunk in chunks) {
                        if (chunk.containsBlock(selectedBlock.second)) {
                            chunk.removeBlock(selectedBlock.second)
                        }
                    }
                }
            }

            if (mouse.isPressed(Button.RIGHT) || keyboard.isPressed(Key.V)) {
                val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera)
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
        }
    }

    private fun doMainRenderPass(selectedBlock: Pair<Chunk, Vector3>?) {
        RenderTargetManager.getDefault().start()
        RenderTargetManager.getDefault().clear()
        skyBox.render(camera)

        chunkRenderer.render(chunks, camera, ambientLight, directionalLight, selectedBlock)
    }
}