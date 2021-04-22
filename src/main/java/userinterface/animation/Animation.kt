package userinterface.animation

abstract class Animation(val onFinish: () -> Unit = {}) {
    
    abstract fun apply(deltaTime: Float): Boolean

}