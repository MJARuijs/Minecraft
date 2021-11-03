import devices.Button
import devices.Key
import devices.Timer
import devices.Window
import environment.sky.SkyBox
import environment.terrain.Selector
import environment.terrain.blocks.BlockType
import environment.terrain.chunks.Chunk
import environment.terrain.chunks.ChunkManager
import environment.terrain.chunks.ChunkRenderer
import game.camera.Camera
import game.camera.CameraType
import game.player.controller.FreeController
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.entity.AnimatedEntity
import graphics.entity.Entity
import graphics.entity.EntityRenderer
import graphics.entity.StaticEntity
import graphics.lights.AmbientLight
import graphics.lights.PointLight
import graphics.lights.Sun
import graphics.model.ModelLoader
import graphics.model.Quad
import graphics.model.animation.LoopEffect
import graphics.model.animation.model.AnimatedModelCache
import graphics.model.mesh.Mesh
import graphics.postprocessing.HorizontalBlur
import graphics.postprocessing.VerticalBlur
import graphics.renderer.RenderData
import graphics.renderer.RenderEngine
import graphics.renderer.RenderType
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowBox
import graphics.shadows.ShadowRenderer
import math.Color
import math.matrices.Matrix4
import math.vectors.Vector3
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.*
import org.lwjgl.opengl.GL20.glStencilOpSeparate
import org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP
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
    private val sun = Sun(Color(directionalValue, directionalValue, directionalValue), Vector3(0.8f, 1.0f, 0.9f))

    private val camera = Camera(aspectRatio = window.aspectRatio)

    private val chunkManager = ChunkManager(camera.position)
    private val chunkRenderer = ChunkRenderer()

    private val renderEngine = RenderEngine(true)
    private val entities = ArrayList<Entity>()
    private val entityRenderer = EntityRenderer()

    private val selector = Selector()
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
        GraphicsContext.init(Color(0.25f, 0.25f, 0.25f))
        GraphicsContext.enable(
            GraphicsOption.FACE_CULLING,
            GraphicsOption.DEPTH_TESTING,
            GraphicsOption.STENCIL_TESTING,
            GraphicsOption.TEXTURE_MAPPING,
            GraphicsOption.MULTI_SAMPLE
        )

        UniversalParameters.init(window.aspectRatio, FontLoader(window.aspectRatio).load("fonts/candara.png"))
        RenderTargetManager.init(window)

        val shadowBox = ShadowBox(camera)
        renderEngine += shadowBox

        val crossHair = Item("crossHair", ConstraintSet(
                CenterConstraint(ConstraintDirection.HORIZONTAL),
                CenterConstraint(ConstraintDirection.VERTICAL),
                RelativeConstraint(ConstraintDirection.VERTICAL, 0.05f),
                AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
        ), TexturedBackground("textures/userinterface/crosshair.jpg", null, UIColor.GREY))

        page += crossHair

        ui += page
        ui.showPage("page")

        val animatedModel = AnimatedModelCache.get("models/player3.dae")
//        val houseModel = ModelLoader().load("models/house.obj", false)
        val houseModel = ModelLoader().load("models/house.obj", true)
        val boxModel = ModelLoader().load("models/box.dae", true)

//        val player = Player(animatedModel, Matrix4().translate(Vector3(0, 17, 0)))
        val house = StaticEntity(houseModel, Matrix4())
        val box = StaticEntity(boxModel, Matrix4())
        val surface = StaticEntity(boxModel, Matrix4().translate(Vector3(0, -7, 0)).scale(30.0f, 1.0f, 30.0f))
//        entities += player
//        entities += house
        entities += box
        entities += surface

        animatedModel.addAnimation("walking", listOf(
            Pair(4, 400),
            Pair(5, 400)
        ), LoopEffect.REVERSE)

        val freeController = FreeController(Vector3(0, 0, 5))
        val boneProgram = ShaderProgram.load("shaders/debug/bone.vert", "shaders/debug/bone.frag")

        var frameCounter = 0

        val mesh = MeshCreator.create()
        val mesh2 = MeshCreator.create()
        val mesh3 = MeshCreator.create()

        val outlineProgram = ShaderProgram.load("shaders/adjacency.vert", "shaders/adjacency.geom", "shaders/adjacency.frag")
        val godrayProgram = ShaderProgram.load("shaders/godrays.vert", "shaders/godrays.geom", "shaders/godrays.frag")
        val meshProgram = ShaderProgram.load("shaders/mesh.vert", "shaders/mesh.frag")

        timer.reset()
        mouse.capture()

        glLineWidth(5.0f)
        glDepthFunc(GL_LEQUAL)

        val pointLight = PointLight(Color(1, 1, 1, 1), Vector3(0, 1, 0) * 10000)
        val mesh2Transformation = Matrix4().translate(Vector3(0, -7, 0)).scale(30.0f, 1.0f, 30.0f)
        val mesh3Trans = Matrix4().translate(Vector3(0, -14, 0)).scale(60f, 1f, 60f)

        val shadowRenderer = ShadowRenderer()
        val shadowMapSampler = Sampler(0)

        val godrayTarget = RenderTarget(window.width, window.height, false, 1, 0, 1, 0)

        val horizontalBlurTarget = RenderTarget(window.width, window.height, false, 1, 0, 0, 0)
        val verticalBlurTarget = RenderTarget(window.width, window.height, false, 1, 0, 0, 0)

        val verticalBlur = VerticalBlur(2.0f)
        val horizontalBlur = HorizontalBlur(2.0f)

        val quad = Quad()
        val sampler = Sampler(0)
        val textureProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/debug/depth.frag")
        val blendProgram = ShaderProgram.load("shaders/postprocessing/blend.vert", "shaders/postprocessing/blend.frag")
        val mainTarget = RenderTarget(window.width, window.height, false, 1, 0, 1, 0)
        val blendTarget = RenderTarget(window.width, window.height, false, 1, 0, 1, 0)

        val sampler1 = Sampler(1)

        while (!window.isClosed()) {
            window.poll()

            if (mouse.isCaptured()) {
                freeController.update(camera, keyboard, mouse, timer.getDelta())
            }

            processInput()
            updateChunkManager()

//            entities.forEach { entity -> entity.update(timer.getDelta()) }

//            stencilShadowTest(mesh, mesh2, mesh2Transformation, meshProgram, outlineProgram, mesh3, mesh3Trans)

//            val shadowData = shadowRenderer.render(camera, sun, listOf(shadowBox), listOf(RenderData(entities, entityRenderer, RenderType.FORWARD)))
//            glViewport(0, 0, window.width, window.height)

//            mainTarget.start()
//            mainTarget.clear()


//            mainTarget.stop()

            glEnable(GL_BLEND)
            glDisable(GL_CULL_FACE)

//            mainTarget.renderTo(godrayTarget, GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
//            godrayTarget.start()
//            godrayTarget.clearColor()
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)


//            godrayTarget.stop()

//            godrayTarget.renderToScreen(GL_COLOR_BUFFER_BIT)
            meshProgram.start()
            meshProgram.set("projection", camera.projectionMatrix)
            meshProgram.set("view", camera.viewMatrix)
            sun.apply(meshProgram)
            ambientLight.apply(meshProgram)

            for (entity in entities) {
//                entity.render(meshProgram)
            }

            meshProgram.stop()

            godrayProgram.start()
            godrayProgram.set("projection", camera.projectionMatrix)
            godrayProgram.set("view", camera.viewMatrix)
            sun.apply(godrayProgram)
            entities.first().render(godrayProgram)

            godrayProgram.stop()
//            glEnable(GL_CULL_FACE)

//            mainTarget.start()
//            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//
////            godrayTarget.renderToScreen(GL_COLOR_BUFFER_BIT )
//            skyBox.render(camera)
//
//            meshProgram.start()
//            meshProgram.set("projection", camera.projectionMatrix)
//            meshProgram.set("view", camera.viewMatrix)
//            sun.apply(meshProgram)
//            ambientLight.apply(meshProgram)
//
//            for (entity in entities) {
//                entity.render(meshProgram)
//            }
//
//            meshProgram.stop()
//            mainTarget.stop()


//            blendProgram.start()
//            sampler.bind(mainTarget.getColorMap())
//            sampler1.bind(godrayTarget.getColorMap())
//            blendProgram.set("texture1", sampler.index)
//            blendProgram.set("texture2", sampler1.index)
//            quad.draw()
//            blendProgram.stop()


//            glBlendEquation(GL_FUNC_SUBTRACT)
//            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

//            godrayTarget.renderToScreen(GL_COLOR_BUFFER_BIT)


//            glDisable(GL_BLEND)
//            glBlendFunc(GL_ZERO, GL_ONE)

//            verticalBlur.apply(godrayTarget, verticalBlurTarget)
//            horizontalBlur.apply(verticalBlurTarget, horizontalBlurTarget)


//            glBlendEquation(GL_FUNC_ADD)
//            glBlendFunc(GL_ONE, GL_ONE)



//            blendTarget.start()
//            blendTarget.clear()
//            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//            blendProgram.start()
//            blendProgram.set("blendFactor", 0.5f)
//            sampler.bind(horizontalBlurTarget.getColorMap())
//            sampler1.bind(mainTarget.getColorMap())
//            blendProgram.set("texture1", sampler.index)
//            blendProgram.set("texture2", sampler1.index)
//            quad.draw()
//            blendProgram.stop()

//            horizontalBlurTarget.renderToScreen(GL_COLOR_BUFFER_BIT)

//            renderEngine.render(camera, ambientLight, sun, skyBox, arrayListOf(
//                    RenderData(entities, entityRenderer, RenderType.FORWARD),
//                    RenderData(chunks, chunkRenderer, RenderType.FORWARD)
//            ))

            ui.update(mouse, timer.getDelta())
            ui.draw(window.width, window.height)

            window.synchronize()
            timer.update()

            if (printPerformance) {
                frameCounter = updatePerformance(frameCounter)
            }
        }

        window.destroy()
    }

    private fun stencilShadowTest(mesh: Mesh, mesh2: Mesh, mesh2Transformation: Matrix4, meshProgram: ShaderProgram, outlineProgram: ShaderProgram, mesh3: Mesh, mesh3Trans: Matrix4) {
        glDepthMask(true)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        renderToDepth(mesh, mesh2, mesh2Transformation, mesh3, mesh3Trans, meshProgram)

        glEnable(GL_STENCIL_TEST)

        renderShadowIntoStencil(mesh, outlineProgram)
        renderShadowedScene(mesh, mesh2, mesh2Transformation, mesh3, mesh3Trans, meshProgram)

        glDisable(GL_STENCIL_TEST)
    }

    private fun renderToDepth(mesh1: Mesh, mesh2: Mesh, trans: Matrix4, mesh3: Mesh, mesh3Trans: Matrix4, shaderProgram: ShaderProgram) {
        glDrawBuffer(GL_NONE)
        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("model", Matrix4())
        mesh1.draw()
        shaderProgram.set("model", trans)
        mesh2.draw()
        shaderProgram.set("model", mesh3Trans)
        mesh3.draw()
        shaderProgram.stop()
    }

    private fun renderShadowIntoStencil(mesh1: Mesh, shaderProgram: ShaderProgram) {
        glDepthMask(false)
        glEnable(GL_DEPTH_CLAMP)
        glDisable(GL_CULL_FACE)

        glStencilFunc(GL_ALWAYS, 0, 0xFF)

        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR_WRAP, GL_KEEP)
        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR_WRAP, GL_KEEP)

        shaderProgram.start()
        sun.apply(shaderProgram)
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("model", Matrix4())
        mesh1.draw()
        shaderProgram.stop()

        glDisable(GL_DEPTH_CLAMP)
        glEnable(GL_CULL_FACE)
    }

    private fun renderShadowedScene(mesh1: Mesh, mesh2: Mesh, trans: Matrix4, mesh3: Mesh, mesh3Trans: Matrix4, shaderProgram: ShaderProgram) {
        glDrawBuffer(GL_BACK)

        glStencilFunc(GL_EQUAL, 0, 0xFF)

        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_KEEP)

        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("model", Matrix4())
        mesh1.draw()
        shaderProgram.set("model", trans)
        mesh2.draw()
        shaderProgram.set("model", mesh3Trans)
        mesh3.draw()
        shaderProgram.stop()
    }

    private fun renderAmbientLight() {

    }

    private fun updateChunkManager() {
        chunkManager.updatePosition(camera.position)
        chunks = chunkManager.determineVisibleChunks()
    }

    var controllerType = CameraType.FREE

    private fun processInput() {
        if (keyboard.isPressed(Key.ESCAPE)) {
            mouse.toggle()
        }

        if (keyboard.isPressed(Key.ONE)) {
            controllerType = CameraType.FIRST_PERSON
        }
        if (keyboard.isPressed(Key.TWO)) {
            controllerType = CameraType.THIRD_PERSON
        }
        if (keyboard.isPressed(Key.THREE)) {
            controllerType = CameraType.FREE
        }

        if (keyboard.isPressed(Key.F1) || keyboard.isPressed(Key.KP1)) {
            window.close()
        }

        if (mouse.isCaptured()) {
//            camera.update(keyboard, mouse, timer.getDelta())
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
            }

            if (mouse.isPressed(Button.RIGHT) || keyboard.isPressed(Key.V)) {
                val selectedBlock = selector.getSelected(chunks, camera, camera.position)
                if (selectedBlock != null) {
                    for (chunk in chunks) {
                        if (chunk.containsBlock(selectedBlock.first)) {
                            chunk.addBlock(selectedBlock.first + selectedBlock.second.normal, BlockType.DIAMOND_ORE)
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