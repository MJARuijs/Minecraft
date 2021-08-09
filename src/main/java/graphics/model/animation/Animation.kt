package graphics.model.animation

class Animation(val name: String, val keyFrames: List<KeyFrame>, val loopEffect: LoopEffect = LoopEffect.NONE, val onFinish: () -> Unit = {}) {

    private var reversing = false

    init {
        if (loopEffect != LoopEffect.NONE && keyFrames.size == 1) {
            throw InvalidAnimationException("Cannot create a looping animation with only 1 keyframe..")
        }
    }

    fun reverse() {
        reversing = !reversing
    }

    fun getNextFrame(currentTime: Float): KeyFrame? {
        if (reversing) {
            for (frame in keyFrames.reversed()) {
                if (currentTime < frame.timeStamp) {
                    return frame
                }
            }
        } else {
            for (frame in keyFrames) {
                if (currentTime < frame.timeStamp) {
                    return frame
                }
            }
            return keyFrames.last()
        }
        return null
    }

    fun getPreviousFrame(currentTime: Float): KeyFrame? {
        if (reversing) {

        } else {
            var previousFrame: KeyFrame? = null
            for (frame in keyFrames) {
                if (currentTime <= frame.timeStamp) {
                    previousFrame = frame
                } else {
                    return previousFrame
                }
            }
            return previousFrame
        }
        return null
    }
//
//    fun getPreviousAndNextFrames(currentTime: Float): Pair<KeyFrame?, KeyFrame?> {
//        var previousFrame: KeyFrame? = null
//        var nextFrame: KeyFrame? = null
//
//        for ((i, frame) in keyFrames.withIndex()) {
//            if (currentTime < frame.timeS)
//        }
//    }

    fun numberOfFrames() = keyFrames.size

    fun getLength(): Float {
        return keyFrames.last().timeStamp
    }

}