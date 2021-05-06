import chunks.blocks.BlockType
import chunks2.*
import devices.Button
import devices.Key
import devices.Timer
import devices.Window
import environment.sky.SkyBox
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.rendertarget.RenderTargetManager
import graphics.shadows.ShadowBox
import graphics.shadows.ShadowData
import graphics.shadows.ShadowRenderer
import math.Color
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

    private val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, ChunkGenerator.TERRAIN_HEIGHT, 0))
//    private val player = Player(Vector3(-80, TERRAIN_HEIGHT, 0))

    private val chunkManager = ChunkManager(camera.position)
    private val chunkRenderer = ChunkRenderer()
    private val entityRenderer = EntityRenderer()

    private val selector = Selector()
    private val skyBox = SkyBox("textures/sky/box", camera.zFar)

    private var chunks = ArrayList<Chunk>()
    private val entities = ArrayList<Entity>()

    private val ui = UserInterface(window.aspectRatio)
    private val page = UIPage("page")

    private const val sampleSize = 40
    private var printPerformance = false
    private val fps = FloatArray(sampleSize)

    @JvmStatic
    fun main(args: Array<String>) {
        GraphicsContext.init(Color(0f, 0f, 0f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING)

        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))
        RenderTargetManager.init(window)

        ShadowRenderer += ShadowBox(camera)
        val crossHair = Item("crossHair", ConstraintSet(
                CenterConstraint(ConstraintDirection.HORIZONTAL),
                CenterConstraint(ConstraintDirection.VERTICAL),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.05f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
        ), TexturedBackground("textures/userinterface/crosshair.jpg", null, UIColor.GREY))

        page += crossHair

        ui += page
        ui.showPage("page")

        var i = 0

        timer.reset()
        mouse.capture()

        while (!window.isClosed()) {
            window.poll()

            processInput()
            updateChunkManager()

//            val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera)
//            val shadows = ShadowRenderer.render(camera, sun, entities, entityRenderer, chunks, chunkRenderer)
            doMainRenderPass(arrayListOf())

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)
            window.synchronize()
            timer.update()

            if (printPerformance) {
                i = updatePerformance(i)
            }
        }

        window.destroy()
    }

    private fun updateChunkManager() {
        chunkManager.updatePosition(camera.position)
        chunks = chunkManager.determineVisibleChunks()
    }

//    private fun doMainRenderPass(selectedBlock: Pair<Chunk, Vector3>?, shadows: List<ShadowData>) {
//        RenderTargetManager.getDefault().start()
//        RenderTargetManager.getDefault().clear()
//        skyBox.render(camera)
//
//        chunkRenderer.render(chunks, camera, ambientLight, sun, shadows, selectedBlock)
//        entityRenderer.render(camera, ambientLight, sun, entities, shadows)
//    }

    private fun doMainRenderPass(shadows: List<ShadowData>) {
        RenderTargetManager.getDefault().start()
        RenderTargetManager.getDefault().clear()
        skyBox.render(camera)

        chunkRenderer.render(chunks, camera, ambientLight, sun, arrayListOf())
//        entityRenderer.render(camera, ambientLight, sun, entities, shadows)
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

        if (keyboard.isPressed(Key.F)) {
            println(chunks.size)
        }

        if (mouse.isCaptured()) {
            if (mouse.isPressed(Button.LEFT)) {
                val selectedBlock = selector.getSelected(chunks, camera, camera.position)
                if (selectedBlock != null) {
                    for (chunk in chunks) {
                        if (chunk.containsBlock(selectedBlock.first)) {
                            chunk.removeBlock(selectedBlock.first)
                        }
                    }
                }

//                val selectedBlock = selector.findSelectedItem(window, chunkRenderer, chunks, camera)
//                if (selectedBlock != null) {
//                    for (chunk in chunks) {
//                        if (chunk.containsBlock(selectedBlock.second)) {
//                            chunk.startBreakingBlock(selectedBlock.second, ToolType.SHOVEL, ToolMaterial.GOLD)
//                        }
//                    }
//                }
            }

            if (mouse.isPressed(Button.RIGHT) || keyboard.isPressed(Key.V)) {
                val selectedBlock = selector.getSelected(chunks, camera, camera.position)
                if (selectedBlock != null) {
                    for (chunk in chunks) {
                        if (chunk.containsBlock(selectedBlock.first)) {
                            chunk.addBlock(selectedBlock.first + selectedBlock.second.normal, BlockType2.TNT)
                        }
                    }
                }
            }
        }
//
//            if (mouse.isReleased(Button.LEFT)) {
//                chunkManager.stopBreaking()
//            }
//        }
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
}