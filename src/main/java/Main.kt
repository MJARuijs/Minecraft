import chunks.Biome
import chunks.ChunkGenerator
import chunks.ChunkManager
import chunks.ChunkRenderer
import chunks.blocks.Selector
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

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val window = Window("Minecraft", GraphicsContext::resize)
        val keyboard = window.keyboard
        val mouse = window.mouse
        val timer = Timer()

        GraphicsContext.init(Color(0.25f, 0.25f, 0.25f))
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.TEXTURE_MAPPING)

        RenderTargetManager.init(window)

        val camera = Camera(aspectRatio = window.aspectRatio, position = Vector3(0, ChunkGenerator.TERRAIN_HEIGHT, 0))

        val ambientLight = AmbientLight(Color(0.25f, 0.25f, 0.25f))
        val directionalLight = DirectionalLight(Color(1.0f, 1.0f, 1.0f), Vector3(0.5f, 0.5f, 0.5f))

        val renderDistance = 1
        val chunkRenderer = ChunkRenderer()
//        val chunks = 1
//        val x = 0
//        val z = 0
//        for (x in -chunks until chunks) {
//            for (z in -chunks until chunks) {

//                ChunkManager += ChunkGenerator.generateChunk(x, z, Biome.PLANES, 0)
//                ChunkManager += ChunkGenerator.generateChunk(Vector3(x, 0, 1), Biome.PLANES, 0)
//            }
//        }

        val skyBox = SkyBox("textures/sky/box", camera.zFar)

        val player = Player()

        val selector = Selector()

        timer.reset()
        mouse.release()

        while (!window.isClosed()) {
            window.poll()

            if (keyboard.isPressed(Key.ESCAPE)) {
                mouse.toggle()
            }

            if (keyboard.isPressed(Key.F1) || keyboard.isPressed(Key.KP1)) {
                window.close()
            }

            if (keyboard.isPressed(Key.P)) {
                println(camera.position)
            }

            GraphicsContext.clear(GraphicsOption.COLOR_BUFFER_BIT, GraphicsOption.DEPTH_BUFFER_BIT)

            skyBox.render(camera)
//            player.update(keyboard, mouse, timer.getDelta())

            if (mouse.isCaptured()) {
                camera.update(keyboard, mouse, timer.getDelta())
            }
//            camera.followPlayer(player)

            val chunks = ChunkManager.update(camera.position)
            chunkRenderer.render(chunks, camera, ambientLight, directionalLight)
//            selector.findSelectedItem(window, chunkRenderer, chunks, camera)

            window.synchronize()
            timer.update()
//            println(1f / timer.getDelta())
        }

        window.destroy()
    }
}