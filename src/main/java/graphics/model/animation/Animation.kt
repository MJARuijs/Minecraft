package graphics.model.animation

data class Animation(val name: String, val keyFrames: ArrayList<KeyFrame>, val loopEffect: LoopEffect = LoopEffect.NONE, val onFinish: () -> Unit = {}) {

    init {
        if (loopEffect != LoopEffect.NONE && keyFrames.size == 1) {
            throw InvalidAnimationException("Cannot create a looping animation with only 1 keyframe..")
        }
    }

    operator fun plusAssign(frame: KeyFrame) {
        keyFrames.add(frame)
    }

    fun numberOfFrames() = keyFrames.size

    fun copy(): Animation {
        return Animation(name, keyFrames, loopEffect, onFinish)
    }

}