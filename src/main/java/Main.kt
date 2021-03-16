import chunks.Chunk
import chunks.ChunkGenerator
import chunks.ChunkManager
import chunks.ChunkRenderer
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
import userinterface.UserInterface
import userinterface.items.Item
import userinterface.items.backgrounds.TexturedBackground
import userinterface.layout.constraints.ConstraintDirection
import userinterface.layout.constraints.ConstraintSet
import userinterface.layout.constraints.constrainttypes.AspectRatioConstraint
import userinterface.layout.constraints.constrainttypes.CenterConstraint
import userinterface.layout.constraints.constrainttypes.RelativeConstraint

object Main {
    private val window = Window("Minecraft", GraphicsContext::resize)
    private val keyboard = window.keyboard
    private val mouse = window.mouse
    private val timer = Timer()

    private val ambientLight = AmbientLight(Color(0.25f, 0.25f, 0.25f))
    private val directionalLight = DirectionalLight(Color(1.0f, 1.0f, 1.0f), Vector3(0.5f, 0.5f, 0.5f))

    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, ChunkGenerator.TERRAIN_HEIGHT, 0))

    private val chunkRenderer = ChunkRenderer()

    private val selector = Selector()
    private val skyBox = SkyBox("textures/sky/box", camera.zFar)

    private var chunks = ArrayList<Chunk>()

    private val ui = UserInterface(window.aspectRatio)

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0.25f, 0.25f, 0.25f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING)

        RenderTargetManager.init(window)

        val player = Player()

        timer.reset()
        mouse.capture()

        val page = UIPage("page")

        val crossHair = Item("crossHair", ConstraintSet(
                CenterConstraint(ConstraintDirection.HORIZONTAL),
                CenterConstraint(ConstraintDirection.VERTICAL),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.05f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
        ), TexturedBackground("textures/userinterface/crosshair.jpg", null, UIColor.GREY))

        page += crossHair
        ui += page
        ui.showPage("page")

        while (!window.isClosed()) {
            window.poll()

            chunks = ChunkManager.update(camera.position)

            processInput()

//            player.update(keyboard, mouse, timer.getDelta())
//            camera.followPlayer(player)

            val selectedBlock = selector.getLastSelected()
            doMainRenderPass(selectedBlock)

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)

            window.synchronize()
            timer.update()
        }

        window.destroy()
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
    }

    private fun doMainRenderPass(selectedBlock: Pair<Chunk, Vector3>?) {
        RenderTargetManager.getDefault().start()
        RenderTargetManager.getDefault().clear()
        skyBox.render(camera)

        chunkRenderer.render(chunks, camera, ambientLight, directionalLight, selectedBlock)
    }
}